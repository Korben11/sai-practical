package bank.gateway;

import java.util.HashMap;
import java.util.Map;

import static jmsmessenger.Constants.AGGREGATION_ID;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.serializers.InterestSerializer;
import jmsmessenger.MessageReceiver;
import jmsmessenger.MessageSender;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public abstract class BankGateway {
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    private Map<BankInterestRequest, Message> map;
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
        Message message = messageSender.createMessage(json);
        message.setJMSCorrelationID(reqMsg.getJMSMessageID());
        message.setIntProperty(AGGREGATION_ID, reqMsg.getIntProperty(AGGREGATION_ID));
        messageSender.send(message);
    }

    public abstract void onResponse(BankInterestRequest interestRequest);
}
