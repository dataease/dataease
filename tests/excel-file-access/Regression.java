import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.datasource.provider.ExcelFileGuard;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.i18n.DeReloadableResourceBundleMessageSource;
import io.dataease.i18n.Translator;
import io.dataease.utils.LocalModelUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Regression {
    private static int passed;
    private static final ObjectMapper JSON = new ObjectMapper();
    interface Work { void run() throws Exception; }
    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
        passed++;
        System.out.println("PASS " + name);
    }
    private static void denied(Work work, String name) throws Exception {
        try { work.run(); } catch (java.io.IOException e) {
            check(e.getMessage().contains("Excel"), name);
            return;
        }
        throw new AssertionError(name);
    }
    private static String configuration(Path path) throws Exception {
        return JSON.writeValueAsString(List.of(Map.of("path", path.toString(),
                "deTableName", "test", "tableName", "test", "fields", List.of(Map.of("name", "value")))));
    }
    public static void main(String[] args) throws Exception {
        var messages = new DeReloadableResourceBundleMessageSource();
        messages.setBasename("classpath:i18n/core");
        new Translator().setMessageSource(messages);
        var model = LocalModelUtils.class.getDeclaredField("modelValue");
        model.setAccessible(true);
        model.set(null, "standalone");
        Path work = Files.createTempDirectory("excel-file-guard-");
        Path root = Files.createDirectory(work.resolve("excel"));
        Path owned = ExcelFileGuard.uploadDirectory(root, "101");
        Path other = ExcelFileGuard.uploadDirectory(root, "202");
        String name = UUID.randomUUID() + ".csv";
        Path ownFile = Files.writeString(owned.resolve(name), "value\n35\n");
        Path otherFile = Files.writeString(other.resolve(name), "value\n77\n");
        Path legacy = Files.writeString(root.resolve(name), "value\n88\n");
        Path outside = Files.writeString(work.resolve(name), "value\n99\n");
        try {
            ExcelFileGuard.validateConfiguration(root, configuration(ownFile), null, "101");
            check(true, "new upload belongs to current user");
            denied(() -> ExcelFileGuard.validateConfiguration(root, configuration(otherFile), null, "101"), "other user's upload rejected");
            denied(() -> ExcelFileGuard.validateConfiguration(root, configuration(legacy), null, "101"), "new datasource cannot claim legacy file");
            ExcelFileGuard.validateConfiguration(root, configuration(legacy), configuration(legacy), "101");
            check(true, "authorized existing legacy reference retained");
            ExcelFileGuard.validateConfiguration(root, configuration(otherFile), configuration(otherFile), "101");
            check(true, "authorized collaborator may retain stored file");
            denied(() -> ExcelFileGuard.validateConfiguration(root, configuration(otherFile), configuration(legacy), "101"), "existing datasource cannot substitute other file");
            denied(() -> ExcelFileGuard.validateConfiguration(root, configuration(outside), configuration(outside), "101"), "legacy outside-root reference rejected");
            denied(() -> ExcelFileGuard.resolve(root, owned.resolve("..").resolve("202").resolve(name).toString()), "traversal rejected");
            denied(() -> ExcelFileGuard.resolve(root, "101/" + name), "relative path rejected");
            denied(() -> ExcelFileGuard.resolve(root, root + "-other/" + name), "prefix sibling rejected");
            denied(() -> ExcelFileGuard.resolve(root, owned.resolve("settings.csv").toString()), "non-upload filename rejected");
            denied(() -> ExcelFileGuard.resolve(root, ownFile + ".txt"), "unsupported extension rejected");
            denied(() -> ExcelFileGuard.resolve(root, ownFile + "\0"), "NUL rejected");
            denied(() -> ExcelFileGuard.uploadDirectory(root, "../202"), "forged upload owner rejected");
            Path symlink = root.resolve(UUID.randomUUID() + ".csv");
            Files.createSymbolicLink(symlink, outside);
            denied(() -> ExcelFileGuard.open(root, symlink.toString()), "file symlink rejected at read");
            Path alias = root.resolve("303");
            Files.createSymbolicLink(alias, other);
            denied(() -> ExcelFileGuard.resolve(root, alias.resolve(name).toString()), "directory symlink rejected");
            denied(() -> ExcelFileGuard.uploadDirectory(root, "303"), "symlink upload directory rejected");
            try (var input = ExcelFileGuard.open(root, ownFile.toString())) {
                check(new String(input.readAllBytes()).contains("35"), "legitimate file read");
            }
            var field = ExcelUtils.class.getDeclaredField("path");
            field.setAccessible(true);
            field.set(null, root.toString() + "/");
            var dto = new DatasourceDTO();
            dto.setType("Excel");
            dto.setConfiguration(configuration(ownFile));
            var request = new DatasourceRequest();
            request.setDatasource(dto);
            request.setTable("test");
            check(new ExcelUtils().fetchDataList(request).get(0)[0].equals("35"), "actual Excel CSV reader");
            dto.setConfiguration(configuration(outside));
            try {
                new ExcelUtils().fetchDataList(request);
                throw new AssertionError("reader allowed outside file");
            } catch (io.dataease.exception.DEException expected) {
                check(true, "actual reader blocks arbitrary server file");
            }
        } finally {
            try (var files = Files.walk(work)) {
                for (Path file : files.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(file);
            }
        }
        System.out.println("RESULT PASS=" + passed);
    }
}
