package jmsmessenger.models;

import jmsmessenger.gateways.IRequest;

/**
 *
 * This class stores all information about an request from a bank to offer
 * a loan to a specific client.
 */
public class BankInterestRequest implements IRequest {

    private int amount; // the ammount to borrow
    private int time; // the time-span of the loan in years
    private int creditScore;
    private int history;

    public BankInterestRequest() {
        super();
        this.amount = 0;
        this.time = 0;
        this.creditScore = 0;
        this.history = 0;
    }

    public BankInterestRequest(int amount, int time) {
        super();
        this.amount = amount;
        this.time = time;
        this.creditScore = 0;
        this.history = 0;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public int getHistory() {
        return history;
    }

    public void setHistory(int history) {
        this.history = history;
    }

    @Override
    public String toString() {
        return " amount=" + amount + " time=" + time + " creditScore=" + creditScore + " history=" + history;
    }
}
