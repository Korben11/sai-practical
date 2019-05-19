package jmsmessenger.models;

/**
 *
 * This class stores all information about a bank offer
 * as a response to a client loan request.
 */
public class LoanReply {

        private double interest; // the interest that the bank offers for the requested loan
        private String bankId; // the unique quote identification of the bank which makes the offer

    public LoanReply() {
        super();
        this.interest = 0;
        this.bankId = "";
    }
    public LoanReply(double interest, String bankId) {
        super();
        this.interest = interest;
        this.bankId = bankId;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    @Override
    public String toString() {
        return "LoanReply{" +
                "interest=" + interest +
                ", bankId='" + bankId + '\'' +
                '}';
    }
}
