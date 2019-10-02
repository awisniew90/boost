/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.microshed.boost.runtimes.tomee.boosters;

import java.io.IOException;
import java.util.List;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.MPHealthBoosterConfig;
import org.microshed.boost.common.boosters.MPMetricsBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;
import org.microshed.boost.runtimes.tomee.TomeeServerConfigGenerator;

public class TomeeMPMetricsBoosterConfig extends MPMetricsBoosterConfig implements TomeeBoosterI {

    public TomeeMPMetricsBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public List<String> getDependencies() {
        List<String> deps = super.getDependencies();
        deps.add("org.apache.geronimo:geronimo-metrics:1.0.3");
        deps.add("org.apache.geronimo:geronimo-metrics-common:1.0.3");
        deps.add("org.eclipse.microprofile.metrics:microprofile-metrics-api:1.1");
        return deps;
    }

    @Override
    public void addServerConfig(TomeeServerConfigGenerator tomeeConfig) throws IOException {
        tomeeConfig.addCatalinaProperty("geronimo.metrics.jaxrs.activated", "true");
    }
}
