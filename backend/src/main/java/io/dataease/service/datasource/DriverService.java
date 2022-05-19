package io.dataease.service.datasource;

import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.DeFileUtils;
import io.dataease.dto.DriverDTO;
import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.base.domain.DeDriverDetails;
import io.dataease.plugins.common.base.domain.DeDriverDetailsExample;
import io.dataease.plugins.common.base.mapper.DeDriverDetailsMapper;
import io.dataease.plugins.common.base.mapper.DeDriverMapper;
import io.dataease.plugins.datasource.provider.ExtendedJdbcClassLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Transactional(rollbackFor = Exception.class)
@Service
public class DriverService {

    private static String DRIVER_PATH = "/opt/dataease/drivers/custom/";
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

    public void delete(String driverId) {
        deDriverMapper.deleteByPrimaryKey(driverId);

        DeDriverDetailsExample example = new DeDriverDetailsExample();
        example.createCriteria().andDeDriverIdEqualTo(driverId);
        deDriverDetailsMapper.deleteByExample(example);
        DeFileUtils.deleteFile(DRIVER_PATH + driverId + "/");
    }

    public DeDriver save(DeDriver deDriver) {
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
        return deDriverDetailsMapper.selectByExampleWithBLOBs(example);
    }

    public void deleteDriverFile(String driverFileId) {
        DeDriverDetails deDriverDetails = deDriverDetailsMapper.selectByPrimaryKey(driverFileId);
        DeFileUtils.deleteFile(DRIVER_PATH + deDriverDetails.getDeDriverId() + "/" + deDriverDetails.getFileName());
        deDriverDetailsMapper.deleteByPrimaryKey(driverFileId);
    }

    public void saveJar(MultipartFile file, String driverId) throws Exception {
        String filename = file.getOriginalFilename();
        String dirPath = DRIVER_PATH + driverId + "/";
        String filePath = dirPath + filename;

        saveFile(file, dirPath, filePath);
        List<String> jdbcList = new ArrayList<>();
        String version = "";
//        ExtendedJdbcClassLoader extendedJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(filePath).toURI().toURL()}, null);
//        for (String className : getClassNameFrom(filePath)) {
//            if (isChildClass(className, java.sql.Driver.class, extendedJdbcClassLoader)) {
//                jdbcList.add(className);
//                version = classVersion(extendedJdbcClassLoader, className);
//            }
//        }

        DeDriverDetails deDriverDetails = new DeDriverDetails();
        deDriverDetails.setId(UUID.randomUUID().toString());
        deDriverDetails.setDeDriverId(driverId);
        deDriverDetails.setVersion(version);
        deDriverDetails.setFileName(filename);
        deDriverDetails.setDriverClass(String.join(",", jdbcList));
        deDriverDetailsMapper.insert(deDriverDetails);
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
