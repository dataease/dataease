package io.dataease.api.communicate.dto;

import io.dataease.constant.MessageEnum;
import lombok.Data;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MessageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1499402825211940277L;

    private List<String> recipientList;

    private String title;

    private byte[] content;

    private List<File> fileList;

    private List<MessageFile> messageFileList;

    private MessageEnum messageEnum;
}
