package broker.gateways;

import broker.routers.ArchiveRouter;
import jmsmessenger.gateways.Consumer;
import jmsmessenger.gateways.Producer;
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

public abstract class ClientGateway {
    private Producer producer;
    private Consumer consumer;
    private Map<LoanRequest, Message> map;
    private LoanSerializer loanSerializer;
    private ArchiveRouter archiveRouter;

    public ClientGateway() {
        producer = new Producer();
        consumer = new Consumer(LOAN_CLIENT_REQUEST_QUEUE);

        archiveRouter = new ArchiveRouter(HTTP_LOCALHOST_8080_ARCHIVE_REST_ACCEPTED);

        loanSerializer = new LoanSerializer();
        map = new HashMap<>();

        consumer.onMessage(message -> {
            TextMessage msg = (TextMessage) message;
            try {
                LoanRequest loanRequest = loanSerializer.deserializeLoanRequest(msg.getText());
                map.put(loanRequest, message);
                onResponse(loanRequest);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendReply(LoanReply loanReply, LoanRequest loanRequest) throws JMSException {
        Message message = map.get(loanRequest);
        String json = loanSerializer.serializeLoanReply(loanReply);
        Message replyMsg = producer.createMessage(json);
        replyMsg.setJMSCorrelationID(message.getJMSMessageID());

        // content based router
        LoanArchive loanArchive = new LoanArchive(loanRequest.getSsn(), loanRequest.getAmount(), loanReply.getBankId(), loanReply.getInterest());
        archiveRouter.archive(loanArchive);

        producer.send(replyMsg, message.getJMSReplyTo());
    }

    public abstract void onResponse(LoanRequest loanRequest);
}
