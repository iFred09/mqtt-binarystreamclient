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

            // create MQTT packet (like the example in the lecture slides: PYTHON)

            String topic = "labs/new-topic";
            String message = "Hello MQTT!";
            byte[] topicBytes = topic.getBytes();
            byte[] messageBytes = message.getBytes();

            int remainingLength = 2 + topicBytes.length + messageBytes.length;
            byte[] publishPacket = new byte[2048];

            publishPacket[0] = 0x30;
            publishPacket[1] = (byte) remainingLength;
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
