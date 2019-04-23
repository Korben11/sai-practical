package broker.gui;

import com.google.gson.Gson;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class BrokerController implements Initializable {

    public ListView<ListViewLine> lvBroker;

    // Map to store bank request messageId (correlationId for reply from bank)
    public Map<String, ListViewLine> lvlToMessageId;

    // Serializer
    private Gson gson;

    // Constants
    public static final String LOAN_CLIENT_REQUEST_QUEUE = "LoanClientRequestQueue";
    public static final String LOAN_CLIENT_RESPONSE_QUEUE = "LoanClientResponseQueue";
    public static final String BANK_CLIENT_REQUEST_QUEUE = "BankClientRequestQueue";
    public static final String BANK_CLIENT_RESPONSE_QUEUE = "BankClientResponseQueue";

    // JMS
    private Connection connection;
    private ConnectionFactory connectionFactory;
    private Session session;

    private Destination loanReceiveDestination;;
    private MessageConsumer loanMessageConsumer;

    private Destination bankReceiveDestination;;
    private MessageConsumer bankMessageConsumer;

    private Destination loanSendDestination;
    private MessageProducer loanMessageProducer;

    private Destination bankSendDestination;
    private MessageProducer bankMessageProducer;

    private Properties props;
    private Context jndiContext;

    public BrokerController() {

        // init map
        lvlToMessageId = new HashMap<>();

        gson = new Gson();

        props = new Properties();

        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        props.put(("queue." + LOAN_CLIENT_REQUEST_QUEUE), LOAN_CLIENT_REQUEST_QUEUE);
        props.put(("queue." + LOAN_CLIENT_RESPONSE_QUEUE), LOAN_CLIENT_RESPONSE_QUEUE);
        props.put(("queue." + BANK_CLIENT_REQUEST_QUEUE), BANK_CLIENT_REQUEST_QUEUE);
        props.put(("queue." + BANK_CLIENT_RESPONSE_QUEUE), BANK_CLIENT_RESPONSE_QUEUE);

        try {
            jndiContext = new InitialContext(props);
            connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            loanReceiveDestination = (Destination) jndiContext.lookup(LOAN_CLIENT_REQUEST_QUEUE);
            loanSendDestination = (Destination) jndiContext.lookup(LOAN_CLIENT_RESPONSE_QUEUE);

            bankReceiveDestination = (Destination) jndiContext.lookup(BANK_CLIENT_RESPONSE_QUEUE);
            bankSendDestination = (Destination) jndiContext.lookup(BANK_CLIENT_REQUEST_QUEUE);

            loanMessageConsumer = session.createConsumer(loanReceiveDestination);
            loanMessageProducer = session.createProducer(loanSendDestination);

            bankMessageConsumer = session.createConsumer(bankReceiveDestination);
            bankMessageProducer = session.createProducer(bankSendDestination);

            // listen to messages
            loanMessageConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("loanMessageConsumer received: " + textMessage.getText());

                        Message bankRequestMessage = null;
                        try {
                            bankRequestMessage = session.createTextMessage(textMessage.getText());
                            bankMessageProducer.send(bankRequestMessage);

                            ListViewLine listViewLine = new ListViewLine(message, bankRequestMessage);
                            lvBroker.getItems().add(listViewLine);

                            // map requesting messages by bankRequestMessageId
                            lvlToMessageId.put(bankRequestMessage.getJMSCorrelationID(), listViewLine);

                            // refresh UI
                            lvBroker.refresh();

                        } catch (JMSException e) {
                            e.printStackTrace();
                        }

                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });

            bankMessageConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("bankMessageConsumer received: " + textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });

            connection.start();
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
