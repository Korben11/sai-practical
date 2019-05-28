package jmsmessenger.gateways;

import jmsmessenger.serializers.Serializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

import static jmsmessenger.Constants.AGGREGATION_ID;

public abstract class AsyncSenderGateway extends ApplicationGateway {
    private Map<String, IRequest> map;

    public AsyncSenderGateway(Serializer serializer, String consumerQueue, String producerQueue) {
        super(serializer, consumerQueue, producerQueue);
        map = new HashMap<>();

        messageReceiverGateway.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            Integer aggregationId = null;
            try {
                if (message.propertyExists(AGGREGATION_ID))
                    aggregationId = message.getIntProperty(AGGREGATION_ID);
                System.out.println("AsyncSenderGateway received message: " + msg.getText());
                IResponse response = serializer.deserializeResponse(msg.getText());
                IRequest request = map.get(message.getJMSCorrelationID());

                onMessageArrived(request, response, aggregationId);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendRequest(IRequest request, String queue, Integer aggregationId) {
        String json = serializer.serializeRequest(request);
        System.out.println("Sending json request: " + json);
        try {
            Message message = messageSenderGateway.createMessage(json);
            message.setJMSReplyTo(messageReceiverGateway.getDestination());
            if (aggregationId != null) {
                message.setIntProperty(AGGREGATION_ID, aggregationId);
            }

            if (queue != null) {
                messageSenderGateway.send(message, queue);
                map.put(message.getJMSMessageID(), request);
                return;
            }

            messageSenderGateway.send(message);
            map.put(message.getJMSMessageID(), request);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
