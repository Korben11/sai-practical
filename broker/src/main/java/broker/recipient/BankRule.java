package broker.recipient;

import jmsmessenger.Constants;

public class BankRule {
    private Constants.BANK bank;
    private String rule;

    public BankRule(Constants.BANK bank, String rule) {
        this.bank = bank;
        this.rule = rule;
    }

    public Constants.BANK getBank() {
        return bank;
    }

    public String getRule() {
        return rule;
    }
}
