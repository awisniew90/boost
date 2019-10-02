package it;

import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetricsIT {

    private List<String> metrics;
    private Client client;
    private static String baseHttpUrl;

    private final String ITEMS_ADD_TO_CART = "items/add-to-cart";
    private final String ITEMS_REMOVE_FROM_CART = "items/remove-from-cart";
    private final String ITEMS_INVENTORY = "items/inventory";
    private final String ITEMS_CHECKOUT = "items/checkout";
    private final String METRICS_APPLICATION = "metrics/application";

    @BeforeClass
    public static void oneTimeSetup() {
        String httpPort = System.getProperty("boost_http_port");
        baseHttpUrl = "http://localhost:" + httpPort + "/";
    }

    @Before
    public void setup() {
        client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);
    }

    @After
    public void teardown() {
        client.close();
    }

    /**
     * Test the gauge metric to monitor items in inventory
     */
    @Test
    public void testTotalItemsGuageMetric() {
        int currentItemsCount = 0;

        // Get current number of items
        metrics = getMetrics();
        for (String metric : metrics) {
            if (metric.startsWith("application:total_items_count")) {
                currentItemsCount = Integer.parseInt(metric.split(" ")[1]);
            }
        }

        // Add an item to the cart
        connectToEndpoint(baseHttpUrl + ITEMS_ADD_TO_CART);

        // Check the inventory and make sure only 99 items are available now
        metrics = getMetrics();
        for (String metric : metrics) {
            if (metric.startsWith("application:total_items_count")) {
                assertEquals("The total number of items in the inventory is not correct", currentItemsCount--,
                        Integer.parseInt(metric.split(" ")[1]));
            }
        }
    }

    /**
     * Test the access count metric when adding items to cart
     */
    @Test
    public void testInventoryAccessCountMetric() {
        // Add 10 items to the cart
        for (int i = 0; i < 10; i++) {
            connectToEndpoint(baseHttpUrl + ITEMS_ADD_TO_CART);
        }

        metrics = getMetrics();
        for (String metric : metrics) {
            System.out.println(metric);
            if (metric.startsWith("application:add_to_cart_access_count")) {
                assertTrue(Integer.parseInt(metric.split(" ")[1]) >= 10);
            }
        }
    }

    /**
     * Test that the timed metric on the checkout endpoint records greater than
     * 5 seconds.
     */
    @Test
    public void testCheckoutTimedMetric() {
        connectToEndpoint(baseHttpUrl + ITEMS_CHECKOUT);
        metrics = getMetrics();
        for (String metric : metrics) {
            if (metric.startsWith("application:checkout_time")) {
                int seconds = Integer.parseInt(metric.split(" ")[1]);
                assertTrue(seconds >= 5);
            }
        }

    }

    public void connectToEndpoint(String url) {
        Response response = this.getResponse(url);
        this.assertResponse(url, response);
        response.close();
    }

    private List<String> getMetrics() {
        Response metricsResponse = client.target(baseHttpUrl + METRICS_APPLICATION).request(MediaType.TEXT_PLAIN).get();

        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) metricsResponse.getEntity()));
        List<String> result = new ArrayList<String>();
        try {
            String input;
            while ((input = br.readLine()) != null) {
                System.out.println(input);
                result.add(input);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        metricsResponse.close();
        return result;
    }

    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    }
}