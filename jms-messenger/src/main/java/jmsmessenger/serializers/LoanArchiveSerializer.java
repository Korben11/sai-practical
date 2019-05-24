package jmsmessenger.serializers;

import com.google.gson.Gson;
import jmsmessenger.models.LoanArchive;

public class LoanArchiveSerializer {
    private Gson gson;

    public LoanArchiveSerializer() {
        this.gson = new Gson();
    }

    public String serializeLoanArchive(LoanArchive loanArchive) {
        return gson.toJson(loanArchive);
    }
}
