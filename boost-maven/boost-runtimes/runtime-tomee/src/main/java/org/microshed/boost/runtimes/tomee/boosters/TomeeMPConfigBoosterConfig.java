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

import java.util.List;

import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.MPConfigBoosterConfig;
import org.microshed.boost.common.config.BoosterConfigParams;

public class TomeeMPConfigBoosterConfig extends MPConfigBoosterConfig {

    public TomeeMPConfigBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params, logger);
    }

    @Override
    public List<String> getDependencies() {
        List<String> deps = super.getDependencies();
        deps.add("org.apache.geronimo.config:geronimo-config-impl:1.2.1");
        deps.add("org.eclipse.microprofile.config:microprofile-config-api:1.3");
        deps.add("org.osgi:org.osgi.annotation.versioning:1.0.0");
        return deps;
    }
}