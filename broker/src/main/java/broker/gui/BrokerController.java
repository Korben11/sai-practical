package broker.gui;

import broker.enrichers.CreditHistoryEnricher;
import broker.gateways.*;
import broker.routers.ArchiveRouter;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jmsmessenger.gateways.AsyncReceiverGateway;
import jmsmessenger.gateways.IRequest;
import jmsmessenger.gateways.IResponse;
import jmsmessenger.gateways.IRouter;
import jmsmessenger.models.*;
import jmsmessenger.serializers.GsonSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import java.net.URL;
import java.util.*;

import static jmsmessenger.Constants.*;

public class BrokerController implements Initializable {

    public ListView<ListViewLine> lvBroker;

    // Map to store bank request messageId (correlationId for reply from bank)
    public Map<IRequest, ListViewLine> map;

    // Gateways
//    private ClientGateway clientGateway;
    private AsyncReceiverGateway clientGateway;
    private ScatterGetter scatterGetter;

    private CreditHistoryEnricher creditHistoryEnricher;

    public BrokerController() {

        // init map
        map = new HashMap<>();

        creditHistoryEnricher = new CreditHistoryEnricher(HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY);

        clientGateway = new AsyncReceiverGateway(new GsonSerializer(LoanRequest.class, LoanReply.class), LOAN_CLIENT_REQUEST_QUEUE, null, new ArchiveRouter(HTTP_LOCALHOST_8080_ARCHIVE_REST_ACCEPTED)) {
            @Override
            public void setAggregationId(Message message, Message requestMessage) throws JMSException {}

            @Override
            public void contentBasedRouters(IRequest request, IResponse response, IRouter router) {
                LoanRequest loanRequest = (LoanRequest) request;
                LoanReply loanReply = (LoanReply) response;
                LoanArchive loanArchive = new LoanArchive(loanRequest.getSsn(), loanRequest.getAmount(), loanReply.getBankId(), loanReply.getInterest());
                ((ArchiveRouter) router).archive(loanArchive);
            }

            @Override
            public void onMessageArrived(IRequest request, IResponse response, Message message) {
                bankSend((LoanRequest) request);
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

    private void bankSend(LoanRequest loanRequest) {

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
        lvBroker.refresh();

        int sendRequests = scatterGetter.applyForLoan(interestRequest);
        System.out.println(sendRequests);
        if (sendRequests > 0)
            return;

        listViewLine.setLoanReply(new LoanReply(true));
        listViewLine.setBankReply(new BankInterestReply());
        clientSend(listViewLine);
        lvBroker.refresh();
    }

    private void clientSend(ListViewLine listViewLine) {
        try {
            clientGateway.sendReply(listViewLine.getLoanRequest(), listViewLine.getLoanReply());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
