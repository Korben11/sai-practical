package loanclient.gateway;

import jmsmessenger.gateways.Consumer;
import jmsmessenger.gateways.Producer;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;
import jmsmessenger.serializers.LoanSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;


public abstract class ClientGateway {
    private Producer producer;
    private Consumer consumer;
    private Map<String, LoanRequest> map;
    private LoanSerializer serializer;

    public ClientGateway(String requestQueue, String responseQueue) {
        producer = new Producer(requestQueue);
        consumer = new Consumer(responseQueue);
        serializer = new LoanSerializer();

        map = new HashMap<>();

        consumer.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                LoanReply loanReply = serializer.deserializeLoanReply(msg.getText());
                LoanRequest loanRequest = map.get(msg.getJMSCorrelationID());
                onResponse(loanRequest, loanReply);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendLoanRequest(LoanRequest loanRequest) throws JMSException {
        String json = serializer.serializeLoanRequest(loanRequest);
        Message message = producer.createMessage(json);
        message.setJMSReplyTo(consumer.getDestination());
        producer.send(message);
        map.put(message.getJMSMessageID(), loanRequest);
    }

    public abstract void onResponse(LoanRequest loanRequest, LoanReply loanReply);
}
