package mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class AsynchronousMessages {
    private static final Logger LOGGER = Logger.getLogger(AsynchronousMessages.class.getName());

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1883);
        String clientId = "myClientId";
        String topic = "labs/my-topic";
        String message1 = "Hello World";
        String message2 = "babar";
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        MQTTFunctions mqttFunctions = new MQTTFunctions(inputStream, outputStream, clientId, topic);

        mqttFunctions.sendConnect(outputStream, clientId);
        mqttFunctions.receiveConnect(inputStream);
        mqttFunctions.subscribe(outputStream, topic);
        mqttFunctions.receiveSuback(inputStream);
        mqttFunctions.publish(outputStream, topic, message1);
        mqttFunctions.receivePuback(inputStream);
        mqttFunctions.publish(outputStream, topic, message2);
        mqttFunctions.publish(outputStream, topic, message1);
    }
}
