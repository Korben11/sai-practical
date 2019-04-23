package bank.gui;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class BankController implements Initializable {

    private final String BANK_ID = "ABN";

    // Serializer
    private Gson gson;

    // Constants
    public static final String BANK_CLIENT_REQUEST_QUEUE = "BankClientRequestQueue";
    public static final String BANK_CLIENT_RESPONSE_QUEUE = "BankClientResponseQueue";

    // JMS
    private Connection connection;
    private ConnectionFactory connectionFactory;
    private Session session;

    private Destination receiveDestination;
    private MessageConsumer messageConsumer;

    private Destination sendDestination;
    private MessageProducer messageProducer;

    private Properties props;
    private Context jndiContext;

    // UI fields
    public ListView<ListViewLine> lvBankRequestReply;
    public TextField tfInterest;

    public BankController() {
        gson = new Gson();

        props = new Properties();

        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        props.put(("queue." + BANK_CLIENT_REQUEST_QUEUE), BANK_CLIENT_REQUEST_QUEUE);
        props.put(("queue." + BANK_CLIENT_RESPONSE_QUEUE), BANK_CLIENT_RESPONSE_QUEUE);

        try {
            jndiContext = new InitialContext(props);
            connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            receiveDestination = (Destination) jndiContext.lookup(BANK_CLIENT_REQUEST_QUEUE);
            sendDestination = (Destination) jndiContext.lookup(BANK_CLIENT_RESPONSE_QUEUE);

            messageConsumer = session.createConsumer(receiveDestination);
            messageProducer = session.createProducer(sendDestination);

            // listen to messages
            messageConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        BankInterestRequest bankInterestRequest = gson.fromJson(textMessage.getText(),
                                BankInterestRequest.class);
                        bankInterestRequest.setMessage(message);
                        ListViewLine listViewLine = new ListViewLine(bankInterestRequest);
                        lvBankRequestReply.getItems().add(listViewLine);
                        lvBankRequestReply.refresh();
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

    @FXML
    public void btnSendBankInterestReplyClicked() {

        if (!lvBankRequestReply.hasProperties()) return;

        ListViewLine listViewLine = lvBankRequestReply.getFocusModel().getFocusedItem();
        if (listViewLine.getBankInterestReply() != null)
            return;

        double interest = Double.parseDouble(tfInterest.getText());
        BankInterestReply bankInterestReply = new BankInterestReply(interest, BANK_ID);
        listViewLine.setBankInterestReply(bankInterestReply);

        sendReply(bankInterestReply, listViewLine.getBankInterestRequest().getMessage());

        lvBankRequestReply.refresh();
    }

    private void sendReply(BankInterestReply bankInterestReply, Message requestMessage) {

        Message msg = null;
        try {
            // create a text message
            msg = session.createTextMessage(gson.toJson(bankInterestReply));

            // set correlation id
            msg.setJMSCorrelationID(requestMessage.getJMSMessageID());

            messageProducer.send(msg);

            System.out.println("sent reply " + msg);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the line of lvMessages which contains the given loan request.
     *
     * @param request BankInterestRequest for which the line of lvMessages should be found and returned
     * @return The ListView line of lvMessages which contains the given request
     */
    private ListViewLine getRequestReply(BankInterestRequest request) {

        for (int i = 0; i < lvBankRequestReply.getItems().size(); i++) {
            ListViewLine rr = lvBankRequestReply.getItems().get(i);
            if (rr.getBankInterestRequest() != null && rr.getBankInterestRequest() == request) {
                return rr;
            }
        }
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
