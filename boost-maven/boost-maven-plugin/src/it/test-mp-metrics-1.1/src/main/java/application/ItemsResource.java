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
import javax.ws.rs.QueryParam;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.MetricUnits;

@Path("/items")
public class ItemsResource {

    private int items = 100;

    @GET
    @Path("/add-to-cart")
    public Response addToCart() {
        return Response.ok(addItemToCart()).build();
    }

    @GET
    @Path("/checkout")
    @Timed(name = "checkout_time", absolute = true, description = "How long it takes to checkout")
    public Response checkout() throws Exception {
        Thread.sleep(5000);
        return Response.ok("Checkout successful!").build();

    }

    @Gauge(unit = MetricUnits.NONE, name = "total_items_count", absolute = true, description = "Total number of items in inventory")
    public int getTotalItemsCount() {
        return items;
    }

    @Counted(name = "add_to_cart_access_count", absolute = true, monotonic = true, description = "Number of times an item was added to the cart")
    private boolean addItemToCart() {
        if (items > 0) {
            items--;
            return true;
        } else {
            return false;
        }
    }

}