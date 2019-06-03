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
package boost.runtimes;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.common.config.ConfigConstants;
import io.openliberty.boost.common.runtimes.RuntimeI;
import io.openliberty.boost.maven.runtimes.RuntimeParams;
import io.openliberty.boost.maven.utils.BoostLogger;
import io.openliberty.boost.maven.utils.MavenProjectUtil;
import net.wasdev.wlp.common.plugins.util.PluginExecutionException;

public class WildflyRuntime implements RuntimeI {

    private final Map<String, String> deps;
    private final ExecutionEnvironment env;
    private final MavenProject project;
    private final Plugin mavenDepPlugin;

    private final String serverName = "BoostServer";
    private final String projectBuildDir;
    private final String wildflyInstallDir;

    private final String runtimeGroupId = "org.wildfly";
    private final String runtimeArtifactId = "wildfly-dist";
    private final String runtimeVersion = "16.0.0.Final";

    private String wildflyMavenPluginGroupId = "org.wildfly.plugins";
    private String wildflyMavenPluginArtifactId = "wildfly-maven-plugin";
    private String wildflyMavenPluginVersion = "2.0.1.Final";
    
    protected String mavenDependencyPluginGroupId = "org.apache.maven.plugins";
    protected String mavenDependencyPluginArtifactId = "maven-dependency-plugin";

    public WildflyRuntime() {
        this.deps = null;
        this.env = null;
        this.project = null;
        this.projectBuildDir = null;
        this.wildflyInstallDir = null;
        this.mavenDepPlugin = null;
    }

    public WildflyRuntime(RuntimeParams runtimeParams) {
        this.deps = runtimeParams.getDeps();
        this.env = runtimeParams.getEnv();
        this.project = runtimeParams.getProject();
        this.projectBuildDir = project.getBuild().getDirectory();
        this.wildflyInstallDir = projectBuildDir + "/wildfly-" + runtimeVersion;
        this.mavenDepPlugin = runtimeParams.getMavenDepPlugin();
    }

    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(wildflyMavenPluginGroupId), artifactId(wildflyMavenPluginArtifactId),
                version(wildflyMavenPluginVersion));
    }
    
    protected Plugin getMavenDependencyPlugin() throws MojoExecutionException {
        return plugin(groupId(mavenDependencyPluginGroupId), artifactId(mavenDependencyPluginArtifactId));
    }

    @Override
    public void doPackage() throws BoostException {
        List<AbstractBoosterConfig> boosterConfigs;
        try {
            boosterConfigs = BoosterConfigurator.getBoosterConfigs(deps, BoostLogger.getInstance());
        } catch (Exception e) {
            throw new BoostException("Error copying booster dependencies", e);
        }

        try {
            packageWildfly(boosterConfigs);
        } catch (Exception e) {
            throw new BoostException("Error packaging Wildfly server", e);
        }
    }

    private void packageWildfly(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
    	installWildfly();

    	copyBoosterDependencies(boosterConfigs);

        generateServerConfig(boosterConfigs);

        // Create the Liberty runnable jar
//        createUberJar();
    }

    /**
     * Get all booster dependencies and invoke the maven-dependency-plugin to
     * copy them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyBoosterDependencies(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigs, this,
                BoostLogger.getInstance());

        for (String dep : dependenciesToCopy) {

            String[] dependencyInfo = dep.split(":");

            executeMojo(mavenDepPlugin, goal("copy"),
                    configuration(element(name("outputDirectory"), wildflyInstallDir + "/boost"),
                            element(name("artifactItems"),
                                    element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
                                            element(name("artifactId"), dependencyInfo[1]),
                                            element(name("version"), dependencyInfo[2])))),
                    env);
        }
    }

    /**
     * Generate config for the Liberty server based on the Maven project.
     * 
     * @throws MojoExecutionException
     */
    private void generateServerConfig(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {

        try {
            // Generate server config
            generateWildflyServerConfig(boosterConfigs);

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    private List<Path> getWarFiles() {
        List<Path> warFiles = new ArrayList<Path>();

        // TODO: are these war files downloaded to target? Do we need to copy
        // them using the dependency plugin?
        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.getType().equals("war")) {
                warFiles.add(Paths.get(artifact.getArtifactId() + "-" + artifact.getVersion()));
            }
        }

        if (project.getPackaging().equals(ConfigConstants.WAR_PKG_TYPE)) {
	        if (project.getVersion() == null) {
	            warFiles.add(Paths.get(projectBuildDir + "/" + project.getArtifactId() + ".war"));
	        } else {
	            warFiles.add(
	                    Paths.get(projectBuildDir + "/" + project.getArtifactId() + "-" + project.getVersion() + ".war"));
	        }
        }

        return warFiles;
    }

    /**
     * Configure the Liberty runtime
     * 
     * @param boosterConfigurators
     * @throws Exception
     */
    private void generateWildflyServerConfig(List<AbstractBoosterConfig> boosterConfigurators) throws Exception {

    	List<Path> warFiles = getWarFiles();
        WildlfyServerConfigGenerator wildflyConfig = new WildlfyServerConfigGenerator(wildflyInstallDir,
                BoostLogger.getInstance());

        
        
        // Add default http endpoint configuration
        Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(BoostLogger.getInstance());

        String host = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HOST, "*");
        wildflyConfig.addHostname(host);

        String httpPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTP_PORT, "9080");
        wildflyConfig.addHttpPort(httpPort);

        String httpsPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTPS_PORT, "9443");
        wildflyConfig.addHttpsPort(httpsPort);

        
        
        // Add war configuration if necessary
        if (!warFiles.isEmpty()) {
            for (Path war : warFiles) {
            	wildflyConfig.addApplication(war.toString());
            }
        } else {
            throw new Exception(
                    "No war files were found. The project must have a war packaging type or specify war dependencies.");
        }

        // Loop through configuration objects and add config
        for (AbstractBoosterConfig configurator : boosterConfigurators) {
        	wildflyConfig.addServerConfig(configurator);
        }

    }


    private void installWildfly() throws MojoExecutionException {

        executeMojo(getMavenDependencyPlugin(), goal("unpack"),
                configuration(element(name("outputDirectory"), projectBuildDir),
                        element(name("artifactItems"), getRuntimeArtifactElement())),
                env);
    }

    private Element getRuntimeArtifactElement() {
    	return element(name("artifactItem"), element(name("groupId"), runtimeGroupId),
                element(name("artifactId"), runtimeArtifactId),
                element(name("version"), runtimeVersion),
                element(name("type"), "zip"));
    }

    /**
     * Invoke the liberty-maven-plugin to package the server into a runnable
     * Liberty JAR
     */
    private void createUberJar() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("package-server"),
                configuration(element(name("isInstall"), "false"), element(name("include"), "minify,runnable"),
                        element(name("outputDirectory"), "target/liberty-alt-output-dir"),
                        element(name("packageFile"), ""), element(name("serverName"), serverName)),
                env);
    }

    @Override
    public void doDebug(boolean clean) throws BoostException {}

    @Override
    public void doRun(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("run"), configuration(element(name("jbossHome"), wildflyInstallDir)), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error running Wildfly server", e);
        }

    }

    @Override
    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException {
    	try {
            executeMojo(getPlugin(), goal("start"), configuration(element(name("jbossHome"), wildflyInstallDir)), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error starting Wildfly server", e);
        }
    }

    @Override
    public void doStop() throws BoostException {
    	try {
            executeMojo(getPlugin(), goal("shutdown"), configuration(), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error stopping Wildfly server", e);
        }
    }

}
