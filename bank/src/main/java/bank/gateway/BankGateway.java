package bank.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import jmsmessenger.Constants;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.serializers.InterestSerializer;
import jmsmessenger.MessageReceiver;
import jmsmessenger.MessageSender;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class BankGateway extends Observable {
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    private Map<BankInterestRequest, String> map;
    private InterestSerializer serializer;

    public BankGateway(String requestQueue, String responseQueue) {
        messageSender = new MessageSender(responseQueue);
        messageReceiver = new MessageReceiver(requestQueue);
        serializer = new InterestSerializer();

        map = new HashMap<>();

        messageReceiver.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                BankInterestRequest request = serializer.deserializeBankInterestRequest(msg.getText());
                map.put(request, message.getJMSMessageID());
                notify(request);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendInterestReply(BankInterestRequest request, BankInterestReply reply) throws JMSException {
        String correlationId = map.get(request);
        String json = serializer.serializeBankInterestReply(reply);
        Message message = messageSender.createMessage(json);
        message.setJMSCorrelationID(correlationId);
        messageSender.send(message);
    }

    private void notify(BankInterestRequest request) {
        setChanged();
        notifyObservers(request);
    }
}
