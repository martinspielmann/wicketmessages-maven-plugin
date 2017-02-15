package com.pingunaut.maven.plugin.wicketmessages;

import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PathAndLocale implements Comparable<PathAndLocale> {
    private final Path path;
    private final Locale locale;

    public PathAndLocale(final Path path, final Locale locale) {
        this.path = path;
        this.locale = locale;
    }

    public Path getPath() {
        return path;
    }

    public Object getLocale() {
        return locale;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int compareTo(final PathAndLocale o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }
}
