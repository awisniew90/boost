package application;

import java.util.Properties;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("properties")
public class PropertiesResource {

    @Inject
    PropertiesConfig propertiesConfig;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProperties() {

        if (propertiesConfig.isInMaintenance()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        Properties props = System.getProperties();
        return Response.ok(props).build();
    }
}