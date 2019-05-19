package broker.gateways;

import jmsmessenger.MessageReceiver;
import jmsmessenger.MessageSender;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;
import jmsmessenger.serializers.LoanSerializer;
import jmsmessenger.serializers.InterestSerializer;

import static jmsmessenger.Constants.LOAN_CLIENT_REQUEST_QUEUE;
import static jmsmessenger.Constants.LOAN_CLIENT_RESPONSE_QUEUE;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class ClientGateway extends Observable {

    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    private Map<LoanRequest, Message> map;
    private LoanSerializer loanSerializer;

    public ClientGateway() {
        messageSender = new MessageSender(LOAN_CLIENT_RESPONSE_QUEUE);
        messageReceiver = new MessageReceiver(LOAN_CLIENT_REQUEST_QUEUE);

        loanSerializer = new LoanSerializer();
        map = new HashMap<>();

        messageReceiver.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                LoanRequest loanRequest = loanSerializer.deserializeLoanRequest(msg.getText());
                map.put(loanRequest, message);
                notify(loanRequest);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendReply(LoanReply loanReply, LoanRequest loanRequest) throws JMSException {
        Message message = map.get(loanRequest);
        String json = loanSerializer.serializeLoanReply(loanReply);
        Message replyMsg = messageSender.createMessage(json);
        replyMsg.setJMSCorrelationID(message.getJMSMessageID());

        messageSender.send(replyMsg);
    }

    private void notify(LoanRequest request) {
        setChanged();
        notifyObservers(request);
    }
}
