package mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class Subscribe {
    private static final Logger LOGGER = Logger.getLogger(Subscribe.class.getName());

    public static void main(String[] args) {
        try{
            Socket socket = new Socket("localhost", 1883);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            String clientId = "myClientId";

            // Construct CONNECT packet
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

            // Send CONNECT packet
            outputStream.write(connectPacket);
            outputStream.flush();

            // read MQTT Broker's response

            byte[] connack = new byte[4];
            int readed = inputStream.read(connack);

            if (readed == 4 && connack[0] == 0x20) {
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

            // create Subscribe packet

            String topic = "labs/my-topic";
            byte[] topicBytes = topic.getBytes();

            int subscribeLength = 2 + 1 + topicBytes.length + 1;
            byte[] subscribePacket = new byte[4 + subscribeLength];

            subscribePacket[0] = (byte) 0x82;
            subscribePacket[1] = (byte) subscribeLength;
            subscribePacket[2] = 0x00;
            subscribePacket[3] = (byte) topicBytes.length;
            System.arraycopy(topicBytes, 0, subscribePacket, 4, topicBytes.length);

            subscribePacket[4 + topicBytes.length] = (byte) 0x01;
            
            // DEBUG: print table
            
            StringBuilder hexString = new StringBuilder();
            for (int i=0 ; i<subscribePacket.length ; i++) {
            	hexString.append(String.format("%02x ", subscribePacket[i]));
            	if ((i+1) % 16 == 0) {
            		hexString.append('\n');
            	}
            }
            LOGGER.info("hex table: " + hexString.toString());

            // send packet

            outputStream.write(subscribePacket);
            outputStream.flush();

            LOGGER.info("Subscribe for the topic: " + topic + " complete.");

            // read SUBACK

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
                if (suback[3] == 0x00) {
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
        catch(Exception e){
            LOGGER.severe("Error: " + e.getMessage());
        }
    }
}
