package loanclient.gui;

import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;

import java.text.MessageFormat;

/**
 * This class is an item/line for a ListView. It makes it possible to put both BankInterestRequest and BankInterestReply object in one item in a ListView.
 */
class ListViewLine {

    private LoanRequest loanRequest;
    private LoanReply loanReply;

    public ListViewLine(LoanRequest loanRequest) {
        setLoanRequest(loanRequest);
        setLoanReply(null);
    }

    public LoanRequest getLoanRequest() {
        return loanRequest;
    }

    private void setLoanRequest(LoanRequest loanRequest) {
        this.loanRequest = loanRequest;
    }

    public LoanReply getLoanReply() {
        return loanReply;
    }

    public void setLoanReply(LoanReply loanReply) {
        this.loanReply = loanReply;
    }

    /**
     * This method defines how one line is shown in the ListView.
     *
     * @return a) if BankInterestReply is null, then this item will be shown as "loanRequest.toString ---> waiting for loan reply..."
     * b) if BankInterestReply is not null, then this item will be shown as "loanRequest.toString ---> loanReply.toString"
     */
    @Override
    public String toString() {
        String format = "{0} ---> {1}";

        if (loanReply != null) {
            if (loanReply.isRejected())
                return MessageFormat.format(format, loanRequest.toString(), "Rejected");
        }

        return MessageFormat.format(format, loanRequest.toString(), ((loanReply != null) ? loanReply.toString() : "waiting for loan reply..."));
    }

}
