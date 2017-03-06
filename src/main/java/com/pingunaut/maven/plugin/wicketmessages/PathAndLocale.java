package com.pingunaut.maven.plugin.wicketmessages;

import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 * The Class PathAndLocale is a container to be used as a key for translations.
 *
 * @author Martin Spielmann
 */
public class PathAndLocale implements Comparable<PathAndLocale> {

    private final Path path;
    private final Locale locale;

    /**
     * Instantiates a new path and locale.
     *
     * @param path the path
     * @param locale the locale
     */
    public PathAndLocale(final Path path, final Locale locale) {
        this.path = path;
        this.locale = locale;
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
     * Gets the locale.
     *
     * @return the locale
     */
    public Object getLocale() {
        return locale;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PathAndLocale other = (PathAndLocale) obj;
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final PathAndLocale o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }
}
