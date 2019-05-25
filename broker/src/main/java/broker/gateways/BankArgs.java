package broker.gateways;

import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;

public class BankArgs {

    public BankInterestRequest interestRequest;
    public BankInterestReply interestReply;
    public Integer aggregationId;

    public BankArgs(BankInterestRequest interestRequest, BankInterestReply interestReply, Integer aggregationId) {
        this.interestRequest = interestRequest;
        this.interestReply = interestReply;
        this.aggregationId = aggregationId;
    }
}
