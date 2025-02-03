package mqtt;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class ConnectConnack {
    private static final Logger LOGGER = Logger.getLogger(ConnectConnack.class.getName());

    public static void main(String[] args) {
        try{
            Socket socket = new Socket("localhost", 1883);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            String clientId = "myClientId";

            // create MQTT packet (like the example in the lecture slides: PYTHON)

            byte[] connectPacket = {0x10, 0x13, 0x0, 0x4, 0x4d, 0x51, 0x54, 0x54, 0x4, 0x2, 0x0, 0x3c, 0x0, 0x7, 0x70, 0x79, 0x74, 0x68, 0x6f, 0x6e, 0x31};

            // send packet

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
                    LOGGER.info("Connection refused, return code " + connack[3]);
                }
            }
            else {
                LOGGER.info("Invalid CONNACK response received: " + Arrays.toString(connack));
            }

        }
        catch(Exception e){
            LOGGER.severe("Error: " + e.getMessage());
        }
    }
}
