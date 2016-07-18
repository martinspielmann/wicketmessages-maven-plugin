package com.pingunaut.maven.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

/**
 *
 */
@Mojo(name = "listFiles", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ListWicketMessagesMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = ".properties.xml", property = "fileExtension", required = true)
    private String fileExtension;

    @Parameter(defaultValue = "${project.basedir}", property = "basedir", required = true)
    private String basedir;

    @Override
    public void execute() throws MojoExecutionException {
        listFiles(basedir, fileExtension).forEach(System.out::println);
    }

    public List<Path> listFiles(String basedir, String fileExtension) {
        try {
            Path targetPath = Paths.get(basedir, "target");
            return Files.find(Paths.get(basedir),
                    Integer.MAX_VALUE,
                    (filePath, fileAttr) -> {
                        //check if is regular file (no folder, link, ...)
                        return fileAttr.isRegularFile()
                                //check if ends with your defined location
                                && filePath.toString().endsWith(fileExtension)
                        //check no in target folder (fildes should be places somewhere in src
                                && !filePath.startsWith(targetPath);
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            getLog().error("Error while listing message files", e);
        }
        //do not return null;
        return Collections.emptyList();
    }
}
