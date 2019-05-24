package jmsmessenger.models;

public class LoanArchive {
    private int SSN;
    private int amount;
    private String bank;
    private Double interest;

    public LoanArchive(int ssn, int amount, String bank, Double interest) {
        this.SSN = ssn;
        this.amount = amount;
        this.bank = bank;
        this.interest = interest;
    }

    public int getSSN() {
        return SSN;
    }

    public int getAmount() {
        return amount;
    }

    public String getBank() {
        return bank;
    }

    public Double getInterest() {
        return interest;
    }
}
