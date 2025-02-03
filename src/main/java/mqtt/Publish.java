package mqtt;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class Publish {
    private static final Logger LOGGER = Logger.getLogger(ConnectConnack.class.getName());

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
            connectPacket[8] = 0x04; // Protocol level (MQTT 3.1.1)
            connectPacket[9] = 0x02; // Connect flags (Clean Session = 1)
            connectPacket[10] = 0x00; // Keep Alive MSB
            connectPacket[11] = 0x3C; // Keep Alive LSB (60 seconds)
            connectPacket[12] = 0x00; // Client ID length MSB
            connectPacket[13] = (byte) clientIdBytes.length; // Client ID length LSB
            System.arraycopy(clientIdBytes, 0, connectPacket, 14, clientIdBytes.length);
            
            // Send CONNECT packet
            outputStream.write(connectPacket);
            outputStream.flush();
            
            // Read CONNACK response
            byte[] connack = new byte[4];
            int readBytes = inputStream.read(connack);
            
            if (readBytes == 4 && connack[0] == 0x20 && connack[3] == 0x00) {
                LOGGER.info("Connection accepted.");
            } else {
                LOGGER.severe("Connection failed: " + Arrays.toString(connack));
                socket.close();
                return;
            }

            // create MQTT packet (like the example in the lecture slides: PYTHON)

            String topic = "labs/new-topic";
            String message = "Hello MQTT!";
            byte[] topicBytes = topic.getBytes();
            byte[] messageBytes = message.getBytes();

            int publishLength = 2 + topicBytes.length + messageBytes.length;
            byte[] publishPacket = new byte[2 + publishLength];

            publishPacket[0] = 0x30;
            publishPacket[1] = (byte) publishLength;
            publishPacket[2] = 0x00;

            publishPacket[3] = (byte) topicBytes.length;
            System.arraycopy(topicBytes, 0, publishPacket, 4, topicBytes.length);

            System.arraycopy(messageBytes, 0, publishPacket, 4 + topicBytes.length, messageBytes.length);
            // send packet

            outputStream.write(publishPacket);
            outputStream.flush();

            LOGGER.info("Publish complete.");
        }
        catch(Exception e){
            LOGGER.severe("Error: " + e.getMessage());
        }
    }
}
