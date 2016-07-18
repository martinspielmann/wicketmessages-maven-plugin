package com.pingunaut.maven.plugin;

import java.nio.file.Path;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PathAndKey implements Comparable<PathAndKey> {
    private final Path path;
    private final Object key;

    public PathAndKey(Path path, Object key) {
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
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int compareTo(PathAndKey o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }
}
