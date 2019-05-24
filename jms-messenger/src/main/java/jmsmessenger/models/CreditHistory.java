package jmsmessenger.models;

public class CreditHistory {
    private int creditScore;
    private int history;

    public CreditHistory(int creditScore, int history) {

        this.creditScore = creditScore;
        this.history = history;
    }

    public int getCredit() { return this.creditScore; }

    public int getHistory() { return this.history; }

    @Override
    public String toString() {

        return "credit: " + this.creditScore + ", history: " + this.history;
    }
}
