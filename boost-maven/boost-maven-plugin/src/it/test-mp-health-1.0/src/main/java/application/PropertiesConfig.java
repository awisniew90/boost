package application;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PropertiesConfig {

    @Inject
    @ConfigProperty(name = "inMaintenance")
    private Provider<Boolean> inMaintenance;

    public boolean isInMaintenance() {
        return inMaintenance.get();
    }
}
