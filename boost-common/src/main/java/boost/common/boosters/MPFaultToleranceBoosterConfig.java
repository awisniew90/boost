/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package boost.common.boosters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mpFaultTolerance")
public class MPFaultToleranceBoosterConfig extends AbstractBoosterConfig {

    public MPFaultToleranceBoosterConfig(Map<String, String> dependencies, Properties boostProperties, BoostLoggerI logger) throws BoostException {
        super(dependencies.get(getCoordinates(MPFaultToleranceBoosterConfig.class)));
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }
}