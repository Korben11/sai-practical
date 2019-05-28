package jmsmessenger.gateways;

import jmsmessenger.serializers.Serializer;

import javax.jms.Message;

public abstract class ApplicationGateway {
    protected Consumer consumer;
    protected Producer producer;
    protected Serializer serializer;

    public ApplicationGateway(Serializer serializer, String consumerQueue, String producerQueue) {
        this.serializer = serializer;
        System.out.println("consumerQueue: " + consumerQueue);
        System.out.println("producerQueue: " + producerQueue);

        consumer = new Consumer(consumerQueue);
        producer = new Producer(producerQueue);
    }

    public abstract void onMessageArrived(IRequest request, IResponse response, Message message);
}
