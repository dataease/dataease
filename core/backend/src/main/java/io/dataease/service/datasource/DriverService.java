package io.dataease.service.datasource;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.Gson;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.DeFileUtils;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.commons.utils.Md5Utils;
import io.dataease.dto.DriverDTO;
import io.dataease.dto.SysLogDTO;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DeDriverDetailsMapper;
import io.dataease.plugins.common.base.mapper.DeDriverMapper;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.provider.DefaultJdbcProvider;
import io.dataease.plugins.datasource.provider.ExtendedJdbcClassLoader;
import io.dataease.provider.ProviderFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Transactional(rollbackFor = Exception.class)
@Service
public class DriverService {

    private static String DRIVER_PATH = "/opt/dataease/custom-drivers/";
    @Resource
    private DeDriverMapper deDriverMapper;
    @Resource
    private DeDriverDetailsMapper deDriverDetailsMapper;
    @Resource
    private DatasourceService datasourceService;


    public List<DriverDTO> list() throws Exception {
        List<DriverDTO> driverDTOS = new ArrayList<>();
        deDriverMapper.selectByExample(null).forEach(deDriver -> {
            DriverDTO driverDTO = new DriverDTO();
            BeanUtils.copyBean(driverDTO, deDriver);
            datasourceService.types().forEach(dataSourceType -> {
                if (dataSourceType.getType().equalsIgnoreCase(deDriver.getType())) {
                    driverDTO.setTypeDesc(dataSourceType.getName());
                }

            });
            driverDTOS.add(driverDTO);
        });
        return driverDTOS;
    }

    public void delete(DeDriver deDriver) {
        for (Datasource datasource : datasourceService.listByType(deDriver.getType())) {
            JdbcConfiguration configuration = new Gson().fromJson(datasource.getConfiguration(), JdbcConfiguration.class);
            if(StringUtils.isNotEmpty(configuration.getCustomDriver()) && configuration.getCustomDriver().equalsIgnoreCase(deDriver.getId())){
                throw new RuntimeException(Translator.get("I18N_DRIVER_NOT_DELETE"));
            }
        }
        deDriverMapper.deleteByPrimaryKey(deDriver.getId());
        DeDriverDetailsExample example = new DeDriverDetailsExample();
        example.createCriteria().andDeDriverIdEqualTo(deDriver.getId());
        deDriverDetailsMapper.deleteByExample(example);
        DeFileUtils.deleteFile(DRIVER_PATH + deDriver.getId() + "/");
    }

    public DeDriver save(DeDriver deDriver) {
        if(StringUtils.isEmpty(deDriver.getName()) || StringUtils.isEmpty(deDriver.getType())){
            throw new RuntimeException("Name or Type cannot be empty.");
        }
        DeDriverExample example = new DeDriverExample();
        example.createCriteria().andNameEqualTo(deDriver.getName());
        if(CollectionUtil.isNotEmpty(deDriverMapper.selectByExample(example))){
            throw new RuntimeException(Translator.get("I18N_DRIVER_REPEAT_NAME"));
        }

        deDriver.setCreateTime(System.currentTimeMillis());
        deDriver.setId(UUID.randomUUID().toString());
        deDriverMapper.insert(deDriver);
        return deDriver;
    }

    public DeDriver update(DeDriver deDriver) {
        deDriverMapper.updateByPrimaryKey(deDriver);
        return deDriver;
    }


    public List<DeDriverDetails> listDriverDetails(String driverId) {
        DeDriverDetailsExample example = new DeDriverDetailsExample();
        example.createCriteria().andDeDriverIdEqualTo(driverId);
        return deDriverDetailsMapper.selectByExample(example);
    }

    public void deleteDriverFile(String driverFileId) throws Exception{
        DeDriverDetails deDriverDetails = deDriverDetailsMapper.selectByPrimaryKey(driverFileId);
        DeDriver deDriver = deDriverMapper.selectByPrimaryKey(deDriverDetails.getDeDriverId());
        if(deDriver == null){
            throw new Exception(Translator.get("I18N_DRIVER_NOT_FOUND"));
        }
        if(deDriverDetails.getIsTransName() == null || !deDriverDetails.getIsTransName()){
            DeFileUtils.deleteFile(DRIVER_PATH + deDriverDetails.getDeDriverId() + "/" + deDriverDetails.getFileName());
        }else {
            DeFileUtils.deleteFile(DRIVER_PATH + deDriverDetails.getDeDriverId() + "/" + deDriverDetails.getTransName());
        }
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DRIVER_FILE, deDriverDetails.getId(), deDriverDetails.getDeDriverId(), null, null);
        DeLogUtils.save(sysLogDTO);
        deDriverDetailsMapper.deleteByPrimaryKey(driverFileId);
        DefaultJdbcProvider defaultJdbcProvider = (DefaultJdbcProvider)ProviderFactory.getProvider(deDriver.getType());
        defaultJdbcProvider.reloadCustomJdbcClassLoader(deDriver);
    }

    public DeDriverDetails saveJar(MultipartFile file, String driverId) throws Exception {
        DeDriver deDriver = deDriverMapper.selectByPrimaryKey(driverId);
        if(deDriver == null){
            throw new Exception(Translator.get("I18N_DRIVER_NOT_FOUND"));
        }
        String filename = file.getOriginalFilename();
        if(!filename.endsWith(".jar")){
            throw new Exception(Translator.get("I18N_NOT_JAR"));
        }
        String dirPath = DRIVER_PATH + driverId + "/";
        String filePath = dirPath + Md5Utils.md5(filename) + ".jar";

        saveFile(file, dirPath, filePath);
        List<String> jdbcList = new ArrayList<>();
        String version = "";

        DeDriverDetails deDriverDetails = new DeDriverDetails();
        deDriverDetails.setId(UUID.randomUUID().toString());
        deDriverDetails.setDeDriverId(driverId);
        deDriverDetails.setVersion(version);
        deDriverDetails.setFileName(filename);
        deDriverDetails.setDriverClass(String.join(",", jdbcList));
        deDriverDetails.setIsTransName(true);
        deDriverDetails.setTransName(Md5Utils.md5(filename) + ".jar");

        DeDriverDetailsExample deDriverDetailsExample = new DeDriverDetailsExample();
        deDriverDetailsExample.createCriteria().andDeDriverIdEqualTo(driverId).andFileNameEqualTo(filename);
        if(CollectionUtil.isNotEmpty(deDriverDetailsMapper.selectByExample(deDriverDetailsExample))){
            throw new Exception("A file with the same name already exists：" + filename);
        }

        deDriverDetailsMapper.insert(deDriverDetails);
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.UPLOADFILE, SysLogConstants.SOURCE_TYPE.DRIVER_FILE, deDriverDetails.getId(), driverId, null, null);
        DeLogUtils.save(sysLogDTO);
        DefaultJdbcProvider defaultJdbcProvider = (DefaultJdbcProvider)ProviderFactory.getProvider(deDriver.getType());
        defaultJdbcProvider.reloadCustomJdbcClassLoader(deDriver);
        return deDriverDetails;
    }

    private List<String> getClassNameFrom(String jarName) {
        List<String> fileList = new ArrayList<String>();
        try {
            JarFile jarFile = new JarFile(new File(jarName));
            Enumeration<JarEntry> en = jarFile.entries(); // 枚举获得JAR文件内的实体,即相对路径
            while (en.hasMoreElements()) {
                String name1 = en.nextElement().getName();
                if (!name1.endsWith(".class")) {//不是class文件
                    continue;
                }
                String name2 = name1.substring(0, name1.lastIndexOf(".class"));
                String name3 = name2.replaceAll("/", ".");
                fileList.add(name3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public boolean isChildClass(String className, Class parentClazz, ExtendedJdbcClassLoader extendedJdbcClassLoader) {
        if (className == null) return false;

        Class clazz = null;
        try {
            clazz = extendedJdbcClassLoader.loadClass(className);
            if (Modifier.isAbstract(clazz.getModifiers())) {//抽象类忽略
                return false;
            }
            if (Modifier.isInterface(clazz.getModifiers())) {//接口忽略
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return parentClazz.isAssignableFrom(clazz);
    }

    private String classVersion(ExtendedJdbcClassLoader extendedJdbcClassLoader, String className) throws Exception {
        Class clazz = extendedJdbcClassLoader.loadClass(className);
        return clazz.getPackage().getImplementationVersion();
    }

    private String saveFile(MultipartFile file, String dirPath, String filePath) throws Exception {
        File p = new File(dirPath);
        if (!p.exists()) {
            p.mkdirs();
        }
        File f = new File(filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
        return filePath;
    }


}
