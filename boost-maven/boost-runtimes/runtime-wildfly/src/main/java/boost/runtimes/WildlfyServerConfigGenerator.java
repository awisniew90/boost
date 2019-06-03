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
package boost.runtimes;

import static io.openliberty.boost.common.config.ConfigConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.*;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.ServerConfigGenerator;
import net.wasdev.wlp.common.plugins.util.OSUtil;

/**
 * Create a Liberty server.xml
 *
 */
public class WildlfyServerConfigGenerator implements ServerConfigGenerator {

    private final String wildflyInstallDir;
    private final String cliScript;

    private final BoostLoggerI logger;

    private final Properties boostConfigProperties;

    public WildlfyServerConfigGenerator(String wildflyInstallDir, BoostLoggerI logger)
            throws ParserConfigurationException {

        this.wildflyInstallDir = wildflyInstallDir;

        if (OSUtil.isWindows()) {
            this.cliScript = "jboss-cli.bat";
        } else {
            this.cliScript = "jboss-cli.sh";
        }

        this.logger = logger;

        boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);

    }

    private void runCliScript(String command) throws IOException {

        ProcessBuilder pb = new ProcessBuilder(wildflyInstallDir + "/bin/" + cliScript,
                "--commands=embed-server," + command);

        logger.info("Issuing cli command: " + command);

        Process cliProcess = pb.start();

        // Print error stream
        BufferedReader error = new BufferedReader(new InputStreamReader(cliProcess.getErrorStream()));
        String line;
        while ((line = error.readLine()) != null) {
            logger.info(line);
        }

        // Print output stream
        BufferedReader in = new BufferedReader(new InputStreamReader(cliProcess.getInputStream()));
        while ((line = in.readLine()) != null) {
            logger.info(line);
        }

        // TODO: throw exception if error stream has any content

    }

    public void addServerConfig(AbstractBoosterConfig boosterConfig) throws Exception {
        if (boosterConfig instanceof JDBCBoosterConfig) {
            addDataSource(((JDBCBoosterConfig) boosterConfig).getJdbcDriver(),
                    ((JDBCBoosterConfig) boosterConfig).getDatasourceProperties());
        }
    }

    @Override
    public void addKeystore(Map<String, String> keystoreProps, Map<String, String> keyProps) {

    }

    @Override
    public void addApplication(String appName) throws Exception {
        runCliScript("deploy --force " + appName);
    }

    @Override
    public void addHostname(String hostname) throws Exception {

    }

    @Override
    public void addHttpPort(String httpPort) throws Exception {

    }

    @Override
    public void addHttpsPort(String httpsPort) throws Exception {

    }

    @Override
    public void addDataSource(String jdbcDriverJar, Properties boostDbProperties) throws Exception {

        // TODO: make variables

        StringBuilder datasourceCommand = new StringBuilder();

        datasourceCommand
                .append("data-source add --name=DefaultDataSource --jndi-name=java:jboss/datasources/DefaultDataSource --driver-name=\""
                        + jdbcDriverJar + "\"");

        String username = (String) boostDbProperties.get(BoostProperties.DATASOURCE_USER);
        if (username != null) {
            datasourceCommand.append(" --user-name=\"" + username + "\"");
        }

        String password = (String) boostDbProperties.get(BoostProperties.DATASOURCE_PASSWORD);
        if (password != null) {
            datasourceCommand.append(" --password=\"" + password + "\"");
        }

        String url = (String) boostDbProperties.get(BoostProperties.DATASOURCE_URL);
        if (url != null) {
            datasourceCommand.append(" --connection-url=\"" + url + "\"");
        } else {

            // Build the url
            String productName = null;
            if (jdbcDriverJar.contains(JDBCBoosterConfig.DERBY_ARTIFACT_ID)) {
                productName = "derby";
            } else if (jdbcDriverJar.contains(JDBCBoosterConfig.DB2_ARTIFACT_ID)) {
                productName = "db2";
            } else if (jdbcDriverJar.contains(JDBCBoosterConfig.MYSQL_ARTIFACT_ID)) {
                productName = "mysql";
            }

            StringBuilder jdbcUrl = new StringBuilder();
            jdbcUrl.append("jdbc:" + productName);

            if (productName.equals("derby")) {

                // Derby's URL is slightly different than MySQL and DB2
                String databaseName = (String) boostDbProperties.get(BoostProperties.DATASOURCE_DATABASE_NAME);
                jdbcUrl.append("/" + databaseName);

                String createDatabase = (String) boostDbProperties.get(BoostProperties.DATASOURCE_CREATE_DATABASE);
                if ("create".equals(createDatabase)) {
                    jdbcUrl.append(";create=true");
                }
            } else {

                String serverName = (String) boostDbProperties.get(BoostProperties.DATASOURCE_SERVER_NAME);
                if (serverName != null) {
                    jdbcUrl.append("://" + serverName);
                }

                String portNumber = (String) boostDbProperties.get(BoostProperties.DATASOURCE_PORT_NUMBER);
                if (portNumber != null) {
                    jdbcUrl.append(":" + portNumber);
                }

                String databaseName = (String) boostDbProperties.get(BoostProperties.DATASOURCE_DATABASE_NAME);
                if (databaseName != null) {
                    jdbcUrl.append("/" + databaseName);
                }
            }

            datasourceCommand.append(" --connection-url=\"" + jdbcUrl.toString() + "\"");
        }

        runCliScript(datasourceCommand.toString());

        // Add jdbc driver
        runCliScript("deploy --force " + wildflyInstallDir + "/boost/" + jdbcDriverJar + ".jar");
    }

}
