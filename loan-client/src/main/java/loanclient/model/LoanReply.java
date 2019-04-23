package loanclient.model;

/**
 *
 * This class stores all information about a bank offer
 * as a response to a client loan request.
 */
public class LoanReply {

        private double interest; // the interest that the bank offers for the requested loan
        private String bankID; // the unique quote identification of the bank which makes the offer

    public LoanReply() {
        super();
        this.interest = 0;
        this.bankID = "";
    }
    public LoanReply(double interest, String bankId) {
        super();
        this.interest = interest;
        this.bankID = bankId;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getBankID() {
        return bankID;
    }

    public void setBankID(String bankID) {
        this.bankID = bankID;
    }

    @Override
    public String toString() {
        return "LoanReply{" +
                "interest=" + interest +
                ", bankID='" + bankID + '\'' +
                '}';
    }
}
