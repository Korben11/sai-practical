package jmsmessenger.models;

import jmsmessenger.gateways.IResponse;

/**
 *
 * This class stores all information about a bank offer
 * as a response to a client loan request.
 */
public class LoanReply implements IResponse {

    private double interest; // the interest that the bank offers for the requested loan
    private String bankId; // the unique quote identification of the bank which makes the offer
    private boolean rejected;


    public boolean isRejected() {
        return rejected;
    }

    public LoanReply() {
        super();
        this.interest = 0;
        this.bankId = "";
        this.rejected = false;
    }

    public LoanReply(boolean rejected) {
        super();
        this.interest = 0;
        this.bankId = "";
        this.rejected = rejected;
    }

    public LoanReply(double interest, String bankId) {
        super();
        this.interest = interest;
        this.bankId = bankId;
        this.rejected = false;
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
