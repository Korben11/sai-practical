package jmsmessenger.models;

/**
 * This class stores information about the bank reply
 *  to a loan request of the specific client
 * 
 */
public class BankInterestReply {

    private double interest; // the interest that the bank offers for the requested loan
    private String bankId; // the unique quote identification of the bank which makes the offer
    
    public BankInterestReply() {
        this.interest = 0;
        this.bankId = "";
    }
    
    public BankInterestReply(double interest, String bankId) {
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
        return "BankInterestReply{" +
                "interest=" + interest +
                ", bankId='" + bankId + '\'' +
                '}';
    }
}
