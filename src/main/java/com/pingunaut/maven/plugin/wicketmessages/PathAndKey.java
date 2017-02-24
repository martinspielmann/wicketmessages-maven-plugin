package com.pingunaut.maven.plugin.wicketmessages;

import java.nio.file.Path;

import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 * The Class PathAndKey is a container to be used as a key for translations.
 * 
 * @author Martin Spielmann
 */
public class PathAndKey implements Comparable<PathAndKey> {
    
    private final Path path;
    private final Object key;

    /**
     * Instantiates a new path and key.
     *
     * @param path the path
     * @param key the key
     */
    public PathAndKey(final Path path, final Object key) {
        this.path = path;
        this.key = key;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public Object getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathAndKey other = (PathAndKey) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final PathAndKey o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }
}
