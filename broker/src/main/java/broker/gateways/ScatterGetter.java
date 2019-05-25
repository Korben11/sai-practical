package broker.gateways;

import broker.recipient.BankRecipientList;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;

import java.util.HashMap;
import java.util.Map;

public abstract class ScatterGetter {

    private BankGateway bankGateway;
    private BankRecipientList recipientList;
    private Aggregator aggregator;

    private static int aggregationId = 0;

    private Map<Integer, BankInterestRequest> mapIntegerBankInterestRequest;

    public ScatterGetter() {
        mapIntegerBankInterestRequest = new HashMap<>();

        aggregator = new Aggregator() {
            @Override
            public void onAllRepliesReceived(BankInterestReply interestReply, Integer aggregationId) {
                onBankInterestSelected(mapIntegerBankInterestRequest.get(aggregationId), interestReply);
            }
        };

        this.bankGateway = new BankGateway() {
            @Override
            public void onBankInterestArrived(BankInterestRequest interestRequest, BankInterestReply interestReply, Integer aggregationId) {
                if (!mapIntegerBankInterestRequest.containsKey(aggregationId)) mapIntegerBankInterestRequest.put(aggregationId, interestRequest);
                aggregator.addBankInterestReply(interestReply, aggregationId);
            }
        };

        recipientList = new BankRecipientList(bankGateway);
    }

    public void applyForLoan(BankInterestRequest request) {
        aggregationId++;
        int passed = recipientList.sendRequest(request, aggregationId);
        if (passed == 0) {
            // TODO: reject directly
            System.out.println("Rejected directly");
            return;
        }
        aggregator.addAggregator(aggregationId, passed);
    }

    public abstract void onBankInterestSelected(BankInterestRequest interestRequest, BankInterestReply interestReply);
}
