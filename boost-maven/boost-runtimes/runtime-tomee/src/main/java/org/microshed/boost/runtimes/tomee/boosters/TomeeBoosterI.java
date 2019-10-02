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

import org.microshed.boost.runtimes.tomee.TomeeServerConfigGenerator;

public interface TomeeBoosterI {
    public List<String> getDependencies();

    public void addServerConfig(TomeeServerConfigGenerator tomeeConfig) throws Exception;
}
