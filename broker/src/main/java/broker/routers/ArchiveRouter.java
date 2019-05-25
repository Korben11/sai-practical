package broker.routers;

import jmsmessenger.models.LoanArchive;
import jmsmessenger.serializers.LoanArchiveSerializer;
import org.glassfish.jersey.client.ClientConfig;

import java.net.URI;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class ArchiveRouter {
    private WebTarget client;
    private LoanArchiveSerializer serializer;

    public ArchiveRouter(String url) {
        URI uri = UriBuilder.fromUri(url).build();
        this.client = ClientBuilder.newClient(new ClientConfig()).target(uri);
        serializer = new LoanArchiveSerializer();
    }

    public void archive(LoanArchive loanArchive) {
        if (loanArchive.getInterest() == 0) {
            return;
        }

        this.client.request(MediaType.APPLICATION_JSON).post(
                Entity.entity(serializer.serializeLoanArchive(loanArchive), MediaType.APPLICATION_JSON)
        );
    }
}
