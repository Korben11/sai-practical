package jmsmessenger.serializers;

import com.google.gson.Gson;
import jmsmessenger.gateways.IRequest;
import jmsmessenger.gateways.IResponse;

public class GsonSerializer extends Serializer {
    private static Gson gson = new Gson();

    public GsonSerializer(Class<? extends IRequest> requestClass, Class<? extends IResponse> responseClass) {
        super(requestClass, responseClass);
    }

    @Override
    public String serializeRequest(IRequest request) {
        return gson.toJson(requestClass.cast(request));
    }

    @Override
    public String serializeResponse(IResponse response) {
        return gson.toJson(responseClass.cast(response));
    }

    @Override
    public IRequest deserializeRequest(String request) {
        return gson.fromJson(request, requestClass);
    }

    @Override
    public IResponse deserializeResponse(String response) {
        return gson.fromJson(response, responseClass);
    }
}
