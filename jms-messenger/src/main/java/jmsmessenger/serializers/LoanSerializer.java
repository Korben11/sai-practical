package jmsmessenger.serializers;

import com.google.gson.Gson;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;

public class LoanSerializer {
    private Gson gson;

    public LoanSerializer() {
        this.gson = new Gson();
    }

    public String serializeLoanRequest(LoanRequest loanRequest) {
        return gson.toJson(loanRequest);
    }

    public String serializeLoanReply(LoanReply loanReply) {
        return gson.toJson(loanReply);
    }

    public LoanRequest deserializeLoanRequest(String loanRequest) {
        return gson.fromJson(loanRequest, LoanRequest.class);
    }

    public LoanReply deserializeLoanReply(String loanReply) {
        return gson.fromJson(loanReply, LoanReply.class);
    }
}
