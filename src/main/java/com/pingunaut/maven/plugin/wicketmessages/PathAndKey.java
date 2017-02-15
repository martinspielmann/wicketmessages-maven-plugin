package com.pingunaut.maven.plugin.wicketmessages;

import java.nio.file.Path;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PathAndKey implements Comparable<PathAndKey> {
    private final Path path;
    private final Object key;

    public PathAndKey(final Path path, final Object key) {
        this.path = path;
        this.key = key;
    }

    public Path getPath() {
        return path;
    }

    public Object getKey() {
        return key;
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
    public int compareTo(final PathAndKey o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }
}
