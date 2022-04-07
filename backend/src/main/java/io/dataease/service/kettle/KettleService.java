package io.dataease.service.kettle;

import com.google.gson.Gson;
import io.dataease.base.domain.DeEngine;
import io.dataease.base.domain.DeEngineExample;
import io.dataease.base.mapper.DeEngineMapper;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.ResultHolder;
import io.dataease.dto.KettleDTO;
import io.dataease.service.engine.EngineService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.checkerframework.checker.units.qual.K;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.util.HttpClientManager;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KettleService {

    @Resource
    private Environment env;
    @Resource
    private DeEngineMapper deEngineMapper;
    @Resource
    private EngineService engineService;

    public ResultHolder save(DeEngine kettle) throws Exception {
        try {
            validate(new Gson().fromJson(kettle.getConfiguration(), KettleDTO.class));
            kettle.setStatus("Success");
        }catch (Exception e){
            kettle.setStatus("Error");
        }

        if (StringUtils.isEmpty(kettle.getId())) {
            kettle.setId(UUID.randomUUID().toString());
            kettle.setType("kettle");
            deEngineMapper.insert(kettle);
        } else {
            deEngineMapper.updateByPrimaryKeyWithBLOBs(kettle);
        }
        return ResultHolder.success(kettle);
    }

    public void delete(String id){
        deEngineMapper.deleteByPrimaryKey(id);
    }

    public ResultHolder validate(KettleDTO kettleDTO) throws Exception {
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((kettleDTO.getUser()
                + ":" + kettleDTO.getPasswd()).getBytes());
        httpClientConfig.addHeader("Authorization", authValue);
        try {
            String response = HttpClientUtil.get("http://" + kettleDTO.getCarte() + ":" + kettleDTO.getPort() + "/kettle/status/", httpClientConfig);
            return ResultHolder.success("Kettle is valid.");
        }catch (Exception e){
            return ResultHolder.error("Kettle is invalid: " + e.getMessage());
        }
    }

    public ResultHolder validate(String id) {
        DeEngine kettle = deEngineMapper.selectByPrimaryKey(id);
        try {
            validate(new Gson().fromJson(kettle.getConfiguration(), KettleDTO.class));
            kettle.setStatus("Success");
            deEngineMapper.updateByPrimaryKeyWithBLOBs(kettle);
            return ResultHolder.success(kettle);
        }catch (Exception e){
            kettle.setStatus("Error");
            deEngineMapper.updateByPrimaryKeyWithBLOBs(kettle);
            return ResultHolder.error(e.getMessage());
        }
    }

    public List<DeEngine> pageList(){
        DeEngineExample deEngineExample = new DeEngineExample();
        deEngineExample.createCriteria().andTypeEqualTo("kettle");
        return deEngineMapper.selectByExampleWithBLOBs(deEngineExample);
    }

    public void updateKettleStatus(){
        if(!engineService.isClusterMode()){
            return;
        }
        List<DeEngine>kettles = pageList();
        kettles.forEach(kettle -> {
            validate(kettle.getId());
        });
    }

    public SlaveServer getSlaveServer() throws Exception{
        SlaveServer remoteSlaveServer = new SlaveServer();
        if(engineService.isLocalMode()){
            remoteSlaveServer.setHostname(env.getProperty("carte.host", "127.0.0.1"));
            remoteSlaveServer.setPort(env.getProperty("carte.port", "8080"));
            remoteSlaveServer.setUsername(env.getProperty("carte.user", "cluster"));
            remoteSlaveServer.setPassword(env.getProperty("carte.passwd", "cluster"));
        }else {
            List<DeEngine> kettles = pageList().stream().filter(kettle -> kettle.getStatus() != null && kettle.getStatus().equalsIgnoreCase("Success"))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(kettles)){
                throw new Exception("No valid kettle service.");
            }
            DeEngine kettle = kettles.get(new Random().nextInt(kettles.size()));
            KettleDTO kettleDTO  = new Gson().fromJson(kettle.getConfiguration(), KettleDTO.class);
            remoteSlaveServer.setHostname(kettleDTO.getCarte());
            remoteSlaveServer.setPort(kettleDTO.getPort());
            remoteSlaveServer.setUsername(kettleDTO.getUser());
            remoteSlaveServer.setPassword(kettleDTO.getPasswd());
        }
        return remoteSlaveServer;
    }

    public boolean isKettleRunning() {
        if(engineService.isLocalMode()){
            try {
                KettleDTO kettleDTO = new KettleDTO();
                kettleDTO.setCarte(env.getProperty("carte.host", "127.0.0.1"));
                kettleDTO.setPort(env.getProperty("carte.port", "8080"));
                kettleDTO.setUser(env.getProperty("carte.user", "cluster"));
                kettleDTO.setPasswd(env.getProperty("carte.passwd", "cluster"));
                validate(kettleDTO);
                return true;
            }catch (Exception e){
                return false;
            }
        }
        if(engineService.isClusterMode()){
            List<DeEngine> kettles = pageList().stream().filter(kettle -> kettle.getStatus() != null && kettle.getStatus().equalsIgnoreCase("Success"))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(kettles)){
               return false;
            }else {
                return true;
            }
        }
        return false;
    }

}
