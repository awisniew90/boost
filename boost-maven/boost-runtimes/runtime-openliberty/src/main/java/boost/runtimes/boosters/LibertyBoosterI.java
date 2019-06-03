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
package boost.runtimes.boosters;

import boost.runtimes.LibertyServerConfigGenerator;
import io.openliberty.boost.common.BoostException;

public interface LibertyBoosterI {

    public String getFeature();
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) throws BoostException;
}