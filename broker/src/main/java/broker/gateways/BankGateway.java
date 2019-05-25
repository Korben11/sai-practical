package broker.gateways;

import jmsmessenger.gateways.Consumer;
import jmsmessenger.gateways.Producer;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.serializers.InterestSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

import static jmsmessenger.Constants.BANK_CLIENT_RESPONSE_QUEUE;
import static jmsmessenger.Constants.AGGREGATION_ID;


public abstract class BankGateway {
    private Consumer consumer;
    private Producer producer;
    private Map<String, BankInterestRequest> map;
    private InterestSerializer interestSerializer;

    public BankGateway() {
        consumer = new Consumer(BANK_CLIENT_RESPONSE_QUEUE);
        producer = new Producer();

        interestSerializer = new InterestSerializer();
        map = new HashMap<>();

        consumer.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                BankInterestReply interestReply = interestSerializer.deserializeBankInterestReply(msg.getText());
                BankInterestRequest interestRequest = map.get(message.getJMSCorrelationID());
                onBankInterestArrived(interestRequest, interestReply, message.getIntProperty(AGGREGATION_ID));
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendRequest(BankInterestRequest interestRequest, String queue, Integer aggregationId) throws JMSException {
        String json = interestSerializer.serializeBankInterestRequest(interestRequest);
        Message message = producer.createMessage(json);
        message.setIntProperty(AGGREGATION_ID, aggregationId);
        producer.send(message, queue);

        map.put(message.getJMSMessageID(), interestRequest);
    }

    public abstract void onBankInterestArrived(BankInterestRequest interestRequest, BankInterestReply interestReply, Integer aggregationId);
}
