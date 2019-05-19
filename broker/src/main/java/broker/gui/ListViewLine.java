package broker.gui;

import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;

import java.text.MessageFormat;

public class ListViewLine {

    private LoanRequest loanRequest;
    private LoanReply loanReply;
    private BankInterestRequest interestRequest;
    private BankInterestReply interestReply;

    public LoanRequest getLoanRequest() {
        return loanRequest;
    }

    public LoanReply getLoanReply() {
        return loanReply;
    }

    public void setLoanReply(LoanReply loanReply) {
        this.loanReply = loanReply;
    }


    public void setBankReply(BankInterestReply interestReply) {
        this.interestReply = interestReply;
    }

    public ListViewLine(LoanRequest loanRequest, BankInterestRequest interestRequest) {
        this.loanRequest = loanRequest;
        this.loanReply = null;
        this.interestRequest = interestRequest;
        this.interestReply = null;
    }

    @Override
    public String toString() {

        String reply = "waiting...";

        if (loanReply != null) {
            reply = loanReply.toString();
        }
        return MessageFormat.format("{0} ---> {1}", loanRequest.toString(), reply);

    }
}
