import java.sql.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
public class LiveMysqlFixture {
    public static void main(String[] args) throws Exception {
        Path file=Path.of(args[1]);
        String rootUrl=System.getenv("LOCAL_DB_URL");
        try(Connection c=DriverManager.getConnection(rootUrl,System.getenv("LOCAL_DB_USERNAME"),System.getenv("LOCAL_DB_PASSWORD"));Statement s=c.createStatement()) {
            if(args[0].equals("cleanup")) {
                if(!Files.exists(file))return;
                var x=new ObjectMapper().readTree(Files.readString(file));String db=x.path("dataBase").asText(),user=x.path("username").asText();
                if(!db.matches("de_security_[a-f0-9]{12}")||!user.matches("de_sec_[a-f0-9]{12}"))throw new IllegalArgumentException("Not a test fixture");
                s.execute("DROP DATABASE IF EXISTS `"+db+"`");s.execute("DROP USER IF EXISTS '"+user+"'@'%'");Files.delete(file);System.out.println("Fixture cleanup complete");return;
            }
            if(Files.exists(file))throw new IllegalStateException("Existing fixture must be cleaned first");
            String suffix=UUID.randomUUID().toString().replace("-","").substring(0,12),db="de_security_"+suffix,user="de_sec_"+suffix;
            String pwd=UUID.randomUUID().toString().replace("-","");
            URI uri=URI.create(rootUrl.substring(5));
            Map<String,Object>x=new LinkedHashMap<>();x.put("host",uri.getHost());x.put("port",uri.getPort()<0?3306:uri.getPort());x.put("dataBase",db);x.put("username",user);x.put("password",pwd);
            Files.writeString(file,new ObjectMapper().writeValueAsString(x));Files.setPosixFilePermissions(file,PosixFilePermissions.fromString("rw-------"));
            s.execute("CREATE DATABASE `"+db+"`");
            s.execute("CREATE TABLE `"+db+"`.sales(id INT PRIMARY KEY,category VARCHAR(20),amount INT)");
            s.execute("INSERT INTO `"+db+"`.sales VALUES(1,'A',10),(2,'A',20),(3,'B',30)");
            s.execute("CREATE USER '"+user+"'@'%' IDENTIFIED BY '"+pwd+"'");
            s.execute("GRANT SELECT ON `"+db+"`.* TO '"+user+"'@'%'");
            System.out.println("Isolated MySQL fixture created: 3 synthetic rows; read-only account");
        }
    }
}
