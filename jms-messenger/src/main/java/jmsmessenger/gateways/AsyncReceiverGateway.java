package jmsmessenger.gateways;

import jmsmessenger.serializers.Serializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

public abstract class AsyncReceiverGateway extends ApplicationGateway {
    private Map<IRequest, Message> map;
    private IRouter router;

    public AsyncReceiverGateway(Serializer serializer, String consumerQueue, String producerQueue) {
        super(serializer, consumerQueue, producerQueue);
        map = new HashMap<>();

        consumer.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                System.out.println("AsyncReceiverGateway received message: " + msg.getText());
                IRequest request = serializer.deserializeRequest(msg.getText());
                map.put(request, message);
                onMessageArrived(request, null, message);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public AsyncReceiverGateway(Serializer serializer, String consumerQueue, String producerQueue, IRouter router) {
        super(serializer, consumerQueue, producerQueue);
        this.router = router;
        map = new HashMap<>();

        consumer.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                IRequest request = serializer.deserializeRequest(msg.getText());
                map.put(request, message);
                onMessageArrived(request, null, message);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendReply(IRequest request, IResponse response) throws JMSException {
        Message requestMessage = map.get(request);
        String json = serializer.serializeResponse(response);
        System.out.println("AsyncReceiverGateway sendReply: " + json);
        Message message = producer.createMessage(json);
        message.setJMSCorrelationID(requestMessage.getJMSMessageID());
        this.setAggregationId(message, requestMessage);
        this.contentBasedRouters(request, response, this.router);
        producer.send(message, requestMessage.getJMSReplyTo());
    }

    public abstract void setAggregationId(Message message, Message requestMessage) throws JMSException;
//    {
        // override in case needed
//        message.setIntProperty(AGGREGATION_ID, requestMessage.getIntProperty(AGGREGATION_ID));
//    }

    public abstract void contentBasedRouters(IRequest request, IResponse response, IRouter router);
//    {
        // override in case needed
//        LoanArchive loanArchive = new LoanArchive(loanRequest.getSsn(), loanRequest.getAmount(), loanReply.getBankId(), loanReply.getInterest());
//        archiveRouter.archive(loanArchive);
//    }

}
