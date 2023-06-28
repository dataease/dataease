package io.dataease;


import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.type.CK;
import io.dataease.datasource.type.Mysql;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.calcite.sql.SqlKind.*;

//@SpringBootTest
public class RsaTest {

    private static final String PK_SEPARATOR = "-pk_separator-";
    private static final String SubstitutedParams = "DATAEASE_PATAMS_BI";
    private static final String SubstitutedSql = " 'DE-BI' = 'DE-BI' ";

    @Test
    public void test() {
        RSA rsa = new RSA();
        String privateKeyBase64 = rsa.getPrivateKeyBase64();
        String publicKeyBase64 = rsa.getPublicKeyBase64();
        LogUtil.info("private is {}", privateKeyBase64);
        LogUtil.info("public is {}", publicKeyBase64);
        String data = "my name is cyw";
        String s = rsa.encryptBase64(data, KeyType.PublicKey);
        String s1 = rsa.decryptStr(s, KeyType.PrivateKey);
        LogUtil.info(s1);
    }

    @Test
    public void test1() {
        String separator = Base64Utils.encodeToUrlSafeString(PK_SEPARATOR.getBytes(StandardCharsets.UTF_8));
        String a = Base64Utils.encodeToString(PK_SEPARATOR.getBytes(StandardCharsets.UTF_8));
//        System.out.println(separator);
//        System.out.println(a);
        String aesKey = "0251434c0eca47977c50ef044f278cb9";
        String pk = "8OT02q8aALvMjvsr2Ws2PobmKVVgg3UjNN9r1jUKVilJMtn3fYAfe1oOozM3TTqxV9JmRA1MhV6Q/FoUfYN3mBIL4cDSYbEPSO+IRGTq4FmLZdBHMvUZilliMpt5+0vxXF43K3dZT3WStsK5usLfTKCcqZLLMZs0Wqo98XbEpgMZBMP+bKrolcpUfY06dwVqvgX9CN4ujYd8aTVXEq4/iVjX3opBGsbT8aD3Fb11ThXfAiQaUghsuT17NIfoV/P0uATIITQsrfmOkERDSmCz3wE0MfJVC7HQn9n8GXNkuOY=";
        byte[] bytes = HexUtil.decodeHex(aesKey);
        AES aes = new AES(bytes);

        String s = aes.decryptStr(pk);

//        System.out.println(s);


    }


    @Test
    public void testaaa() {
        String sql = "SELECT FROM_UNIXTIME(round(time_stamp/1000,0)) as time_stamp,\n" +
                "name, \n" +
                "num\n" +
                "FROM aaaa where name in (select name from aaaa where bb=DATAEASE_PATAMS_BI)";
//        String sql = "SELECT * FROM sys_user where user_id > 0 and gender='男' and enabled=0 or is_admin=1 and is_admin2=1";
//        String sql = "select * from emps e where e.deptno = (select d.deptno from depts d where d.deptno = e.deptno )";
        // 在解析前可以对 SQL 语句进行预处理，比如将不支持的 && 替换为 AND， != 替换为 <>
        SqlParser.Config config =
                SqlParser.configBuilder()
//                        .setParserFactory(FlinkSqlParserImpl.FACTORY)
                        .setLex(Lex.JAVA)
                        .setIdentifierMaxLength(256)
                        .build();
        // 创建解析器
        SqlParser sqlParser = SqlParser
                .create(sql, config);
        // 生成 AST 语法树
        SqlNode sqlNode;
        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            throw new RuntimeException("使用 Calcite 进行语法分析发生了异常", e);
        }

        System.out.println("---");
        // 递归遍历语法树
        StringBuilder stringBuilder = new StringBuilder();
        getDependencies(sqlNode, false);

        System.out.println(sqlNode.toString());

    }

    private void getDependencies(SqlNode sqlNode, Boolean fromOrJoin) {

        if (sqlNode.getKind() == JOIN) {
            SqlJoin sqlKind = (SqlJoin) sqlNode;
            getDependencies(sqlKind.getLeft(), true);
            getDependencies(sqlKind.getRight(), true);
        } else if (sqlNode.getKind() == IDENTIFIER) {

            if (fromOrJoin) {
                // 获取 source 表名

            }
        } else if (sqlNode.getKind() == AS) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
            if (sqlKind.getOperandList().size() >= 2) {
                getDependencies(sqlKind.getOperandList().get(0), fromOrJoin);
            }
        } else if (sqlNode.getKind() == SELECT) {
            SqlSelect sqlKind = (SqlSelect) sqlNode;
            List<SqlNode> list = sqlKind.getSelectList().getList();
            for (SqlNode i : list) {
                getDependencies(i, false);
            }
            SqlNode newWhere = sqlKind.getWhere().accept(getSqlShuttle());
            sqlKind.setWhere(newWhere);
            getDependencies(sqlKind.getFrom(), true);
        } else {

            // TODO 这里可根据需求拓展处理其他类型的 sqlNode
        }
    }

    SqlShuttle getSqlShuttle() {
        return new SqlShuttle() {

            @Override
            public @Nullable SqlNode visit(final SqlCall call) {

                CallCopyingArgHandler argHandler = new CallCopyingArgHandler(call, false);
                call.getOperator().acceptCall(this, call, false, argHandler);
                if (argHandler.result().toString().contains(SubstitutedParams)) {
                    SqlNode sqlNode1 = null;
                    try {
                        sqlNode1 = SqlParser.create(SubstitutedSql).parseExpression();
                    } catch (Exception e) {

                    }
                    return sqlNode1;
                }
                return argHandler.result();
            }


        };
    }


    @Test
    public void testCalcite() throws Exception{
        CalciteProvider provider = new CalciteProvider();
        provider.init();
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        CoreDatasource coreDatasource = new CoreDatasource();
        coreDatasource.setType("ck");

        CK mysql = new CK();
        mysql.setUsername("dataease");
        mysql.setPassword("Calong@2015");
        mysql.setHost("123.56.8.132");
        mysql.setPort(8123);
        mysql.setDataBase("dataease");
        coreDatasource.setConfiguration(JsonUtil.toJSONString(mysql).toString());
        datasourceRequest.setDatasource(coreDatasource);
        provider.checkStatus(datasourceRequest);
    }

}
