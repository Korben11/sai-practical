package broker.recipient;

import jmsmessenger.Constants;
import jmsmessenger.gateways.AsyncSenderGateway;
import jmsmessenger.gateways.IRequest;
import jmsmessenger.models.BankInterestRequest;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import javax.jms.JMSException;

import static jmsmessenger.Constants.*;

public class BankRecipientList {

    private BankRule banksRules[] = {
//            new BankRule(Constants.BANK.ING, AMOUNT_GTE_200000_AND_AMOUNT_LTE_300000_AND_TIME_LTE_20),
            new BankRule(Constants.BANK.ING, AMOUNT_LTE_100000_AND_TIME_LTE_10),
            new BankRule(Constants.BANK.ABN, AMOUNT_GTE_200000_AND_AMOUNT_LTE_300000_AND_TIME_LTE_20),
            new BankRule(Constants.BANK.RABO, AMOUNT_LTE_250000_AND_TIME_LTE_15),
    };
    private AsyncSenderGateway bankGateway;
    private Evaluator evaluator;

    public BankRecipientList(AsyncSenderGateway bankGateway) {
        this.bankGateway = bankGateway;
        evaluator = new Evaluator();
    }

    public int sendRequest(BankInterestRequest request, Integer aggregationId) {
        int passed = 0;

        setEvaluator(request.getAmount(), request.getTime());

        try {
            for (BankRule bankRule : banksRules) {
                if (!(evaluator.evaluate(bankRule.getRule()).equals("1.0")))
                    continue;
                passed++;
                bankGateway.sendRequest((IRequest) request, bankRule.getBank() + Constants.BANK_CLIENT_REQUEST_QUEUE, aggregationId);
            }
        } catch (EvaluationException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        return passed;
    }

    private void setEvaluator(int amount, int time) {
        evaluator.clearVariables();
        evaluator.putVariable("amount", Integer.toString(amount));
        evaluator.putVariable("time", Integer.toString(time));
    }
}
