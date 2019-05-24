package broker.enrichers;

import jmsmessenger.models.CreditHistory;
import jmsmessenger.serializers.CreditHistorySerializer;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class CreditHistoryEnricher {
    private WebTarget client;
    private CreditHistorySerializer serializer;

    public CreditHistoryEnricher(String url) {
        URI uri = UriBuilder.fromUri(url).build();
        client = ClientBuilder.newClient(new ClientConfig()).target(uri);
        serializer = new CreditHistorySerializer();
    }

    public CreditHistory getCreditHistory(int ssn) {
        Invocation.Builder request = client.path(Integer.toString(ssn)).request(MediaType.APPLICATION_JSON);
        Response response = request.get();
        String json = response.readEntity(String.class);
        if (response.getStatus() == 200) {
            return serializer.deserializeCreditHistory(json);
        }
        return null;
    }
}
