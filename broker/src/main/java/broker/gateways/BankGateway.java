package broker.gateways;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import broker.enrichers.CreditHistoryEnricher;
import jmsmessenger.Constants;
import jmsmessenger.MessageReceiver;
import jmsmessenger.MessageSender;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.models.CreditHistory;
import jmsmessenger.serializers.InterestSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import static jmsmessenger.Constants.BANK_CLIENT_RESPONSE_QUEUE;


public class BankGateway extends Observable {
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
                notify(interestRequest, interestReply);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendRequest(BankInterestRequest interestRequest, String queue) throws JMSException {
        String json = interestSerializer.serializeBankInterestRequest(interestRequest);
        Message message = messageSender.createMessage(json);
        messageSender.send(message, queue);

        map.put(message.getJMSMessageID(), interestRequest);
    }

    private void notify(BankInterestRequest interestRequest, BankInterestReply interestReply) {
        BankArgs args = new BankArgs(interestRequest, interestReply);
        setChanged();
        notifyObservers(args);
    }
}
