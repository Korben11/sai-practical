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

        consumer.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                System.out.println("AsyncSenderGateway received message: " + msg.getText());
                IResponse response = serializer.deserializeResponse(msg.getText());
                IRequest request = map.get(message.getJMSCorrelationID());
                onMessageArrived(request, response, message);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendRequest(IRequest request, String queue, Integer aggregationId) throws JMSException {
        String json = serializer.serializeRequest(request);
        System.out.println("Sending json request: " + json);
        Message message = producer.createMessage(json);
        message.setJMSReplyTo(consumer.getDestination());

        if (aggregationId != null) {
            message.setIntProperty(AGGREGATION_ID, aggregationId);
        }

        if (queue != null) {
            producer.send(message, queue);
            map.put(message.getJMSMessageID(), request);
            return;
        }

        producer.send(message);
        map.put(message.getJMSMessageID(), request);
    }

}
