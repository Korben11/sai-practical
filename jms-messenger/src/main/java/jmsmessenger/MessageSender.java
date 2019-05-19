package jmsmessenger;

import jmsmessenger.Constants;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageSender {
    private String queue;

    // jms connection
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;

    public MessageSender(String queue) {
        this.queue = queue;

        // set properties
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                Constants.ORG_APACHE_ACTIVEMQ_JNDI_ACTIVE_MQINITIAL_CONTEXT_FACTORY);
        properties.setProperty(Context.PROVIDER_URL, Constants.TCP_LOCALHOST_61616);
        properties.put((Constants.QUEUE + queue), queue);

        // create connection and session
        try {
            Context jndiContext = new InitialContext(properties);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(Constants.CONNECTION_FACTORY);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // create destination and producer
            destination = (Destination) jndiContext.lookup(queue);
            producer = session.createProducer(destination);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Message createMessage(String body) throws JMSException {
        return session.createTextMessage(body);
    }

    public void send(Message message) throws JMSException {
        producer.send(message);
    }
}
