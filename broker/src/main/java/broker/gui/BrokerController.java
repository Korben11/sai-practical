package broker.gui;

import broker.gateways.BankArgs;
import broker.gateways.BankGateway;
import broker.gateways.ClientGateway;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jmsmessenger.models.BankInterestRequest;
import jmsmessenger.models.LoanReply;
import jmsmessenger.models.LoanRequest;

import javax.jms.JMSException;
import java.net.URL;
import java.util.*;

public class BrokerController implements Initializable, Observer {

    public ListView<ListViewLine> lvBroker;

    // Map to store bank request messageId (correlationId for reply from bank)
    public Map<BankInterestRequest, ListViewLine> map;

    // Gateways
    private ClientGateway clientGateway;
    private BankGateway bankGateway;

    public BrokerController() {

        // init map
        map = new HashMap<>();

        // create and subscribe gateways
        clientGateway = new ClientGateway();
        clientGateway.addObserver(this);

        bankGateway = new BankGateway();
        bankGateway.addObserver(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == bankGateway) {
//            System.out.println("bankGateway");
            BankArgs args = (BankArgs) arg;
            ListViewLine listViewLine = map.get(args.interestRequest);
            listViewLine.setBankReply(args.interestReply);
            listViewLine.setLoanReply(new LoanReply(args.interestReply.getInterest(), "ABN"));
            lvBroker.refresh();
            clientSend(listViewLine);

        } else {
//            System.out.println("clientGateway");
            LoanRequest loanRequest = (LoanRequest) arg;
            bankSend(loanRequest);
        }
    }

    private void bankSend(LoanRequest loanRequest) {
        BankInterestRequest interestRequest = new BankInterestRequest(loanRequest.getAmount(),
                loanRequest.getTime());
        ListViewLine listViewLine = new ListViewLine(loanRequest, interestRequest);
        lvBroker.getItems().add(listViewLine);
        map.put(interestRequest, listViewLine);
        lvBroker.refresh();

        try {
            bankGateway.sendRequest(interestRequest, loanRequest.getSsn());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void clientSend(ListViewLine listViewLine) {
        try {
            clientGateway.sendReply(listViewLine.getLoanReply(), listViewLine.getLoanRequest());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
