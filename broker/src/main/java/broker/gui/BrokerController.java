package broker.gui;

import broker.enrichers.CreditHistoryEnricher;
import broker.gateways.*;
import broker.routers.ArchiveRouter;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jmsmessenger.gateways.AsyncReceiverGateway;
import jmsmessenger.gateways.IRequest;
import jmsmessenger.gateways.IResponse;
import jmsmessenger.models.*;
import jmsmessenger.serializers.GsonSerializer;

import java.net.URL;
import java.util.*;

import static jmsmessenger.Constants.*;

public class BrokerController implements Initializable {

    public ListView<ListViewLine> lvBroker;

    // Map to store bank request messageId (correlationId for reply from bank)
    public Map<IRequest, ListViewLine> map;

    // Gateways
    private AsyncReceiverGateway clientGateway;
    private ScatterGetter scatterGetter;

    private ArchiveRouter archiveRouter;
    private CreditHistoryEnricher creditHistoryEnricher;

    public BrokerController() {

        // init map
        map = new HashMap<>();

        creditHistoryEnricher = new CreditHistoryEnricher(HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY);
        archiveRouter = new ArchiveRouter(HTTP_LOCALHOST_8080_ARCHIVE_REST_ACCEPTED);

        clientGateway = new AsyncReceiverGateway(new GsonSerializer(LoanRequest.class, LoanReply.class), LOAN_CLIENT_REQUEST_QUEUE, null) {
            @Override
            public void onMessageArrived(IRequest request, IResponse response, Integer aggregationId) {
                LoanRequest loanRequest = (LoanRequest) request;
                BankInterestRequest interestRequest = new BankInterestRequest(loanRequest.getAmount(), loanRequest.getTime());

                // content enricher
                CreditHistory creditHistory = creditHistoryEnricher.getCreditHistory(loanRequest.getSsn());
                if (creditHistory != null) {
                    interestRequest.setHistory(creditHistory.getHistory());
                    interestRequest.setCreditScore(creditHistory.getCredit());
                }

                ListViewLine listViewLine = new ListViewLine(loanRequest, interestRequest);

                lvBroker.getItems().add(listViewLine);
                map.put(interestRequest, listViewLine);

                int sendRequests = scatterGetter.applyForLoan(interestRequest);
                if (sendRequests > 0) {
                    lvBroker.refresh();
                    return;
                }
                
                listViewLine.setLoanReply(new LoanReply(true));
                listViewLine.setBankReply(new BankInterestReply());
                clientSend(listViewLine);
                lvBroker.refresh();
            }
        };

        scatterGetter = new ScatterGetter() {
            @Override
            public void onBankInterestSelected(IRequest request, IResponse response) {
                ListViewLine listViewLine = map.get(request);
                listViewLine.setBankReply((BankInterestReply) response);
                BankInterestReply interestReply = (BankInterestReply) response;
                listViewLine.setLoanReply(new LoanReply(interestReply.getInterest(), interestReply.getBankId()));
                lvBroker.refresh();
                clientSend(listViewLine);
            }
        };

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void clientSend(ListViewLine listViewLine) {
        LoanRequest loanRequest = listViewLine.getLoanRequest();
        LoanReply loanReply = listViewLine.getLoanReply();
        LoanArchive loanArchive = new LoanArchive(loanRequest.getSsn(), loanRequest.getAmount(), loanReply.getBankId(), loanReply.getInterest());
        archiveRouter.archive(loanArchive);
        clientGateway.sendReply(listViewLine.getLoanRequest(), listViewLine.getLoanReply());
    }
}
