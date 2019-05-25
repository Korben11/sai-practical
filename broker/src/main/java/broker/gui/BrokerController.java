package broker.gui;

import broker.enrichers.CreditHistoryEnricher;
import broker.gateways.Aggregator;
import broker.gateways.BankArgs;
import broker.gateways.BankGateway;
import broker.gateways.ClientGateway;
import broker.recipient.BankList;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jmsmessenger.models.*;

import javax.jms.JMSException;
import java.net.URL;
import java.util.*;

import static jmsmessenger.Constants.HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY;

public class BrokerController implements Initializable, Observer {

    public ListView<ListViewLine> lvBroker;

    // Map to store bank request messageId (correlationId for reply from bank)
    public Map<BankInterestRequest, ListViewLine> map;
    // Map aggregationId to interest request
    private Map<Integer, BankInterestRequest> mapBankInterestRequest;

    // Gateways
    private ClientGateway clientGateway;
    private BankGateway bankGateway;
    private CreditHistoryEnricher creditHistoryEnricher;

    // recipient list
    private BankList bankList;

    // Aggregator
    private static int aggregationId = 0;
    private Aggregator aggregator;

    public BrokerController() {

        // init map
        map = new HashMap<>();
        mapBankInterestRequest = new HashMap<>();

        creditHistoryEnricher = new CreditHistoryEnricher(HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY);

        // create and subscribe gateways
        clientGateway = new ClientGateway();
        clientGateway.addObserver(this);

        bankGateway = new BankGateway();
        bankGateway.addObserver(this);

        // init recipient list
        bankList = new BankList(bankGateway);

        aggregator = new Aggregator() {
            @Override
            public void onAllRepliesReceived(BankInterestReply interestReply, Integer aggregationId) {
                BankInterestRequest interestRequest = mapBankInterestRequest.get(aggregationId);
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

    @Override
    public void update(Observable o, Object arg) {
        if (o == bankGateway) {
            BankArgs args = (BankArgs) arg;
            aggregator.addBankInterestReply(args.interestReply, args.aggregationId);
        } else {
            LoanRequest loanRequest = (LoanRequest) arg;
            bankSend(loanRequest);
        }
    }

    private void bankSend(LoanRequest loanRequest) {
        aggregationId++;

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
        mapBankInterestRequest.put(aggregationId, interestRequest);
        lvBroker.refresh();

        int passed = bankList.sendRequest(interestRequest, aggregationId);

        if (passed == 0) {
            // TODO: reject directly
            System.out.println("Rejected directly");
            return;
        }
        aggregator.addAggregator(aggregationId, passed);
    }

    private void clientSend(ListViewLine listViewLine) {
        try {
            clientGateway.sendReply(listViewLine.getLoanReply(), listViewLine.getLoanRequest());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
