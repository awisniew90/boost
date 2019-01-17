/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.common.config;

import static io.openliberty.boost.common.config.ConfigConstants.JSONP_11;

import java.util.Properties;

import org.w3c.dom.Document;

public class JSONPBoosterPackConfigurator extends BoosterPackConfigurator {

    String libertyFeature = null;

    public JSONPBoosterPackConfigurator(String version) {
        if (version.equals(MP_20_VERSION)) {
            libertyFeature = JSONP_11;
        }

    }

    @Override
    public String getFeature() {
        return libertyFeature;
    }

    @Override
    public void addServerConfig(Document doc) {
        // No config to write

    }

    @Override
    public String getDependency() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Properties getServerProperties() {
        // TODO Auto-generated method stub
        return null;
    }

}