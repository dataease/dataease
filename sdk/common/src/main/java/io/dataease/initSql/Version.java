package io.dataease.initSql;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class Version implements Comparable<Version> {
    private final String version;
    private final List<String> parts;

    public Version(String version) {
        this.version = version;
        this.parts = parseVersionString(version);
    }

    private List<String> parseVersionString(String version) {
        return Arrays.asList(version.split("\\."));
    }

    private static int parsePart(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public int compareTo(Version other) {
        int i = 0;
        while (i < parts.size() || i < other.parts.size()) {
            if (i < parts.size() && i < other.parts.size()) {
                int thisPart = parsePart(parts.get(i));
                int otherPart = parsePart(other.parts.get(i));
                if (thisPart != otherPart) {
                    return thisPart - otherPart;
                }
            } else if (i < parts.size()) {
                if (parsePart(parts.get(i)) != 0) {
                    return 1;
                }
            } else {
                if (parsePart(other.parts.get(i)) != 0) {
                    return -1;
                }
            }
            i++;
        }
        return 0;
    }
}
