package loanclient.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import jmsmessenger.gateways.AsyncSenderGateway;
import jmsmessenger.gateways.IRequest;
import jmsmessenger.gateways.IResponse;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;
import jmsmessenger.serializers.GsonSerializer;

import java.net.URL;
import java.util.*;

import static jmsmessenger.Constants.LOAN_CLIENT_REQUEST_QUEUE;
import static jmsmessenger.Constants.LOAN_CLIENT_RESPONSE_QUEUE;

public class LoanClientController implements Initializable {

    private static String CLIENT_ID;

    private AsyncSenderGateway asyncGateway;

    // UI fields
    public TextField tfSsn;
    public TextField tfAmount;
    public TextField tfTime;
    public ListView<ListViewLine> lvLoanRequestReply;

    public LoanClientController() {
    }

    public void initGateway(String name){
        CLIENT_ID = name;

        asyncGateway = new AsyncSenderGateway(new GsonSerializer(LoanRequest.class, LoanReply.class),  LOAN_CLIENT_RESPONSE_QUEUE + CLIENT_ID, LOAN_CLIENT_REQUEST_QUEUE) {
            @Override
            public void onMessageArrived(IRequest request, IResponse response, Integer aggregationId) {
                ListViewLine lvl = getRequestReply((LoanRequest) request);
                lvl.setLoanReply((LoanReply) response);
                lvLoanRequestReply.refresh();
            }
        };
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
        asyncGateway.sendRequest(loanRequest, null, null);
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
