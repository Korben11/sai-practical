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
    private CreditHistoryEnricher creditHistoryEnricher;

    public BankGateway() {
        messageReceiver = new MessageReceiver(BANK_CLIENT_RESPONSE_QUEUE);
        messageSender = new MessageSender();

        creditHistoryEnricher = new CreditHistoryEnricher("http://localhost:8080/credit/rest/history/");

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

    public void sendRequest(BankInterestRequest interestRequest, int ssn) throws JMSException {
        // content enricher
        CreditHistory creditHistory = creditHistoryEnricher.getCreditHistory(ssn);
        if (creditHistory != null){
            interestRequest.setCreditScore(creditHistory.getCredit());
            interestRequest.setHistory(creditHistory.getHistory());
        }

        String json = interestSerializer.serializeBankInterestRequest(interestRequest);
        for (Constants.BANK bank: Constants.BANK.values()) {
            Message message = messageSender.createMessage(json);

            String queue = bank + Constants.BANK_CLIENT_REQUEST_QUEUE;
            messageSender.send(message, queue);
            map.put(message.getJMSMessageID(), interestRequest);
        }

    }

    private void notify(BankInterestRequest interestRequest, BankInterestReply interestReply) {
        BankArgs args = new BankArgs(interestRequest, interestReply);
        setChanged();
        notifyObservers(args);
    }
}
