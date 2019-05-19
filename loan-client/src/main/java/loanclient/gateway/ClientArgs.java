package loanclient.gateway;

import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;

public class ClientArgs {
    public LoanReply loanReply;
    public LoanRequest loanRequest;

    public ClientArgs(LoanReply loanReply, LoanRequest loanRequest) {
        this.loanReply = loanReply;
        this.loanRequest = loanRequest;
    }
}
