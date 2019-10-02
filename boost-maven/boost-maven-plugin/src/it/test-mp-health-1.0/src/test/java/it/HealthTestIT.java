package it;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import javax.json.JsonArray;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class HealthTestIT {

    @After
    public void teardown() {
        HealthTestUtil.cleanUp();
    }

    @Test
    public void testServiceStateUp() {
        JsonArray serviceState = HealthTestUtil.connectToHealthEnpoint(200);
        assertEquals("The state of the PropertiesResource service is not correct.", "UP",
                HealthTestUtil.getActualState("PropertiesResource", serviceState));
    }

    @Test
    public void testServiceStateDown() {
        JsonArray serviceState = HealthTestUtil.connectToHealthEnpoint(200);
        assertEquals("The state of the PropertiesResource service is not correct.", "UP",
                HealthTestUtil.getActualState("PropertiesResource", serviceState));

        // Place service in maintenance mode
        HealthTestUtil.changeInventoryProperty(HealthTestUtil.INV_MAINTENANCE_FALSE,
                HealthTestUtil.INV_MAINTENANCE_TRUE);

        serviceState = HealthTestUtil.connectToHealthEnpoint(503);
        assertEquals("The state of the PropertiesResource service is not correct.", "DOWN",
                HealthTestUtil.getActualState("PropertiesResource", serviceState));
    }

}
