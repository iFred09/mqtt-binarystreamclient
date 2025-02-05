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
        String message3 = "coucou tout le monde";
        String message4 = "ça dit quoi le projet MQTT";
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        MQTTFunctions mqttFunctions = new MQTTFunctions(inputStream, outputStream, clientId, topic);
        
        int id1, id2, id3, id4, id5;
        mqttFunctions.sendConnect(outputStream, clientId);
        mqttFunctions.receiveConnect(inputStream);
        mqttFunctions.subscribe(outputStream, topic);
        mqttFunctions.receiveSuback(inputStream);
        
        Thread listenerThread = new Thread(() -> {
			try {
				mqttFunctions.receiveAndParseMessage(inputStream);
			} catch (IOException e) {
				LOGGER.severe("Error when receiving the message: " + e.getMessage());
			}
		});
        listenerThread.start();
        
        id1 = mqttFunctions.publish(outputStream, topic, message1, 1);
        id2 = mqttFunctions.publish(outputStream, topic, message2, 1);
        id3 = mqttFunctions.publish(outputStream, topic, message3, 2);
        id4 = mqttFunctions.publish(outputStream, topic, message4, 2);
        id5 = mqttFunctions.publish(outputStream, topic, message2, 1);
    }
}
