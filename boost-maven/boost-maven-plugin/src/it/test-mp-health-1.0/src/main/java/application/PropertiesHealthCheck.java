package application;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class PropertiesHealthCheck implements HealthCheck {

    public boolean isHealthy() {
        try {
            // Check that the "properties" endpoint is reachable
            String url = new URI("http", null, "localhost", 9000, "/properties", null, null).toString();

            Client client = ClientBuilder.newClient();
            Response response = client.target(url).request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() != 200) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public HealthCheckResponse call() {
        if (!isHealthy()) {
            return HealthCheckResponse.named(PropertiesResource.class.getSimpleName())
                    .withData("service", "not available").down().build();
        }
        return HealthCheckResponse.named(PropertiesResource.class.getSimpleName()).withData("service", "available").up()
                .build();
    }

}
