package io.dataease.service.sys;

import com.google.gson.Gson;
import io.dataease.base.domain.MyPlugin;
import io.dataease.base.mapper.MyPluginMapper;
import io.dataease.base.mapper.ext.ExtSysPluginMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.DeFileUtils;
import io.dataease.commons.utils.ZipUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.plugins.config.LoadjarUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;


@Service
public class PluginService {

    @Value("${dataease.plugin.dir:/opt/dataease/plugins/}")
    private String pluginDir;

    private final static String pluginJsonName = "plugin.json";

    @Resource
    private ExtSysPluginMapper extSysPluginMapper;

    @Resource
    private MyPluginMapper myPluginMapper;

    @Resource
    private LoadjarUtil loadjarUtil;

    public List<MyPlugin> query(BaseGridRequest request) {
        GridExample gridExample = request.convertExample();
        List<MyPlugin> results = extSysPluginMapper.query(gridExample);
        return results;
    }

    /**
     * 从本地安装处插件
     * @param file
     * @return
     */
    public Map<String, Object> localInstall(MultipartFile file) {
        //1.上传文件到服务器pluginDir目录下
        File dest = DeFileUtils.upload(file, pluginDir+"temp/");
        //2.解压目标文件dest 得到plugin.json和jar
        String folder = pluginDir+"folder/";
        try {
            ZipUtils.upZipFile(dest, folder);
        } catch (IOException e) {
            // 需要删除文件
            e.printStackTrace();
        }
        //3.解析plugin.json 失败则 直接返回错误 删除文件
        File folderFile = new File(folder);
        File[] jsonFiles = folderFile.listFiles(this::isPluginJson);
        if (ArrayUtils.isEmpty(jsonFiles)) {
            throw new RuntimeException("缺少插件描述文件");
        }
        MyPlugin myPlugin = formatJsonFile(jsonFiles[0]);
        //4.加载jar包 失败则 直接返回错误 删除文件
        File[] jarFiles = folderFile.listFiles(this::isPluginJar);
        if (ArrayUtils.isEmpty(jarFiles)) {
            throw new RuntimeException("缺少插件jar文件");
        }
        File jarFile = jarFiles[0];
        String jarRoot = pluginDir+"jar/";
        String jarPath = null;
        try {
            jarPath = DeFileUtils.copy(jarFile, jarRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadjarUtil.loadJar(jarPath);
        //5.写表到my_plugin
        myPlugin.setPluginId(0L);
        myPluginMapper.insert(myPlugin);
        return null;
    }

    //判断当前文件是否实插件描述文件
    //文件名称必须plugin.json
    private boolean isPluginJson(File file) {
        return StringUtils.equals(file.getName(), pluginJsonName);
    }

    private boolean isPluginJar(File file) {
        String name = file.getName();
        return StringUtils.equals(DeFileUtils.getExtensionName(name), "jar");
    }

    /**
     * 从plugin.json文件反序列化为MyPlugin实例对象
     * @return
     */
    private MyPlugin formatJsonFile(File file) {
        String str = DeFileUtils.readJson(file);
        Gson gson = new Gson();
        Map<String, Object> myPlugin = gson.fromJson(str, Map.class);
        myPlugin.put("free", (Double)myPlugin.get("free") > 0.0);
        MyPlugin result = new MyPlugin();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(result, myPlugin);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //BeanUtils.copyBean(result, myPlugin);
        return  result;
    }

    /**
     * 从插件商城远程安装插件
     * 2.0版本实现
     * @param params
     * @return
     */
    public Map<String, Object> remoteInstall(Map<String, Object> params) {
        return null;
    }
}
