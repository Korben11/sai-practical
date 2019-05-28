package broker.gateways;

import broker.recipient.BankRecipientList;
import jmsmessenger.gateways.AsyncSenderGateway;
import jmsmessenger.gateways.IRequest;
import jmsmessenger.gateways.IResponse;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.serializers.GsonSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.HashMap;
import java.util.Map;

import static jmsmessenger.Constants.AGGREGATION_ID;
import static jmsmessenger.Constants.BANK_CLIENT_RESPONSE_QUEUE;

public abstract class ScatterGetter {

    private AsyncSenderGateway bankGateway;
    private BankRecipientList recipientList;
    private Aggregator aggregator;

    private Map<Integer, IRequest> mapAggregationIdToRequest;

    public ScatterGetter() {
        mapAggregationIdToRequest = new HashMap<>();

        aggregator = new Aggregator() {
            @Override
            public void onAllRepliesReceived(BankInterestReply interestReply, Integer aggregationId) {
                onBankInterestSelected(mapAggregationIdToRequest.get(aggregationId), interestReply);
            }
        };

        bankGateway = new AsyncSenderGateway(new GsonSerializer(BankInterestRequest.class, BankInterestReply.class), BANK_CLIENT_RESPONSE_QUEUE, null) {
            @Override
            public void onMessageArrived(IRequest request, IResponse response, Message message) {
                Integer aggId = null;
                try {
                    aggId = message.getIntProperty(AGGREGATION_ID);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                if (!mapAggregationIdToRequest.containsKey(aggId)) mapAggregationIdToRequest.put(aggId, request);
                aggregator.addBankInterestReply(response, aggId);
            }
        };

        recipientList = new BankRecipientList(bankGateway);
    }

    public int applyForLoan(BankInterestRequest request) {
        Aggregator.id++;
        int passed = recipientList.sendRequest(request, Aggregator.id);
        if (passed == 0) {
            return passed;
        }
        aggregator.addAggregator(Aggregator.id, passed);
        return passed;
    }

    public abstract void onBankInterestSelected(IRequest request, IResponse response);
}
