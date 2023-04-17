package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.ds.DatasourceDriverApi;

import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.api.ds.vo.DriveDTO;
import io.dataease.api.ds.vo.DriveJarDTO;
import io.dataease.datasource.dao.auto.entity.CoreDriver;
import io.dataease.datasource.dao.auto.entity.CoreDriverJar;
import io.dataease.datasource.dao.auto.mapper.CoreDriverJarMapper;
import io.dataease.datasource.dao.auto.mapper.CoreDriverMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.FileUtils;
import io.dataease.utils.Md5Utils;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("/datasourceDriver")
public class DatasourceDriverServer implements DatasourceDriverApi {

    private final static String DRIVER_PATH = "/opt/dataease/custom-drivers/";

    @Resource
    private CoreDriverMapper coreDriverMapper;

    @Resource
    private CoreDriverJarMapper coreDriverJarMapper;

    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    @Override
    public List<DriveDTO> list() {
        List<DriveDTO> driveDTOS = new ArrayList<>();
        List<CoreDriver> coreDrivers = coreDriverMapper.selectList(null);
        coreDrivers.forEach(coreDriver -> {
            DriveDTO datasourceDrive = new DriveDTO();
            BeanUtils.copyBean(datasourceDrive, coreDriver);
            datasourceDrive.setTypeDesc(""); //TODO 设置数据源类型desc
        });
        return driveDTOS;
    }

    @Override
    public List<DriveDTO> listByDsType(String dsType) {
        List<DriveDTO> driveDTOS = new ArrayList<>();
        QueryWrapper<CoreDriver> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", dsType);
        List<CoreDriver> coreDrivers = coreDriverMapper.selectList(queryWrapper);
        coreDrivers.forEach(coreDriver -> {
            DriveDTO datasourceDrive = new DriveDTO();
            BeanUtils.copyBean(datasourceDrive, coreDriver);
        });
        return driveDTOS;
    }

    @Override
    public DriveDTO save(DriveDTO datasourceDrive){
        CoreDriver coreDriver = new CoreDriver();
        BeanUtils.copyBean(coreDriver, datasourceDrive);
        coreDriverMapper.insert(coreDriver);
        return datasourceDrive;
    }

    @Override
    public DriveDTO update(DriveDTO datasourceDrive){
        CoreDriver coreDriver = new CoreDriver();
        BeanUtils.copyBean(coreDriver, datasourceDrive);
        coreDriverMapper.updateById(coreDriver);
        return datasourceDrive;
    }

    @Override
    public void delete(String driverId){
        coreDriverMapper.deleteById(driverId);
        Map<String, Object> map = new HashMap<>();
        map.put("deDriverId", driverId);
        coreDriverJarMapper.deleteByMap(map);
    }


    @Override
    public List<DriveJarDTO> listDriverJar(String driverId){
        List<DriveJarDTO> driveJarDTOS = new ArrayList<>();
        QueryWrapper<CoreDriverJar> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deDriverId", driverId);
        coreDriverJarMapper.selectList(queryWrapper).forEach(coreDriverJar -> {
            DriveJarDTO driveJarDTO = new DriveJarDTO();
            BeanUtils.copyBean(driveJarDTO, coreDriverJar);
            driveJarDTOS.add(driveJarDTO);
        });
        return driveJarDTOS;
    }

    @Override
    public void deleteDriverJar(String jarId){
        CoreDriverJar driverJar = coreDriverJarMapper.selectById(jarId);
        coreDriverJarMapper.deleteById(jarId);
        CoreDriver driver = coreDriverMapper.selectById(driverJar.getDeDriverId());
        FileUtils.deleteFile(DRIVER_PATH + driverJar.getDeDriverId() + "/" + driverJar.getTransName());
        //TODO 更新classloader
    };

    @Override
    public DriveJarDTO uploadJar(@RequestParam("deDriverId") String deDriverId, @RequestParam("jarFile") MultipartFile jarFile) throws Exception{
        CoreDriver coreDriver = coreDriverMapper.selectById(deDriverId);
        if(coreDriver == null){
            throw new RuntimeException("DRIVER_NOT_FOUND");
        }
        String filename = jarFile.getOriginalFilename();
        if(!filename.endsWith(".jar")){
            throw new RuntimeException("NOT_JAR");
        }

        QueryWrapper<CoreDriverJar> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fileName", filename);
        if(!CollectionUtils.isEmpty(coreDriverJarMapper.selectList(queryWrapper))){
            throw new Exception("A file with the same name already exists：" + filename);
        }

        String dirPath = DRIVER_PATH + deDriverId + "/";
        String filePath = dirPath + Md5Utils.md5(filename) + ".jar";
        saveJarFile(jarFile, dirPath, filePath);

        CoreDriverJar coreDriverJar = new CoreDriverJar();
        coreDriverJar.setDeDriverId(deDriverId);
        coreDriverJar.setVersion("");
        coreDriverJar.setFileName(filename);
        coreDriverJar.setDriverClass(String.join(",", new ArrayList<>()));
        coreDriverJar.setIsTransName(true);
        coreDriverJar.setTransName(Md5Utils.md5(filename) + ".jar");
        coreDriverJarMapper.insert(coreDriverJar);
        //TODO 并更新classloader

        DriveJarDTO driveJarDTO = new DriveJarDTO();
        BeanUtils.copyBean(driveJarDTO, coreDriverJar);
        return driveJarDTO;
    }

    private String saveJarFile(MultipartFile file, String dirPath, String filePath) throws Exception {
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
