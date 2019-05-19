package broker.gateways;

import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;

public class BankArgs {

    public BankInterestRequest interestRequest;
    public BankInterestReply interestReply;

    public BankArgs(BankInterestRequest interestRequest, BankInterestReply interestReply) {
        this.interestRequest = interestRequest;
        this.interestReply = interestReply;
    }
}
