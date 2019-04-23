package broker.gui;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.text.MessageFormat;

public class ListViewLine {

    private Message loanRequestMessage;
    private Message loanReplyMessage;
    private Message bankRequestMessage;
    private Message bankReplyMessage;

    public Message getLoanRequestMessage() {
        return loanRequestMessage;
    }

    public void setLoanReplyMessage(Message loanReplyMessage) {
        this.loanReplyMessage = loanReplyMessage;
    }

    public void setBankReplyMessage(Message bankReplyMessage) {
        this.bankReplyMessage = bankReplyMessage;
    }

    public ListViewLine(Message loanRequestMessage, Message bankRequestMessage) {
        this.loanRequestMessage = loanRequestMessage;
        this.loanReplyMessage = null;
        this.bankRequestMessage = bankRequestMessage;
        this.bankReplyMessage = null;
    }

    @Override
    public String toString() {

        TextMessage textMessageLoanRequest = (TextMessage) this.loanRequestMessage;
        TextMessage textMessageLoanResponse = null;
        String reply = "waiting...";

        try {
            if (loanReplyMessage != null) {
                textMessageLoanResponse = (TextMessage) loanReplyMessage;
                reply = textMessageLoanResponse.getText();
            }
            return MessageFormat.format("{0} ---> {1}", textMessageLoanRequest.getText(), reply);

        } catch (JMSException e) {
            e.printStackTrace();
        }

        return "";
    }
}
