package com.pingunaut.maven.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
/*
 * Copyright 2001-2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.pingunaut.maven.plugin.messages.XlsWriter;

/**
 *
 */
@Mojo(name = "generateXls", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateXlsMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = ".properties.xml", property = "fileExtension", required = true)
    private String fileExtension;

    @Parameter(defaultValue = "${project.basedir}", property = "basedir", required = true)
    private String basedir;

    
    private final Map<PathAndKey, Map<Locale, String>> localeProps = new HashMap<>();
	public static final Locale DEFAULT_LOCALE = new Locale("default");

    
    private void addProps(Path file, Object key, Locale locale, Object value) {
    	PathAndKey pathAndKey = new PathAndKey(file, key);
    	localeProps.putIfAbsent(pathAndKey, new HashMap<>());
    	Map<Locale, String> localeMap = localeProps.get(pathAndKey);
    	String v = value==null?"":value.toString();
    	System.out.println("put " + v + " with " + locale + " to " + localeMap);
    	localeMap.putIfAbsent(locale, v);
    	System.out.println("after put " + v + " with " + locale);

	}
    
    @Override
    public void execute() throws MojoExecutionException {
        List<Path> files = new ListWicketMessagesMojo().listFiles(basedir, fileExtension);
        
      //find default language files and aditional locales
        
        files.forEach(file->{
        	String fileName = FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getFileName().toString()));
        	int indexOfFirstUnderscore = fileName.indexOf('_');

        	Properties properties = new Properties();
        	try {
    			properties.loadFromXML(Files.newInputStream(file));
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        	
            System.out.println("props for " + file.toString());

        	properties.forEach((k,v)->{
        		if(-1==indexOfFirstUnderscore){
            		//default file
            		addProps(file, k, DEFAULT_LOCALE, v);
            	}else{
            		//other locale files
            		String baseFileName = fileName.substring(0, indexOfFirstUnderscore);
            		String localeName = fileName.substring(indexOfFirstUnderscore+1, fileName.length());
            		addProps(Paths.get(file.getParent().toString(), baseFileName+fileExtension), k, new Locale(localeName), v);
            	}
        	});
        });

        System.out.println("start gen excel");
        XlsWriter xlsWriter = new XlsWriter(localeProps);
        xlsWriter.writeToFile();
    }
}
