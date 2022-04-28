package io.dataease.commons.constants;

import java.io.File;

import static io.dataease.commons.utils.StaticResourceUtils.ensureSuffix;

/**
 * Author: wangjiahao
 * Date: 2022/4/28
 * Description:
 */
public class StaticResourceConstants {

    public static final String FILE_PROTOCOL = "file://";

    public static final String FILE_SEPARATOR = File.separator;

    public static final String USER_HOME = "/opt/dataease/data";

    public static String WORK_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "static-resource" + FILE_SEPARATOR;

    /**
     * Upload prefix.
     */
    public final static String UPLOAD_URL_PREFIX = "static-resource";

    /**
     * url separator.
     */
    public static final String URL_SEPARATOR = "/";

}
