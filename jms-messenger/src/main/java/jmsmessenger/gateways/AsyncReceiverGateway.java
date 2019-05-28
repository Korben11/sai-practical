package jmsmessenger.gateways;

import jmsmessenger.serializers.Serializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

import static jmsmessenger.Constants.AGGREGATION_ID;

public abstract class AsyncReceiverGateway extends ApplicationGateway {
    private Map<IRequest, Message> map;

    public AsyncReceiverGateway(Serializer serializer, String consumerQueue, String producerQueue) {
        super(serializer, consumerQueue, producerQueue);
        map = new HashMap<>();

        messageReceiverGateway.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            Integer aggregationId = null;
            try {
                if (message.propertyExists(AGGREGATION_ID))
                    aggregationId = message.getIntProperty(AGGREGATION_ID);
                System.out.println("AsyncReceiverGateway received message: " + msg.getText());
                IRequest request = serializer.deserializeRequest(msg.getText());
                map.put(request, message);
                onMessageArrived(request, null, aggregationId);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendReply(IRequest request, IResponse response) {
        Message requestMessage = map.get(request);
        String json = serializer.serializeResponse(response);
        System.out.println("AsyncReceiverGateway sendReply: " + json);
        Message message = null;
        try {
            message = messageSenderGateway.createMessage(json);
            message.setJMSCorrelationID(requestMessage.getJMSMessageID());
            if (requestMessage.propertyExists(AGGREGATION_ID))
                message.setIntProperty(AGGREGATION_ID, requestMessage.getIntProperty(AGGREGATION_ID));
            messageSenderGateway.send(message, requestMessage.getJMSReplyTo());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
