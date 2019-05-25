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
import java.util.ResourceBundle;

import static jmsmessenger.Constants.BANK_CLIENT_REQUEST_QUEUE;
import static jmsmessenger.Constants.BANK_CLIENT_RESPONSE_QUEUE;

public class BankController implements Initializable {

    private BankGateway gateway;

    private static String BANK_ID;

    // UI fields
    public ListView<ListViewLine> lvBankRequestReply;
    public TextField tfInterest;

    public BankController() { }

    public void initGateway(String bank) {
        BANK_ID = bank;
        gateway = new BankGateway(BANK_ID + BANK_CLIENT_REQUEST_QUEUE, BANK_CLIENT_RESPONSE_QUEUE) {
            @Override
            public void onResponse(BankInterestRequest interestRequest) {
                ListViewLine listViewLine = new ListViewLine(interestRequest);
                lvBankRequestReply.getItems().add(listViewLine);
            }
        };
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
}
