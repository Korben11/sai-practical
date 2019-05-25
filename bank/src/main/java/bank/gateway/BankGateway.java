package bank.gateway;

import java.util.HashMap;
import java.util.Map;

import static jmsmessenger.Constants.AGGREGATION_ID;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.serializers.InterestSerializer;
import jmsmessenger.gateways.Consumer;
import jmsmessenger.gateways.Producer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public abstract class BankGateway {
    private Producer producer;
    private Consumer consumer;
    private Map<BankInterestRequest, Message> map;
    private InterestSerializer serializer;

    public BankGateway(String requestQueue, String responseQueue) {
        producer = new Producer(responseQueue);
        consumer = new Consumer(requestQueue);
        serializer = new InterestSerializer();

        map = new HashMap<>();

        consumer.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                BankInterestRequest request = serializer.deserializeBankInterestRequest(msg.getText());
                map.put(request, message);
                onResponse(request);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendInterestReply(BankInterestRequest request, BankInterestReply reply) throws JMSException {
        Message reqMsg = map.get(request);
        String json = serializer.serializeBankInterestReply(reply);
        Message message = producer.createMessage(json);
        message.setJMSCorrelationID(reqMsg.getJMSMessageID());
        message.setIntProperty(AGGREGATION_ID, reqMsg.getIntProperty(AGGREGATION_ID));
        producer.send(message);
    }

    public abstract void onResponse(BankInterestRequest interestRequest);
}
