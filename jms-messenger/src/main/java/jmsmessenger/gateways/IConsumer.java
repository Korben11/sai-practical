package jmsmessenger.gateways;

import javax.jms.MessageListener;

public interface IConsumer {
    public void onMessage(MessageListener listener);
}
