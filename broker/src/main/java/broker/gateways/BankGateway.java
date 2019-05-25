package broker.gateways;

import jmsmessenger.MessageReceiver;
import jmsmessenger.MessageSender;
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
    private MessageReceiver messageReceiver;
    private MessageSender messageSender;
    private Map<String, BankInterestRequest> map;
    private InterestSerializer interestSerializer;

    public BankGateway() {
        messageReceiver = new MessageReceiver(BANK_CLIENT_RESPONSE_QUEUE);
        messageSender = new MessageSender();

        interestSerializer = new InterestSerializer();
        map = new HashMap<>();

        messageReceiver.onMessage(message -> {
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
        Message message = messageSender.createMessage(json);
        message.setIntProperty(AGGREGATION_ID, aggregationId);
        messageSender.send(message, queue);

        map.put(message.getJMSMessageID(), interestRequest);
    }

    public abstract void onBankInterestArrived(BankInterestRequest interestRequest, BankInterestReply interestReply, Integer aggregationId);
}
