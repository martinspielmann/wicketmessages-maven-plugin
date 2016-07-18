package com.pingunaut.maven.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractWicketMessagesMojo extends AbstractMojo {

    @Parameter(defaultValue = ".properties.xml", property = "fileExtension", required = true)
    protected String fileExtension;

    @Parameter(defaultValue = "${project.basedir}", property = "basedir", required = true)
    protected String basedir;

    protected static final Locale DEFAULT_LOCALE = new Locale("default");


    protected Path basePath() {
        return Paths.get(basedir);
    }
}
