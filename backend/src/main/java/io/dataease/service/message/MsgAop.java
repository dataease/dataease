package io.dataease.service.message;


import io.dataease.controller.sys.response.SubscribeNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

@Aspect
@Component
public class MsgAop {




    @Resource
    private SysMsgService sysMsgService;





    /**
     * 对sendMsg 切面拦截
     * @param point
     */
    @Around("(execution(* io.dataease.service.message.SysMsgService.sendMsg(..)))")
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

        List<SubscribeNode> subscribes = sysMsgService.subscribes(userId);

        try {
            // 如果已经订阅了这种类型的消息 直接发送 否则直接返回
            if (CollectionUtils.isNotEmpty(subscribes) && subscribes.stream().anyMatch(item -> item.match(typeId, channelId)))
                return point.proceed(args);
            return null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;

    }
}
