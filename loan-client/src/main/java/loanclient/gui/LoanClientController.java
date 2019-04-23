package loanclient.gui;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import loanclient.model.LoanReply;
import loanclient.model.LoanRequest;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoanClientController implements Initializable {

    // Serializer
    private Gson gson;

    // Constants
    public static final String LOAN_CLIENT_REQUEST_QUEUE = "LoanClientRequestQueue";
    public static final String LOAN_CLIENT_RESPONSE_QUEUE = "LoanClientResponseQueue";

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
    public TextField tfSsn;
    public TextField tfAmount;
    public TextField tfTime;
    public ListView<ListViewLine> lvLoanRequestReply;

    public LoanClientController() {
        gson = new Gson();

        props = new Properties();

        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        props.put(("queue." + LOAN_CLIENT_REQUEST_QUEUE), LOAN_CLIENT_REQUEST_QUEUE);
        props.put(("queue." + LOAN_CLIENT_RESPONSE_QUEUE), LOAN_CLIENT_RESPONSE_QUEUE);

        try {
            jndiContext = new InitialContext(props);
            connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            receiveDestination = (Destination) jndiContext.lookup(LOAN_CLIENT_RESPONSE_QUEUE);
            sendDestination = (Destination) jndiContext.lookup(LOAN_CLIENT_REQUEST_QUEUE);

            messageConsumer = session.createConsumer(receiveDestination);
            messageProducer = session.createProducer(sendDestination);

            // listen to messages
            messageConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        LoanReply questionReply = gson.fromJson(textMessage.getText(), LoanReply.class);
                        System.out.println("New message received: " + questionReply);
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
    public void btnSendLoanRequestClicked() {
        // create the BankInterestRequest
        int ssn = Integer.parseInt(tfSsn.getText());
        int amount = Integer.parseInt(tfAmount.getText());
        int time = Integer.parseInt(tfTime.getText());
        LoanRequest loanRequest = new LoanRequest(ssn, amount, time);

        // create the ListView line with the request and add it to lvLoanRequestReply
        ListViewLine listViewLine = new ListViewLine(loanRequest);
        lvLoanRequestReply.getItems().add(listViewLine);

        Message message = null;
        try {
            message = session.createTextMessage(gson.toJson(loanRequest));
            messageProducer.send(message);
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
    private ListViewLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < lvLoanRequestReply.getItems().size(); i++) {
            ListViewLine rr = lvLoanRequestReply.getItems().get(i);
            if (rr.getLoanRequest() != null && rr.getLoanRequest() == request) {
                return rr;
            }
        }

        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tfSsn.setText("123456");
        tfAmount.setText("80000");
        tfTime.setText("30");
    }
}
