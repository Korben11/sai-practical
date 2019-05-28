package broker.gateways;

import jmsmessenger.gateways.IResponse;
import jmsmessenger.models.BankInterestReply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Aggregator {

    public static int id = 0;

    private Map<Integer, Integer> mapAggIdToSentCount;
    private Map<Integer, ArrayList<IResponse>> mapAggIdToInterestReplies;

    public Aggregator() {
        mapAggIdToSentCount = new HashMap<>();
        mapAggIdToInterestReplies = new HashMap<>();
    }

    public void addAggregator(Integer aggregationId, Integer sentCount) {
        mapAggIdToSentCount.put(aggregationId, sentCount);
        mapAggIdToInterestReplies.put(aggregationId, new ArrayList<>());
    }

    public void addBankInterestReply(IResponse response, Integer aggregationId) {
        mapAggIdToInterestReplies.get(aggregationId).add(response);
        checkReplies(aggregationId);
    }

    private void checkReplies(Integer aggregatorId) {
        if (!mapAggIdToSentCount.get(aggregatorId).equals(this.getNrOfReplies(aggregatorId))){
            return;
        }

        BankInterestReply bankInterestReply = null;
        for(IResponse interestReply: mapAggIdToInterestReplies.get(aggregatorId)) {
            if (bankInterestReply == null) {
                bankInterestReply = (BankInterestReply) interestReply;
                continue;
            }
            if (((BankInterestReply)interestReply).getInterest() < bankInterestReply.getInterest()) {
                bankInterestReply = (BankInterestReply)interestReply;
            }
        }

        onAllRepliesReceived(bankInterestReply, aggregatorId);
    }

    private Integer getNrOfReplies(Integer aggregatorId) {
        return mapAggIdToInterestReplies.get(aggregatorId).size();
    }

    public abstract void onAllRepliesReceived(BankInterestReply interestReply, Integer aggregationId);
}
