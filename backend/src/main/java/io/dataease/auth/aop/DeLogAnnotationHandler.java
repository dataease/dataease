package io.dataease.auth.aop;


import io.dataease.auth.annotation.DeLog;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AopUtils;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.log.FolderItem;
import io.dataease.service.sys.log.LogManager;
import io.dataease.service.sys.log.LogService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
public class DeLogAnnotationHandler {

    @Resource
    private LogManager logManager;

    @Resource
    private LogService logService;

    private static List<Integer> before = new ArrayList<>();

    @PostConstruct
    public void init() {
        before.add(SysLogConstants.OPERATE_TYPE.DELETE.getValue());
        before.add(SysLogConstants.OPERATE_TYPE.UNSHARE.getValue());
        before.add(SysLogConstants.OPERATE_TYPE.UNAUTHORIZE.getValue());
    }

    private SysLogDTO exec(JoinPoint point, DeLog deLog) throws Exception{

        Object[] args = point.getArgs();
        if (ArrayUtils.isEmpty(args)) return null;

        SysLogConstants.OPERATE_TYPE operatetype = deLog.operatetype();
        SysLogConstants.SOURCE_TYPE sourcetype = deLog.sourcetype();

        String sourceKey = StringUtils.isNotBlank(deLog.sourceKey()) ? deLog.sourceKey() : deLog.value();
        int sourceIndex = deLog.sourceIndex();
        if (args.length <= sourceIndex) return null;
        Object arg = args[sourceIndex];
        Object sourceIdValue = AopUtils.getParamValue(arg, sourceKey, 0);
        if (ObjectUtils.isEmpty(sourceIdValue)) return null;

        SysLogDTO sysLogDTO = new SysLogDTO();
        sysLogDTO.setOperateType(operatetype.getValue());
        sysLogDTO.setSourceType(sourcetype.getValue());
        sysLogDTO.setSourceId(sourceIdValue.toString());
        FolderItem sourceInfo = logManager.nameWithId(sourceIdValue.toString(), sourcetype.getValue());
        if (ObjectUtils.isEmpty(sourceInfo)) {
            return null;
        }
        sysLogDTO.setSourceName(sourceInfo.getName());

        // 填充资源位置信息
        int positionIndex = deLog.positionIndex();
        if (positionIndex > -1 && args.length > positionIndex){
            String positionKey = deLog.positionKey();
            Object positionArg = args[positionIndex];
            Object bottomPositionValue = AopUtils.getParamValue(positionArg, positionKey, 0);
            if (ObjectUtils.isNotEmpty(bottomPositionValue)) {
                if (sourcetype == SysLogConstants.SOURCE_TYPE.DATASOURCE) {
                    FolderItem folderItem = logManager.dsTypeInfo(bottomPositionValue.toString());
                    List<FolderItem> items = new ArrayList<>();
                    items.add(folderItem);
                    sysLogDTO.setPositions(items);
                }else {
                    List<FolderItem> parentsAndSelf = logManager.parentsAndSelf(bottomPositionValue.toString(), sourcetype);
                    sysLogDTO.setPositions(parentsAndSelf);
                }

            }
        }
        // 填充资源目标位置信息
        int targetIndex = deLog.targetIndex();
        if (targetIndex > -1 && args.length > targetIndex){
            String targetKey = deLog.targetKey();
            Object targetArg = args[targetIndex];
            SysLogConstants.SOURCE_TYPE targetType = deLog.targetType();
            Object bottomTargetValue = AopUtils.getParamValue(targetArg, targetKey, 0);
            if (ObjectUtils.isNotEmpty(bottomTargetValue)) {
                List<FolderItem> parentsAndSelf = logManager.parentsAndSelf(bottomTargetValue.toString(), targetType);
                sysLogDTO.setRemarks(parentsAndSelf);
            }
        }
        return sysLogDTO;
    }

    @Around(value = "@annotation(io.dataease.auth.annotation.DeLog)")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        SysLogDTO logDTO = null;
        Object result = null;
        DeLog log = getLog(point);
        if(before.contains(log.operatetype().getValue())) {
            // 前置处理 比如删除操作 需要在数据删除之前查询
            logDTO = exec(point, log);
            result = point.proceed(point.getArgs());
        }else {
            // 后置处理 比如保存操作 需要在保存之后才有主键
            result = point.proceed(point.getArgs());
            logDTO = exec(point, log);
        }

        if (ObjectUtils.isNotEmpty(result) && result instanceof ResultHolder && !((ResultHolder) result).isSuccess()) {
            return result;
        }
        Optional.ofNullable(logDTO).ifPresent(logService::saveLog);
        return result;
    }



    private DeLog getLog(JoinPoint point) {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        DeLog deLog = method.getAnnotation(DeLog.class);
        return deLog;
    }




}
