package bank.gui;

import jmsmessenger.gateways.AsyncReceiverGateway;
import jmsmessenger.gateways.IRequest;
import jmsmessenger.gateways.IResponse;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import jmsmessenger.models.BankInterestReply;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.serializers.GsonSerializer;

import java.net.URL;
import java.util.ResourceBundle;

import static jmsmessenger.Constants.*;

public class BankController implements Initializable {

//    private BankGateway gateway;
    private AsyncReceiverGateway gateway;

    private static String BANK_ID;

    // UI fields
    public ListView<ListViewLine> lvBankRequestReply;
    public TextField tfInterest;

    public BankController() { }

    public void initGateway(String bank) {
        BANK_ID = bank;
        gateway = new AsyncReceiverGateway(new GsonSerializer(BankInterestRequest.class, BankInterestReply.class),BANK_ID + BANK_CLIENT_REQUEST_QUEUE, BANK_CLIENT_RESPONSE_QUEUE) {
            @Override
            public void onMessageArrived(IRequest request, IResponse response, Integer aggregationId) {
                ListViewLine listViewLine = new ListViewLine((BankInterestRequest) request);
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

        gateway.sendReply(listViewLine.getBankInterestRequest(), bankInterestReply);

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
