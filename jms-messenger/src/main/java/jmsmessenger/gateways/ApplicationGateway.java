package jmsmessenger.gateways;

import jmsmessenger.serializers.Serializer;


public abstract class ApplicationGateway {
    protected MessageReceiverGateway messageReceiverGateway;
    protected MessageSenderGateway messageSenderGateway;
    protected Serializer serializer;

    public ApplicationGateway(Serializer serializer, String consumerQueue, String producerQueue) {
        this.serializer = serializer;
        System.out.println("consumerQueue: " + consumerQueue);
        System.out.println("producerQueue: " + producerQueue);

        messageReceiverGateway = new MessageReceiverGateway(consumerQueue);
        messageSenderGateway = new MessageSenderGateway(producerQueue);
    }

    public abstract void onMessageArrived(IRequest request, IResponse response, Integer aggregationId);
}
