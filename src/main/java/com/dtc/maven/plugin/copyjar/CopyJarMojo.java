package com.dtc.maven.plugin.copyjar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "copyjar", defaultPhase = LifecyclePhase.INSTALL)
public class CopyJarMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", property = "sourceDir", required = true)
	private File sourceDir;

	@Parameter(property = "targetDir", required = true)
	private File targetDir;

	@Parameter(defaultValue = "${project.artifactId}-${project.version}.jar", property = "fileName", required = true)
	private String fileName;

	@Parameter(defaultValue = "${project.artifactId}", property = "artifactId")
	private String artifactId;

	@Parameter(defaultValue = "${project.groupId}", property = "artifactId")
	private String groupId;

	@Parameter(property = "exclude")
	private String exclude;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		String fn=fileName;
		File sourceFile = new File(sourceDir, fn);
		
		Set<String> excludeSet=new HashSet<String>();
		if(exclude!=null) {
			excludeSet.addAll(Arrays.asList(exclude.split(",")));
		}
		
		if(excludeSet.contains(artifactId)) {
			getLog().info("exclude copy for jar:"+artifactId);
			return ;
		}
		
		if (!fileIsExists(sourceFile)) {
			fn=groupId+"-"+fn;
			getLog().warn(sourceFile.getName()+"is not exists,try to read "+fn);
			sourceFile = new File(sourceDir,fn);
			if(!fileIsExists(sourceFile)) {
				getLog().error(sourceFile.getName()+" and " + fn + "are not exists,exit copyjar!");
				return ;
			}
			
		}
		copyFileUsingFileChannels(sourceFile, new File(targetDir, fn));
	}
	
	private static boolean fileIsExists(File file) {
		if (file == null) {
			return false;
		}
		return file.exists();
	}

	private void copyFileUsingFileChannels(File source, File dest) {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			
			try {
				inputChannel = new FileInputStream(source).getChannel();
				outputChannel = new FileOutputStream(dest).getChannel();
				outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			} finally {
				inputChannel.close();
				outputChannel.close();
			}
		} catch (Exception e) {
			getLog().error("copy error:", e);
		}
	}
}
