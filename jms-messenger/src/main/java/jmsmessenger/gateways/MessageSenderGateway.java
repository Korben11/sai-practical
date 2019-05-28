package jmsmessenger.gateways;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.naming.Context;
import javax.naming.NamingException;

public class MessageSenderGateway extends MessageGateway implements IProducer {

    private MessageProducer producer;

    public MessageSenderGateway(String queue) {
        super(queue);
    }

    public MessageSenderGateway() {
        super();
    }

    public Message createMessage(String body) throws JMSException {
        return session.createTextMessage(body);
    }

    @Override
    public void setUp(Context jndiContext) throws NamingException, JMSException {
        if (queue == null) {
            producer = session.createProducer(null);
        } else {
            super.destination = (Destination) jndiContext.lookup(queue);
            producer = session.createProducer(destination);
        }
    }

    @Override
    public void send(Message message) throws JMSException {
        producer.send(message);
    }

    @Override
    public void send(Message message, String queue) throws JMSException {
        producer.send(session.createQueue(queue), message);
    }

    @Override
    public void send(Message message, Destination queue) throws JMSException {
        System.out.println("Send to: " + queue.toString());
        producer.send(queue, message);
    }
}
