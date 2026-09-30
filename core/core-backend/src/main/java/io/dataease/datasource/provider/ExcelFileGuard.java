package io.dataease.datasource.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.i18n.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Only server-generated uploads may be used as local Excel datasource files. */
public final class ExcelFileGuard {
    private static final Pattern FILE = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(?i:csv|xls|xlsx)");
    private static final Pattern OWNER = Pattern.compile("[0-9]+");
    private static final ObjectMapper JSON = new ObjectMapper();

    private ExcelFileGuard() {
    }

    private static IOException denied() {
        return new IOException(Translator.get("i18n_excel_file_forbidden"));
    }

    public static Path uploadDirectory(Path root, String owner) throws IOException {
        if (owner == null || !OWNER.matcher(owner).matches()) throw denied();
        Files.createDirectories(root);
        Path directory = root.toAbsolutePath().normalize().resolve(owner);
        if (Files.isSymbolicLink(directory)) throw denied();
        Files.createDirectories(directory);
        if (!directory.toRealPath().getParent().equals(root.toRealPath())) throw denied();
        return directory;
    }

    public static Path resolve(Path root, String value) throws IOException {
        if (value == null || value.isBlank()) throw denied();
        final Path file;
        try {
            file = Path.of(value);
        } catch (RuntimeException e) {
            throw denied();
        }
        Path base = root.toAbsolutePath().normalize();
        if (!file.isAbsolute() || !file.equals(file.normalize()) || !file.startsWith(base)) throw denied();
        Path relative = base.relativize(file);
        if (relative.getNameCount() < 1 || relative.getNameCount() > 2
                || !FILE.matcher(file.getFileName().toString()).matches()
                || (relative.getNameCount() == 2 && !OWNER.matcher(relative.getName(0).toString()).matches())) {
            throw denied();
        }
        Path cursor = base;
        for (Path segment : relative) {
            cursor = cursor.resolve(segment);
            if (Files.isSymbolicLink(cursor)) throw denied();
        }
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                || !file.toRealPath().startsWith(base.toRealPath())) throw denied();
        return file;
    }

    /** Existing paths must come from the authorized datasource loaded on the server, never from the request. */
    public static void validateConfiguration(Path root, String configuration, String storedConfiguration,
                                             String owner) throws IOException {
        if (owner == null || !OWNER.matcher(owner).matches()) throw denied();
        JsonNode requested = JSON.readTree(configuration);
        if (requested == null || !requested.isArray()) throw denied();
        Set<String> stored = new HashSet<>();
        if (storedConfiguration != null) {
            JsonNode previous = JSON.readTree(storedConfiguration);
            if (previous != null && previous.isArray()) {
                previous.forEach(sheet -> {
                    if (sheet.path("path").isTextual()) stored.add(sheet.path("path").asText());
                });
            }
        }
        Path ownedDirectory = root.toAbsolutePath().normalize().resolve(owner);
        for (JsonNode sheet : requested) {
            String value = sheet.path("path").asText(null);
            Path file = resolve(root, value);
            if (!file.getParent().equals(ownedDirectory) && !stored.contains(value)) throw denied();
        }
    }

    public static InputStream open(Path root, String value) throws IOException {
        return Files.newInputStream(resolve(root, value), StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    }
}
