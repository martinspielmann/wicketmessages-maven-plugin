package com.pingunaut.maven.plugin.wicketmessages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;

import org.apache.maven.plugin.logging.Log;

public class GrepFiles {

    public static boolean fileContainsRecursive(final Path root, final String searchFor, final Log log, final String extension) {
        final byte[] searchBytes = searchFor.getBytes(StandardCharsets.UTF_8);
        final BiPredicate<Path, BasicFileAttributes> matcher = (p, attrs) -> {
            return attrs.isRegularFile() && !p.startsWith(Paths.get("target")) && !p.toString().endsWith(extension);
        };
        try {
            final long matches = Files.find(root, Integer.MAX_VALUE, matcher).filter(p -> {
                try {
                    final byte[] fileBytes = Files.readAllBytes(p);
                    if (indexOf(fileBytes, searchBytes) >= 0) {
                        log.debug("found in " + p.toString());
                        return true;
                    }
                } catch (final IOException e) {
                    log.error("Error reading file", e);
                    return true;
                }
                return false;
            }).count();
            return matches > 0;
        } catch (final IOException e) {
            log.error("Error reading file", e);
            return true;
        }
    }

    public static int indexOf(final byte[] array, final byte[] target) {
        outer: for (int i = 0; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
