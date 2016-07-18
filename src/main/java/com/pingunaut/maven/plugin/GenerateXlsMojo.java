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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.pingunaut.maven.plugin.messages.XlsWriter;

/**
 *
 */
@Mojo(name = "generateXls", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateXlsMojo extends AbstractWicketMessagesMojo {

    private final Map<PathAndKey, Map<Locale, String>> localeProps = new HashMap<>();


    private void addProps(Path file, Object key, Locale locale, Object value) {
        PathAndKey pathAndKey = new PathAndKey(basePath().relativize(file), key);
        localeProps.putIfAbsent(pathAndKey, new HashMap<>());
        Map<Locale, String> localeMap = localeProps.get(pathAndKey);
        localeMap.putIfAbsent(locale, value == null ? "" : value.toString());
    }

    @Override
    public void execute() throws MojoExecutionException {
        List<Path> files = new ListWicketMessagesMojo().listFiles(basedir, fileExtension);

        //find default language files and aditional locales

        files.forEach(file -> {
            String fileName = FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getFileName().toString()));
            int indexOfFirstUnderscore = fileName.indexOf('_');

            Properties properties = new Properties();
            try {
                properties.loadFromXML(Files.newInputStream(file));
            } catch (IOException e) {
                getLog().error("Error while loading the properites", e);
            }

            properties.forEach((k, v) -> {
                if (-1 == indexOfFirstUnderscore) {
                    //default file
                    addProps(file, k, DEFAULT_LOCALE, v);
                } else {
                    //other locale files
                    String baseFileName = fileName.substring(0, indexOfFirstUnderscore);
                    String localeName = fileName.substring(indexOfFirstUnderscore + 1, fileName.length());
                    addProps(Paths.get(file.getParent().toString(), baseFileName + fileExtension), k, new Locale(localeName), v);
                }
            });
        });

        new XlsWriter(localeProps).writeToFile();
    }
}
