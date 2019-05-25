package broker.gateways;

import broker.routers.ArchiveRouter;
import jmsmessenger.MessageReceiver;
import jmsmessenger.MessageSender;
import jmsmessenger.models.LoanArchive;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;
import jmsmessenger.serializers.LoanSerializer;

import static jmsmessenger.Constants.HTTP_LOCALHOST_8080_ARCHIVE_REST_ACCEPTED;
import static jmsmessenger.Constants.LOAN_CLIENT_REQUEST_QUEUE;

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
    private ArchiveRouter archiveRouter;

    public ClientGateway() {
        messageSender = new MessageSender();
        messageReceiver = new MessageReceiver(LOAN_CLIENT_REQUEST_QUEUE);

        archiveRouter = new ArchiveRouter(HTTP_LOCALHOST_8080_ARCHIVE_REST_ACCEPTED);

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

        // content based router
        LoanArchive loanArchive = new LoanArchive(loanRequest.getSsn(), loanRequest.getAmount(), loanReply.getBankId(), loanReply.getInterest());
        archiveRouter.archive(loanArchive);

        messageSender.send(replyMsg, message.getJMSReplyTo());
    }

    private void notify(LoanRequest request) {
        setChanged();
        notifyObservers(request);
    }
}
