package io.dataease;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;

/**
 * 研发使用，请勿提交
 */
public class MybatisPlusGenerator {
    /**
     * 为什么不从配置文件读？
     * 第一 我嫌麻烦
     * 第二 后面配置会放到nacos读起来更麻烦了
     */
    private static final String url = "jdbc:mysql://localhost:3306/de_standalone?autoReconnect=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false";
    private static final String username = "root";
    private static final String password = "Password123@mysql";

    /**
     * 业务模块例如datasource,dataset,panel等
     */
    private static final String busi = "share";
    /**
     * 这是要生成代码的表名称
     */
    private static final String TABLE_NAME = "xpack_share";

    /**
     * 下面两个配置基本上不用动
     */
    private static final String codeDir = "src/main/java/";

    private static final String AUTO_DAO = ".dao.auto";


    public static void main(String[] args) throws Exception{

        String path = System.getProperty("java.class.path");
        path = path.substring(0, path.indexOf("target/classes"));
        String packageName = packageName() + "." + busi + AUTO_DAO;
        String outPath = path + codeDir;
        DataSourceConfig.Builder dsc = new DataSourceConfig.Builder(url, username, password);
        dsc.typeConvert( MySqlTypeConvert.INSTANCE);
        FastAutoGenerator.create(dsc)
                .globalConfig(builder -> {
                    builder.author("fit2cloud").outputDir(outPath);
                })
                .packageConfig(builder -> {
                    builder.parent(packageName);
                })
                .strategyConfig(builder -> {
                    builder.addInclude(TABLE_NAME).entityBuilder().enableFileOverride().mapperBuilder().mapperAnnotation(org.apache.ibatis.annotations.Mapper.class).enableFileOverride(); //设置需要生成的表名
                })
                .templateConfig(builder -> {
                    builder.disable(TemplateType.CONTROLLER).disable(TemplateType.SERVICE).disable(TemplateType.SERVICE_IMPL).disable(TemplateType.XML).build();
                })
                .execute();
    }

    private static String packageName() {
        return new Object() {
            public String getPackageName() {
                String packageName = this.getClass().getPackageName();
                return packageName;
            }
        }.getPackageName();
    }
}
