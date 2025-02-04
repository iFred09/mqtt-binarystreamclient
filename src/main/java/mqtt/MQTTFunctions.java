package mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

public class MQTTFunctions {
    private static final Logger LOGGER = Logger.getLogger(MQTTFunctions.class.getName());

    private InputStream inputStream;
    private OutputStream outputStream;
    private String topic;
    private String clientId;
    
    private static int publishId = 2;

    public MQTTFunctions(InputStream inputStream, OutputStream outputStream, String topic, String clientId) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.topic = topic;
        this.clientId = clientId;
    }

    public void sendConnect(OutputStream outputStream, String clientId) throws IOException {
        byte[] clientIdBytes = clientId.getBytes();
        int connectLength = 12 + clientIdBytes.length;
        byte[] connectPacket = new byte[2 + connectLength];

        connectPacket[0] = 0x10; // CONNECT packet type
        connectPacket[1] = (byte) connectLength; // Remaining length
        connectPacket[2] = 0x00; // Protocol name length MSB
        connectPacket[3] = 0x04; // Protocol name length LSB
        connectPacket[4] = 'M';
        connectPacket[5] = 'Q';
        connectPacket[6] = 'T';
        connectPacket[7] = 'T';
        connectPacket[8] = 0x4;
        connectPacket[9] = 0x2; // Connect flags (Clean Session = 1)
        connectPacket[10] = 0x00; // Keep Alive MSB
        connectPacket[11] = 0x3C; // Keep Alive LSB (60 seconds)
        connectPacket[12] = 0x00; // Client ID length MSB
        connectPacket[13] = (byte) clientIdBytes.length; // Client ID length LSB
        System.arraycopy(clientIdBytes, 0, connectPacket, 14, clientIdBytes.length);

        outputStream.write(connectPacket);
        outputStream.flush();

        LOGGER.info("Trying to connect to broker with clientId:" + clientId);
    }

    public void receiveConnect(InputStream inputStream) throws IOException {
        byte[] connack = new byte[4];
        int read = inputStream.read(connack);
        if (read == 4 && connack[0] == 0x20) {
            LOGGER.info("Received CONNACK: " + Arrays.toString(connack));
            if (connack[3] == 0x0) {
                LOGGER.info("Connection accepted.");
            }
            else {
                LOGGER.severe("Connection refused, return code " + connack[3]);
            }
        }
        else {
            LOGGER.severe("Invalid CONNACK response received: " + Arrays.toString(connack));
        }
    }

    public void subscribe(OutputStream outputStream, String topic) throws IOException {
        byte[] topicBytes = topic.getBytes();

        int remainingLength = 2 + 2 + topicBytes.length + 1;
        byte[] subscribePacket = new byte[2 + remainingLength];

        subscribePacket[0] = (byte) 0x82;
        subscribePacket[1] = (byte) remainingLength;
        subscribePacket[2] = 0x00;
        subscribePacket[3] = 0x01;
        subscribePacket[4] = (byte) ((topicBytes.length >> 8) & 0xFF);
        subscribePacket[5] = (byte) (topicBytes.length & 0xFF);
        System.arraycopy(topicBytes, 0, subscribePacket, 6, topicBytes.length);

        subscribePacket[6 + topicBytes.length] = (byte) 0x01;

        outputStream.write(subscribePacket);
        outputStream.flush();

        LOGGER.info("Subscribed to topic: " + topic);
    }

    public void receiveSuback(InputStream inputStream) throws IOException {
        byte[] suback = new byte[5];
        int subackRead = 0;
        try {
            subackRead = inputStream.read(suback);
        }
        catch (IOException e) {
            LOGGER.severe("error: " + e.getMessage());
        }

        if (subackRead == 5) {
            LOGGER.info("Received SUBACK: " + Arrays.toString(suback));
            if (suback[3] == 0x01 || suback[3] == 0x00) {
                LOGGER.info("Subscription accepted.");
            }
            else {
                LOGGER.severe("Subscription refused, return code " + suback[3]);
            }
        }
        else {
            LOGGER.severe("Invalid SUBACK response received: " + Arrays.toString(suback));
        }
    }

    public int publish(OutputStream outputStream, String topic, String message, int qos) throws IOException {
        byte[] topicBytes = topic.getBytes();
        byte[] messageBytes = message.getBytes();

        int publishLength = 2 + topicBytes.length + 2 + messageBytes.length;
        byte[] publishPacket = new byte[2 + publishLength];

        if(qos == 0) publishPacket[0] = 0x30;
        else if (qos == 1) publishPacket[0] = 0x32;
        else if (qos == 2) publishPacket[0] = 0x34;
        else {
        	LOGGER.severe("QoS " + qos + " invalid.");
        	throw new IOException("QoS invalid");
        }
        publishPacket[1] = (byte) publishLength;
        publishPacket[2] = (byte) ((topicBytes.length >> 8) & 0xFF);
        publishPacket[3] = (byte) (topicBytes.length & 0xFF);
        System.arraycopy(topicBytes, 0, publishPacket, 4, topicBytes.length);
        
        LOGGER.info("Publish Packet ID: " + publishId);
        publishPacket[4 + topicBytes.length] = (byte) ((publishId >> 8) & 0xFF);
        publishPacket[5 + topicBytes.length] = (byte) (publishId & 0xFF);
        publishId++;

        System.arraycopy(messageBytes, 0, publishPacket, 6 + topicBytes.length, messageBytes.length);
        // send packet

        outputStream.write(publishPacket);
        outputStream.flush();

        LOGGER.info("Publish complete.");
        return publishId - 1;
    }

    public void receivePuback(InputStream inputStream, int expectedPacketId) throws IOException {
        byte[] header = new byte[2];

        while (true) {
            int bytesRead = inputStream.read(header);
            if (bytesRead != 2) {
                LOGGER.severe("Failed to read PUBACK header.");
                return;
            }

            int packetType = header[0] & 0xF0;  // Vérifier le type du paquet
            int remainingLength = header[1] & 0xFF;

            if (packetType == 0x40 && remainingLength == 2) {  // Si c'est un PUBACK
                byte[] packetId = new byte[2];
                bytesRead = inputStream.read(packetId);
                if (bytesRead != 2) {
                    LOGGER.severe("Failed to read PUBACK packet ID.");
                    return;
                }

                int receivedPacketId = ((packetId[0] & 0xFF) << 8) | (packetId[1] & 0xFF);

                if (receivedPacketId == expectedPacketId) {
                    LOGGER.info("Correct PUBACK received for Packet ID: " + receivedPacketId);
                    return;
                } else {
                    LOGGER.warning("Unexpected PUBACK received: " + receivedPacketId + " (expected: " + expectedPacketId + ")");
                }
            } else {
                LOGGER.warning("Ignoring unexpected packet: " + Arrays.toString(header));
                byte[] skip = new byte[remainingLength];
                inputStream.read(skip); // Lire et ignorer ce paquet
            }
        }
    }
    
    public void receiveAndParseMessage(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[256];
        int length = inputStream.read(buffer);
        if (length > 0) {
            parseMqttMessage(buffer, length);
        }
    }

    private void parseMqttMessage(byte[] packet, int length) {
        if ((packet[0] & 0xF0) == 0x30) { // Vérifie si c'est un PUBLISH
            int topicLength = ((packet[2] & 0xFF) << 8) | (packet[3] & 0xFF);
            String topic = new String(packet, 4, topicLength);
            
            int payloadStart = 4 + topicLength;
            String message = new String(packet, payloadStart, length - payloadStart);
            
            LOGGER.info("New message from topic: " + topic);
            LOGGER.info("Message: " + message);
        }
    }
}
