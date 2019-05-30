package boost.runtimes.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.JPA_21;
import static io.openliberty.boost.common.config.ConfigConstants.JPA_22;
import java.util.Map;
import io.openliberty.boost.common.boosters.JPABoosterConfig;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import boost.runtimes.LibertyServerConfigGenerator;
import boost.runtimes.boosters.LibertyBoosterI;

public class LibertyJPABoosterConfig extends JPABoosterConfig implements LibertyBoosterI {

    public LibertyJPABoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
	public String getFeature() {
        if (getVersion().equals(EE_7_VERSION)) {
            return JPA_21;
        } else if (getVersion().equals(EE_8_VERSION)) {
            return JPA_22;
        }
        return null;
    }

    @Override
	public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {
        
    }
}