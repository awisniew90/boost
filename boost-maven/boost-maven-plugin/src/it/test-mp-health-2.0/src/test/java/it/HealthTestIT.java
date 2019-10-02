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
        JsonArray serviceStatus = HealthTestUtil.connectToHealthEnpoint(200);
        assertEquals("The status of the PropertiesResource service is not correct.", "UP",
                HealthTestUtil.getActualStatus("PropertiesResource", serviceStatus));
    }

    @Test
    public void testServiceStateDown() {
        JsonArray serviceStatus = HealthTestUtil.connectToHealthEnpoint(200);
        assertEquals("The status of the PropertiesResource service is not correct.", "UP",
                HealthTestUtil.getActualStatus("PropertiesResource", serviceStatus));

        // Place service in maintenance mode
        HealthTestUtil.changeInventoryProperty(HealthTestUtil.INV_MAINTENANCE_FALSE,
                HealthTestUtil.INV_MAINTENANCE_TRUE);

        serviceStatus = HealthTestUtil.connectToHealthEnpoint(503);
        assertEquals("The status of the PropertiesResource service is not correct.", "DOWN",
                HealthTestUtil.getActualStatus("PropertiesResource", serviceStatus));
    }

}
