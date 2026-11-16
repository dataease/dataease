package io.dataease.visualization.manage;

import io.dataease.api.material.vo.MaterialBundle;
import io.dataease.api.material.vo.MaterialBundleEntry;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.result.ResultCode;
import io.dataease.utils.ImageContentGuard;
import io.dataease.utils.LogUtil;
import io.dataease.utils.MaterialDocuments;
import io.dataease.utils.StaticResourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.dataease.constant.StaticResourceConstants.UPLOAD_URL_PREFIX;
import static io.dataease.constant.StaticResourceConstants.WORK_DIR;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

@Component
public class ImportedImageManage {
    private static final int MAX_FILES = 200;
    private static final long MAX_TOTAL_BYTES = 200L * 1024 * 1024;
    private static final long MAX_ENCODED_BYTES = (ImageContentGuard.MAX_BYTES + 2L) / 3 * 4;
    private static final String PACKAGE_REFERENCE_PREFIX = "material-file://";
    private static final Pattern PACKAGE_REFERENCE = Pattern.compile(Pattern.quote(PACKAGE_REFERENCE_PREFIX) + "[1-9][0-9]{0,2}");
    private static final Set<String> FORMATS = Set.of("png", "jpg", "jpeg", "gif", "svg");

    private Path directory = Path.of(WORK_DIR);

    public void validate(MaterialBundle bundle) {
        decode(bundle, Set.of());
    }

    public void validatePreview(MaterialBundle bundle, String style, String components) {
        validate(bundle);
        if (!images(style, components).equals(bundle.getFiles().stream()
                .map(MaterialBundleEntry::getReference).collect(Collectors.toSet()))) {
            throw invalid();
        }
    }

    public Set<String> imageReferences(String style, String components) {
        return images(style, components).stream()
                .filter(image -> image.startsWith(PACKAGE_REFERENCE_PREFIX)).collect(Collectors.toSet());
    }

    private Set<String> images(String style, String components) {
        try {
            return MaterialDocuments.images(style, components);
        } catch (IllegalArgumentException e) {
            throw invalid();
        }
    }

    public Map<String, String> restore(MaterialBundle bundle, Set<String> imageReferences) {
        List<ImportedImage> images = decode(bundle, imageReferences);
        if (images.isEmpty()) {
            return Map.of();
        }

        List<Path> written = new ArrayList<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    deleteFiles(written);
                }
            }
        });

        Map<String, String> mapping = new LinkedHashMap<>();
        try {
            Files.createDirectories(directory);
            for (ImportedImage image : images) {
                String name = UUID.randomUUID() + "." + image.format();
                Path file = StaticResourceUtils.resolveResourcePath(directory, name);
                try (var output = Files.newOutputStream(file, CREATE_NEW, WRITE, NOFOLLOW_LINKS)) {
                    written.add(file);
                    output.write(image.content());
                }
                mapping.put(image.reference(), "/" + UPLOAD_URL_PREFIX + "/" + name);
            }
        } catch (IOException e) {
            deleteFiles(written);
            LogUtil.error("Failed to store imported images", e);
            throw new DEException(ResultCode.SYSTEM_INNER_ERROR.code(), Translator.get("i18n_imported_image_write_failed"));
        }
        return mapping;
    }

    private List<ImportedImage> decode(MaterialBundle bundle, Set<String> selected) {
        if (bundle == null || bundle.getVersion() != 1 || bundle.getFiles() == null
                || bundle.getFiles().size() > MAX_FILES) {
            throw invalid();
        }
        List<ImportedImage> images = new ArrayList<>();
        Set<String> references = new HashSet<>();
        long total = 0;
        for (MaterialBundleEntry entry : bundle.getFiles()) {
            if (entry == null || entry.getReference() == null
                    || !PACKAGE_REFERENCE.matcher(entry.getReference()).matches()
                    || !references.add(entry.getReference()) || !validName(entry.getName())
                    || entry.getContent() == null || entry.getContent().length() > MAX_ENCODED_BYTES) {
                throw invalid();
            }
            String format = entry.getFormat() == null ? "" : entry.getFormat().toLowerCase(Locale.ROOT);
            if (!FORMATS.contains(format)) {
                throw invalid();
            }
            if ("jpeg".equals(format)) {
                format = "jpg";
            }
            byte[] content;
            try {
                content = Base64.getDecoder().decode(entry.getContent());
                total += content.length;
                if (total > MAX_TOTAL_BYTES) {
                    throw invalid();
                }
                ImageContentGuard.validateMaterial("image." + format, content);
            } catch (Exception e) {
                throw invalid();
            }
            if (selected.contains(entry.getReference())) {
                images.add(new ImportedImage(entry.getReference(), format, content));
            }
        }
        if (!references.containsAll(selected)) {
            throw invalid();
        }
        return images;
    }

    private boolean validName(String name) {
        return name != null && !name.isBlank() && name.length() <= 255
                && name.chars().noneMatch(Character::isISOControl)
                && !name.contains("/") && !name.contains("\\") && !name.contains("..");
    }

    private void deleteFiles(List<Path> files) {
        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                LogUtil.error("Failed to remove an imported image after rollback", e);
            }
        }
    }

    private DEException invalid() {
        return new DEException(ResultCode.PARAM_IS_INVALID.code(), Translator.get("i18n_static_resource_invalid"));
    }

    private record ImportedImage(String reference, String format, byte[] content) { }
}
