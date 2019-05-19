package loanclient.gateway;

import jmsmessenger.MessageReceiver;
import jmsmessenger.MessageSender;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;
import jmsmessenger.serializers.LoanSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import static jmsmessenger.Constants.LOAN_CLIENT_REQUEST_QUEUE;
import static jmsmessenger.Constants.LOAN_CLIENT_RESPONSE_QUEUE;

public class ClientGateway extends Observable {
    private MessageSender messageSender;
    private MessageReceiver messageReceiver;
    private Map<String, LoanRequest> map;
    private LoanSerializer serializer;

    public ClientGateway() {
        messageSender = new MessageSender(LOAN_CLIENT_REQUEST_QUEUE);
        messageReceiver = new MessageReceiver(LOAN_CLIENT_RESPONSE_QUEUE);
        serializer = new LoanSerializer();

        map = new HashMap<>();

        messageReceiver.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                LoanReply loanReply = serializer.deserializeLoanReply(msg.getText());
                LoanRequest loanRequest = map.get(msg.getJMSCorrelationID());
                notify(loanRequest, loanReply);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendLoanRequest(LoanRequest loanRequest) throws JMSException {
        String json = serializer.serializeLoanRequest(loanRequest);
        Message message = messageSender.createMessage(json);
        messageSender.send(message);
        map.put(message.getJMSMessageID(), loanRequest);
    }

    private void notify(LoanRequest loanRequest, LoanReply loanReply) {
        ClientArgs clientArgs = new ClientArgs(loanReply, loanRequest);
        setChanged();
        notifyObservers(clientArgs);
    }
}
