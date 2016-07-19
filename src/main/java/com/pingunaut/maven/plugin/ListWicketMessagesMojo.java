package com.pingunaut.maven.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The Class ListWicketMessagesMojo.
 * 
 * @author martin spielmann
 */
@Mojo(name = "listFiles", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ListWicketMessagesMojo extends AbstractWicketMessagesMojo {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException {
		listFiles(basedir, fileExtension).forEach(System.out::println);
	}

	/**
	 * List files.
	 *
	 * @param basedir
	 *            the basedir
	 * @param fileExtension
	 *            the file extension
	 * @return the list
	 */
	public List<Path> listFiles(String basedir, String fileExtension) {
		try {
			Path targetPath = Paths.get(basedir, "target");
			return Files.find(Paths.get(basedir), Integer.MAX_VALUE, (filePath, fileAttr) -> {
				// check if is regular file (no folder, link, ...)
				return fileAttr.isRegularFile()
						// check if ends with your defined location
						&& filePath.toString().endsWith(fileExtension)
				// check no in target folder (fildes should be places somewhere in src
						&& !filePath.startsWith(targetPath);
			}).collect(Collectors.toList());
		} catch (IOException e) {
			getLog().error("Error while listing message files", e);
		}
		// do not return null;
		return Collections.emptyList();
	}
}
