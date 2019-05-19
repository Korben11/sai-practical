package jmsmessenger;

import jmsmessenger.Constants;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageReceiver {

    private String queue;

    // connection objects
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer consumer;

    public MessageReceiver(String queue) {
        this.queue = queue;

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


            // add client destination and consumer
            destination = (Destination) jndiContext.lookup(queue);
            consumer = session.createConsumer(this.destination);

            // connect
            connection.start();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(MessageListener listener) {
        try {
            consumer.setMessageListener(listener);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
