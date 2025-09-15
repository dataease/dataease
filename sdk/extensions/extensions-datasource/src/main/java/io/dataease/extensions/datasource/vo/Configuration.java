package io.dataease.extensions.datasource.vo;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Configuration {
    private String type;
    private String name;
    private String catalog;
    private String catalogDesc;
    private String extraParams;
    private String keywordPrefix = "";
    private String keywordSuffix = "";
    private String aliasPrefix = "";
    private String aliasSuffix = "";
    protected String jdbc;
    private String host;
    private String jdbcUrl;
    private String urlType;
    private Integer port;
    private String username;
    private String password;
    private String dataBase;
    private String schema;
    private String customDriver = "default";
    private String authMethod = "passwd";
    private String connectionType;
    private String charset;
    private String targetCharset;
    private String driver;
    private int initialPoolSize = 5;
    private int minPoolSize = 5;
    private int maxPoolSize = 50;
    private int queryTimeout = 30;
    private boolean useSSH = false;
    private String sshHost;
    private Integer sshPort;
    private Integer lPort;
    private String sshUserName;
    private String sshType = "password";
    private String sshPassword;
    private String sshKey;
    private String sshKeyPassword;


    public String getLHost(){
        if(useSSH){
            return "127.0.0.1";
        }else {
            return this.host;
        }
    }

    public Integer getLPort(){
        if(useSSH && lPort != null){
            return lPort;
        }else {
            return this.port;
        }
    }

    protected static final Pattern HOST_PORT_PATTERN = Pattern.compile("//([^:/]+)(?::(\\d+))?");
    protected static final Pattern PARAMETERS_PATTERN = Pattern.compile("([^&=]+)=([^&]*)");
    private static final Pattern DB_NAME_PATTERN = Pattern.compile("//[^/]+/([^?]+)");
    private Map<String, String> parameters;
    protected void parseHostAndPort(String jdbcUrl) {
        Matcher matcher = HOST_PORT_PATTERN.matcher(jdbcUrl);
        if (matcher.find()) {
            setHost(matcher.group(1));
            if (matcher.group(2) != null) {
                setPort(Integer.parseInt(matcher.group(2)));
            }
        }
    }

    protected void parseParameters(String jdbcUrl) {
        int paramStart = jdbcUrl.indexOf('?');
        if (paramStart > 0) {
            String paramString = jdbcUrl.substring(paramStart + 1);
            Matcher matcher = PARAMETERS_PATTERN.matcher(paramString);
            while (matcher.find()) {
                parameters.put(matcher.group(1), matcher.group(2));
            }
        }
    }

    protected void convertParameters(){
        if (ObjectUtils.isEmpty(parameters)) {
            return;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.equalsIgnoreCase(key, "user")) {
                setUsername(value);
            }
            if (StringUtils.equalsIgnoreCase(key, "password")) {
                setPassword(value);
            }
        }
    }

    protected void convertDatabase(String jdbcUrl) {
        Matcher matcher = getDatabasePattern().matcher(jdbcUrl);
        if (matcher.find()) {
            setDataBase(matcher.group(1));
        }
    }

    protected Pattern getDatabasePattern() {
        return DB_NAME_PATTERN;
    }

    public void convertJdbcUrl() {
        if (StringUtils.isNotBlank(urlType) && StringUtils.equalsAnyIgnoreCase(this.urlType, "jdbcUrl")) {
            parseHostAndPort(jdbcUrl);
            parseParameters(jdbcUrl);
            convertParameters();
            convertDatabase(jdbcUrl);
        }
    }

}
