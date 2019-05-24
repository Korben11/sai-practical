package jmsmessenger.serializers;

import com.google.gson.Gson;
import jmsmessenger.models.CreditHistory;

public class CreditHistorySerializer {
    private Gson gson;

    public CreditHistorySerializer() {
        this.gson = new Gson();
    }

    public CreditHistory deserializeCreditHistory(String json) {
        return gson.fromJson(json, CreditHistory.class);
    }
}
