package loanclient.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import loanclient.gateway.ClientArgs;
import loanclient.gateway.ClientGateway;
import jmsmessenger.models.LoanRequest;

import javax.jms.*;
import java.net.URL;
import java.util.*;

public class LoanClientController implements Initializable, Observer {

    private ClientGateway gateway;

    // UI fields
    public TextField tfSsn;
    public TextField tfAmount;
    public TextField tfTime;
    public ListView<ListViewLine> lvLoanRequestReply;

    public LoanClientController() {
        gateway = new ClientGateway();
        gateway.addObserver(this);
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
        try {
            gateway.sendLoanRequest(loanRequest);
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

    // listen to messages
    @Override
    public void update(Observable o, Object arg) {
        ClientArgs clientArgs = (ClientArgs) arg;
        ListViewLine lvl = getRequestReply(clientArgs.loanRequest);
        lvl.setLoanReply(clientArgs.loanReply);
        lvLoanRequestReply.refresh();
    }
}
