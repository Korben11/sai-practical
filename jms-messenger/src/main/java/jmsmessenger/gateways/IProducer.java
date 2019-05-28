package jmsmessenger.gateways;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

public interface IProducer {
    public void send(Message message) throws JMSException;
    public void send(Message message, String queue) throws JMSException;
    public void send(Message message, Destination queue) throws JMSException;
}
