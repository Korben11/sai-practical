package bank.gui;

import bank.gateway.BankGateway;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javax.jms.*;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

public class BankController implements Initializable, Observer {

    private BankGateway gateway;

    private final String BANK_ID = "ABN";

    // UI fields
    public ListView<ListViewLine> lvBankRequestReply;
    public TextField tfInterest;

    public BankController() {
        gateway = new BankGateway();
        gateway.addObserver(this);
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

        try {
            gateway.sendInterestReply(listViewLine.getBankInterestRequest(), bankInterestReply);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        lvBankRequestReply.refresh();
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
        tfInterest.setText("10");
    }

    @Override
    public void update(Observable o, Object arg) {
        BankInterestRequest request = (BankInterestRequest) arg;
        ListViewLine listViewLine = new ListViewLine(request);
        lvBankRequestReply.getItems().add(listViewLine);
    }
}
