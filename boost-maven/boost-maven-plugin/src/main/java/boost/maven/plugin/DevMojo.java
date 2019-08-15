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
package boost.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import boost.common.BoostException;
import boost.common.boosters.AbstractBoosterConfig;
import boost.maven.utils.BoostLogger;

/**
 * Runs the executable archive application (in the console foreground).
 */
@Mojo(name = "dev", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DevMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
        
        Thread thread = new Thread(new Runnable()
    	{
    	   public void run()
    	   {
    		   try {
    			   setupPomListener();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	   }
    	});
    	thread.start(); 
        
        try {
        	// Create the boostConfig directory if it doesnt exist in the build directory
        	File boostConfigDir = new File(projectBuildDir + "/boostConfig");
        	if (!boostConfigDir.exists()) {
        		boostConfigDir.mkdirs();
        	}
        	this.getRuntimeInstance().generateServerConfig(projectBuildDir + "/boostConfig", projectBuildDir + "/boostResources", boosterConfigs);
            this.getRuntimeInstance().doDev();
        } catch (BoostException e) {
            throw new MojoExecutionException("Error starting dev mode", e);
        }
    }

    private void setupPomListener() throws Exception {

	    	WatchService watchService = FileSystems.getDefault().newWatchService();
	    	
	    	Path path = Paths.get(project.getFile().getParent());
	    	BoostLogger.getInstance().info(project.getFile().getParent());
	    	path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
	    	
	    	WatchKey wk;
	        while ((wk = watchService.take()) != null) {
		        for (WatchEvent<?> event : wk.pollEvents()) {
		        	if (event.context().toString().equals("pom.xml")) {
		        		BoostLogger.getInstance().info("Change to pom.xml detected.");
		        		this.project = reloadProject();
		        		super.execute();
		        		this.getRuntimeInstance().generateServerConfig(projectBuildDir + "/boostConfig", projectBuildDir + "/boostResources", boosterConfigs);
		        	}
		        }
		        wk.reset();
	        }
    }
    
    private MavenProject reloadProject() throws Exception {
    	File pomFile = new File(project.getFile().getAbsolutePath());
        ProjectBuildingResult build = mavenProjectBuilder.build(pomFile, session.getProjectBuildingRequest().setResolveDependencies(true));

        return build.getProject();  
    }
}
