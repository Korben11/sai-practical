package jmsmessenger.models;

import com.google.gson.annotations.Expose;

import javax.jms.Message;

/**
 *
 * This class stores all information about an request from a bank to offer
 * a loan to a specific client.
 */
public class BankInterestRequest {

    private int amount; // the ammount to borrow
    private int time; // the time-span of the loan in years

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Expose(serialize = false, deserialize = false)
    private Message message;

    public BankInterestRequest() {
        super();
        this.amount = 0;
        this.time = 0;
    }

    public BankInterestRequest(int amount, int time) {
        super();
        this.amount = amount;
        this.time = time;
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

    @Override
    public String toString() {
        return " amount=" + amount + " time=" + time;
    }
}
