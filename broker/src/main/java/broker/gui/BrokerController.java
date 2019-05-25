package broker.gui;

import broker.enrichers.CreditHistoryEnricher;
import broker.gateways.BankArgs;
import broker.gateways.BankGateway;
import broker.gateways.ClientGateway;
import broker.recipient.BankList;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.models.CreditHistory;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;

import javax.jms.JMSException;
import java.net.URL;
import java.util.*;

import static jmsmessenger.Constants.HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY;

public class BrokerController implements Initializable, Observer {

    public ListView<ListViewLine> lvBroker;

    // Map to store bank request messageId (correlationId for reply from bank)
    public Map<BankInterestRequest, ListViewLine> map;

    // Gateways
    private ClientGateway clientGateway;
    private BankGateway bankGateway;
    private CreditHistoryEnricher creditHistoryEnricher;

    // recipient list
    private BankList bankList;

    public BrokerController() {

        // init map
        map = new HashMap<>();

        creditHistoryEnricher = new CreditHistoryEnricher(HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY);

        // create and subscribe gateways
        clientGateway = new ClientGateway();
        clientGateway.addObserver(this);

        bankGateway = new BankGateway();
        bankGateway.addObserver(this);

        // init recipient list
        bankList = new BankList(bankGateway);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == bankGateway) {
            BankArgs args = (BankArgs) arg;
            ListViewLine listViewLine = map.get(args.interestRequest);
            listViewLine.setBankReply(args.interestReply);
            listViewLine.setLoanReply(new LoanReply(args.interestReply.getInterest(), args.interestReply.getBankId()));
            lvBroker.refresh();
            clientSend(listViewLine);

        } else {
            LoanRequest loanRequest = (LoanRequest) arg;
            bankSend(loanRequest);
        }
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

        int passed = bankList.sendRequest(interestRequest);
        System.out.println("interest request send was passed: " + passed + " times");
    }

    private void clientSend(ListViewLine listViewLine) {
        try {
            clientGateway.sendReply(listViewLine.getLoanReply(), listViewLine.getLoanRequest());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
