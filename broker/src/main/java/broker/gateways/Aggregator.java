package broker.gateways;

import jmsmessenger.models.BankInterestReply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Aggregator {

    private Map<Integer, Integer> mapAggIdToSentCount;
    private Map<Integer, ArrayList<BankInterestReply>> mapAggIdToInterestReplies;

    public Aggregator() {
        mapAggIdToSentCount = new HashMap<>();
        mapAggIdToInterestReplies = new HashMap<>();
    }

    public void addAggregator(Integer aggregationId, Integer sentCount) {
        mapAggIdToSentCount.put(aggregationId, sentCount);
        mapAggIdToInterestReplies.put(aggregationId, new ArrayList<>());
    }

    public void addBankInterestReply(BankInterestReply interestReply, Integer aggregationId) {
        mapAggIdToInterestReplies.get(aggregationId).add(interestReply);
        checkReplies(aggregationId);
    }

    private void checkReplies(Integer aggregatorId) {
        if (!mapAggIdToSentCount.get(aggregatorId).equals(this.getNrOfReplies(aggregatorId))){
            return;
        }

        BankInterestReply bankInterestReply = null;
        for(BankInterestReply interestReply: mapAggIdToInterestReplies.get(aggregatorId)) {
            if (bankInterestReply == null) {
                bankInterestReply = interestReply;
                continue;
            }
            if (interestReply.getInterest() < bankInterestReply.getInterest()) {
                bankInterestReply = interestReply;
            }
        }

        onAllRepliesReceived(bankInterestReply, aggregatorId);
    }

    private Integer getNrOfReplies(Integer aggregatorId) {
        return mapAggIdToInterestReplies.get(aggregatorId).size();
    }

    public abstract void onAllRepliesReceived(BankInterestReply interestReply, Integer aggregationId);
}
