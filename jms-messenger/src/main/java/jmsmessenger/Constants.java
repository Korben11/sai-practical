package jmsmessenger;

public final class Constants {

    // services
    public static final String HTTP_LOCALHOST_8080_CREDIT_REST_HISTORY = "http://localhost:8080/credit/rest/history/";
    public static final String HTTP_LOCALHOST_8080_ARCHIVE_REST_ACCEPTED = "http://localhost:8080/archive/rest/accepted";

    public static final String TCP_LOCALHOST_61616 = "tcp://localhost:61616";
    public static final String ORG_APACHE_ACTIVEMQ_JNDI_ACTIVE_MQINITIAL_CONTEXT_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    public static final String QUEUE = "queue.";
    public static final String CONNECTION_FACTORY = "ConnectionFactory";

    // Banks
    public static enum BANK {
        ABN,
        ING,
        RABO,
    }

    // Rules
    // LTE = LESS THAN EQUAL
    // GTE = GREATER THAN EQUAL
    public static final String AMOUNT_LTE_100000_AND_TIME_LTE_10 = "#{amount} <= 100000 && #{time} <= 10";
    public static final String AMOUNT_GTE_200000_AND_AMOUNT_LTE_300000_AND_TIME_LTE_20 = "#{amount} >= 200000 && #{amount} <= 300000 && #{time} <= 20";
    public static final String AMOUNT_LTE_250000_AND_TIME_LTE_15 = "#{amount} <= 250000 && #{time} <= 15";

    public static final String LOAN_CLIENT_REQUEST_QUEUE = "LoanClientRequestQueue";
    public static final String LOAN_CLIENT_RESPONSE_QUEUE = "LoanClientResponseQueue";
    public static final String BANK_CLIENT_REQUEST_QUEUE = "BankClientRequestQueue";
    public static final String BANK_CLIENT_RESPONSE_QUEUE = "BankClientResponseQueue";


    private Constants(){}
}
