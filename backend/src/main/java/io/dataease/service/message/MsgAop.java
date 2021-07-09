package io.dataease.service.message;

import io.dataease.base.domain.SysMsgSettingExample;
import io.dataease.base.mapper.SysMsgSettingMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Aspect
@Component
public class MsgAop {

    @Resource
    private SysMsgSettingMapper sysMsgSettingMapper;





    /**
     * 对sendMsg 切面拦截
     * @param point
     */
    @Around("(execution(* io.dataease.service.message.DeMsgutil.sendMsg(..)))")
    public Object cutPoint(ProceedingJoinPoint point) {

        Object[] args = point.getArgs();
        Object arg0 = args[0];
        Object arg1 = args[1];
        Object arg2 = args[2];

        if (ObjectUtils.isEmpty(arg0) || ObjectUtils.isEmpty(arg1) || ObjectUtils.isEmpty(arg2)) {
            return null;
        }
        Long userId = (Long) arg0;
        Long typeId = (Long) arg1;
        Long channelId = (Long) arg2;

        SysMsgSettingExample example = new SysMsgSettingExample();
        example.createCriteria().andChannelIdEqualTo(channelId).andUserIdEqualTo(userId).andTypeIdEqualTo(typeId).andEnableEqualTo(true);

        try {
            if (sysMsgSettingMapper.countByExample(example) > 0)
                return point.proceed(args);
            return null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;

    }
}
