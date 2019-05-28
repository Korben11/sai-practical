package jmsmessenger.gateways;

import jmsmessenger.Constants;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public abstract class MessageGateway {
    protected String queue = null;

    // jms connection objects
    private Connection connection;
    protected Session session;
    protected Destination destination;

    public MessageGateway(String queue) {
        this.queue = queue;
        init();
    }

    public MessageGateway() {
        init();
    }

    private void init() {

        // set properties
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                Constants.ORG_APACHE_ACTIVEMQ_JNDI_ACTIVE_MQINITIAL_CONTEXT_FACTORY);
        properties.setProperty(Context.PROVIDER_URL, Constants.TCP_LOCALHOST_61616);
        if (queue!=null)
            properties.put((Constants.QUEUE + queue), queue);

        // create connection and session
        try {
            Context jndiContext = new InitialContext(properties);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup(Constants.CONNECTION_FACTORY);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // create destination and producer or consumer
            setUp(jndiContext);

            // connect
            connection.start();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public abstract void setUp(Context jndiContext) throws NamingException, JMSException;

}
