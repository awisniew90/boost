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
package io.openliberty.boost.common.boosters;

import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.runtimes.RuntimeI;
import io.openliberty.boost.common.config.ServerConfigGenerator;
import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;

import static io.openliberty.boost.common.config.ConfigConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":jdbc")
public class JDBCBoosterConfig extends AbstractBoosterConfig {
	
	public static String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
	public static String DB2_DRIVER_CLASS_NAME = "com.ibm.db2.jcc.DB2Driver";
	public static String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	
    public static String DERBY_GROUP_ID = "org.apache.derby";
    public static String DERBY_ARTIFACT_ID = "derby";
    public static String DB2_GROUP_ID = "com.ibm.db2.jcc";
    public static String DB2_ARTIFACT_ID = "db2jcc";
    public static String MYSQL_GROUP_ID = "mysql";
    public static String MYSQL_ARTIFACT_ID = "mysql-connector-java";

    private static String DERBY_DEFAULT_VERSION = "10.14.2.0";

    private Properties boostConfigProperties;
    private String dependency;
    private String jdbcDriverJar;

    public JDBCBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {

    	boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);
    	
        // Determine JDBC driver dependency
        if (dependencies.containsKey(DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID)) {
            String derbyVersion = dependencies.get(DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID);
            this.dependency = DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID + ":" + derbyVersion;
            this.jdbcDriverJar = DERBY_ARTIFACT_ID + "-" + derbyVersion;

        } else if (dependencies.containsKey(DB2_GROUP_ID + ":" + DB2_ARTIFACT_ID)) {
            String db2Version = dependencies.get(DB2_GROUP_ID + ":" + DB2_ARTIFACT_ID);
            this.dependency = DB2_GROUP_ID + ":" + DB2_ARTIFACT_ID + ":" + db2Version;
            this.jdbcDriverJar = DB2_ARTIFACT_ID + "-" + db2Version;

        } else if (dependencies.containsKey(MYSQL_GROUP_ID + ":" + MYSQL_ARTIFACT_ID)) {
            String mysqlVersion = dependencies.get(MYSQL_GROUP_ID + ":" + MYSQL_ARTIFACT_ID);
            this.dependency = MYSQL_GROUP_ID + ":" + MYSQL_ARTIFACT_ID + ":" + mysqlVersion;
            this.jdbcDriverJar = MYSQL_ARTIFACT_ID + "-" + mysqlVersion;
            
        } else {
        	this.dependency = DERBY_GROUP_ID + ":" + DERBY_ARTIFACT_ID + ":" + DERBY_DEFAULT_VERSION;
        	this.jdbcDriverJar = DERBY_ARTIFACT_ID + "-" + DERBY_DEFAULT_VERSION;
        }
    }

    public Properties getDatasourceProperties() {
    	
        Properties datasourceProperties = new Properties();
        
        // Find and add all "boost.db." properties. 
        for (String key : boostConfigProperties.stringPropertyNames()) {
            if (key.startsWith(BoostProperties.DATASOURCE_PREFIX)) {
                String value = (String) boostConfigProperties.get(key);
                datasourceProperties.put(key, value);
            }
        }
        
        if (!datasourceProperties.containsKey(BoostProperties.DATASOURCE_URL) &&
        	!datasourceProperties.containsKey(BoostProperties.DATASOURCE_DATABASE_NAME) && 
        	!datasourceProperties.containsKey(BoostProperties.DATASOURCE_SERVER_NAME) && 
        	!datasourceProperties.containsKey(BoostProperties.DATASOURCE_PORT_NUMBER)) {
        	
        	// No db connection properties have been specified. Set defaults.
        	if (dependency.contains(DERBY_GROUP_ID)) {
        		datasourceProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, DERBY_DB);
		        datasourceProperties.put(BoostProperties.DATASOURCE_CREATE_DATABASE, "create");
		        
        	} else if (dependency.contains(DB2_GROUP_ID)) {
        		datasourceProperties.put(BoostProperties.DATASOURCE_URL, "jdbc:db2://localhost:" + DB2_DEFAULT_PORT_NUMBER);
        		
        	} else if (dependency.contains(MYSQL_GROUP_ID)) {
        		datasourceProperties.put(BoostProperties.DATASOURCE_URL, "jdbc:mysql://localhost:" + MYSQL_DEFAULT_PORT_NUMBER);
        	}
        }
        return datasourceProperties; 
    }

    @Override
    public List<String> getDependencies(RuntimeI runtime) {
        List<String> deps = new ArrayList<String>();
        deps.add(dependency);
        
        return deps;
    }

    public String getJdbcDriver() {
        return jdbcDriverJar;
    }
}
