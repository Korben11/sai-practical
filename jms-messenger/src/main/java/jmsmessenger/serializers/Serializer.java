package jmsmessenger.serializers;

import jmsmessenger.gateways.IRequest;
import jmsmessenger.gateways.IResponse;

public abstract class Serializer {
    protected Class<? extends IRequest> requestClass;
    protected Class<? extends IResponse> responseClass;

    public Serializer(Class<? extends IRequest> requestClass, Class<? extends IResponse> responseClass) {
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    public abstract String serializeRequest(IRequest request);
    public abstract String serializeResponse(IResponse response);

    public abstract IRequest deserializeRequest(String request);
    public abstract IResponse deserializeResponse(String response);
}
