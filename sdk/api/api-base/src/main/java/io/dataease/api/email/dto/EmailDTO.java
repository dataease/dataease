package io.dataease.api.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3599154848839436838L;

    private List<String> recipientList;

    private String from;

    private String subject;

    private byte[] content;

    private List<File> fileList;

    private List<EmailFile> emailFiles;

    public boolean existFile() {
        return CollectionUtils.isNotEmpty(fileList) || CollectionUtils.isNotEmpty(emailFiles);
    }
}
