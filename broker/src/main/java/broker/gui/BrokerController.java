package broker.gui;

import broker.enrichers.CreditHistoryEnricher;
import broker.gateways.*;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jmsmessenger.models.*;

import javax.jms.JMSException;
import java.net.URL;
import java.util.*;

import static jmsmessenger.Constants.HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY;

public class BrokerController implements Initializable {

    public ListView<ListViewLine> lvBroker;

    // Map to store bank request messageId (correlationId for reply from bank)
    public Map<BankInterestRequest, ListViewLine> map;

    // Gateways
    private ClientGateway clientGateway;
    private ScatterGetter scatterGetter;

    private CreditHistoryEnricher creditHistoryEnricher;

    public BrokerController() {

        // init map
        map = new HashMap<>();

        creditHistoryEnricher = new CreditHistoryEnricher(HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY);

        // create and subscribe gateways
        clientGateway = new ClientGateway() {
            @Override
            public void onResponse(LoanRequest loanRequest) {
                bankSend(loanRequest);
            }
        };

        scatterGetter = new ScatterGetter() {
            @Override
            public void onBankInterestSelected(BankInterestRequest interestRequest, BankInterestReply interestReply) {
                ListViewLine listViewLine = map.get(interestRequest);
                listViewLine.setBankReply(interestReply);
                listViewLine.setLoanReply(new LoanReply(interestReply.getInterest(), interestReply.getBankId()));
                lvBroker.refresh();
                clientSend(listViewLine);
            }
        };

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void bankSend(LoanRequest loanRequest) {

        BankInterestRequest interestRequest = new BankInterestRequest(loanRequest.getAmount(),
                loanRequest.getTime());

        // content enricher
        CreditHistory creditHistory = creditHistoryEnricher.getCreditHistory(loanRequest.getSsn());
        if (creditHistory != null) {
            interestRequest.setHistory(creditHistory.getHistory());
            interestRequest.setCreditScore(creditHistory.getCredit());
        }

        ListViewLine listViewLine = new ListViewLine(loanRequest, interestRequest);
        lvBroker.getItems().add(listViewLine);
        map.put(interestRequest, listViewLine);
        lvBroker.refresh();

        scatterGetter.applyForLoan(interestRequest);
    }

    private void clientSend(ListViewLine listViewLine) {
        try {
            clientGateway.sendReply(listViewLine.getLoanReply(), listViewLine.getLoanRequest());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
