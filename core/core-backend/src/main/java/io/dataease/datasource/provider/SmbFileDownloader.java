package io.dataease.datasource.provider;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.protocol.commons.socket.ProxySocketFactory;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import io.dataease.api.ds.vo.ExcelConfiguration;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.utils.RemoteTransfer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Downloads a single file; credentials are deliberately not accepted in the URL. */
public final class SmbFileDownloader {
    private SmbFileDownloader() {
    }

    public static Map<String, String> download(ExcelConfiguration configuration, Path directory) {
        URI uri;
        String[] segments;
        try {
            uri = URI.create(configuration.getUrl().trim());
            String remotePath = uri.getPath();
            if (!"smb".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || uri.getPort() == 0 || uri.getPort() > 65535 || remotePath == null
                    || remotePath.indexOf('\\') >= 0 || remotePath.chars().anyMatch(Character::isISOControl)) {
                throw new IllegalArgumentException();
            }
            segments = remotePath.substring(1).split("/", -1);
            if (segments.length < 2 || Arrays.stream(segments).anyMatch(s -> s.isEmpty() || s.equals(".")
                    || s.equals("..") || s.contains(":") || s.contains("*") || s.contains("?"))) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException e) {
            DEException.throwException(Translator.get("i18n_smb_invalid_address"));
            return Map.of();
        }
        String fileName = segments[segments.length - 1];
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!Arrays.asList("csv", "xls", "xlsx").contains(suffix)) {
            DEException.throwException(Translator.get("i18n_unsupported_file_format"));
        }
        String username = configuration.getUserName();
        if (username == null || username.isBlank()) {
            DEException.throwException(Translator.get("i18n_smb_username_required"));
        }
        char[] password = configuration.getPasswd() == null ? new char[0] : configuration.getPasswd().toCharArray();
        long timeout = RemoteTransfer.timeoutMillis(configuration.getTransferTimeoutSeconds());
        long maxBytes = RemoteTransfer.sizeBytes(configuration.getMaxFileSizeMb(), 100, 1024);
        SmbConfig config = SmbConfig.builder()
                .withSocketFactory(new ProxySocketFactory((int) Math.min(10000, timeout)))
                .withTimeout(Math.min(60000, timeout), TimeUnit.MILLISECONDS)
                .withSoTimeout((int) Math.min(60000, timeout), TimeUnit.MILLISECONDS)
                .withSigningRequired(true)
                .withDfsEnabled(false)
                .build();
        Path localFile = null;
        try (SMBClient client = new SMBClient(config);
             RemoteTransfer transfer = new RemoteTransfer(
                     maxBytes, timeout, client::close);
             Connection connection = client.connect(uri.getHost(), uri.getPort() < 0 ? 445 : uri.getPort());
             Session session = connection.authenticate(new AuthenticationContext(username, password,
                     configuration.getDomain() == null ? "" : configuration.getDomain().trim()));
             DiskShare share = (DiskShare) session.connectShare(segments[0]);
             com.hierynomus.smbj.share.File remoteFile = share.openFile(
                     String.join("\\", Arrays.copyOfRange(segments, 1, segments.length)),
                     EnumSet.of(AccessMask.GENERIC_READ), EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                     EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ), SMB2CreateDisposition.FILE_OPEN,
                     EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE))) {
            Files.createDirectories(directory);
            localFile = Files.createTempFile(directory, "smb-", "." + suffix);
            try (InputStream input = transfer.wrap(remoteFile.getInputStream()); OutputStream output = Files.newOutputStream(localFile)) {
                input.transferTo(output);
            }
        } catch (Exception e) {
            // Do not propagate server messages containing paths or authentication details to logs/UI.
            if (localFile != null) {
                try {
                    Files.deleteIfExists(localFile);
                } catch (Exception ignored) {
                    // Preserve the original download error.
                }
            }
            DEException.throwException(Translator.get("i18n_smb_download_failed"));
        } finally {
            Arrays.fill(password, '\0');
        }
        return Map.of("fileName", fileName, "tranName", localFile.getFileName().toString());
    }
}
