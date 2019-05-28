package jmsmessenger.gateways;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;

public class MessageReceiverGateway extends MessageGateway implements IConsumer {

    private MessageConsumer consumer;

    public Destination getDestination() {
        return destination;
    }

    public MessageReceiverGateway(String queue) {
        super(queue);
    }

    public MessageReceiverGateway() {
        super();
    }

    @Override
    public void setUp(Context jndiContext) throws NamingException, JMSException {
        destination = (Destination) jndiContext.lookup(queue);
        consumer = session.createConsumer(this.destination);
    }

    @Override
    public void onMessage(MessageListener listener) {
        try {
            consumer.setMessageListener(listener);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
