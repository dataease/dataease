package io.dataease.operation.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.operation.dao.auto.entity.CoreOptRecent;
import io.dataease.operation.dao.auto.mapper.CoreOptRecentMapper;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.IDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CoreOptRecentManage {

    @Autowired
    private CoreOptRecentMapper coreStoreMapper;

    public void saveOpt(Long resourceId,int resourceType,int optType) {
        Long uid = AuthUtils.getUser().getUserId();
        QueryWrapper<CoreOptRecent> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("resource_id",resourceId);
        updateWrapper.eq("resource_type",resourceType);
        updateWrapper.eq("uid",uid);
        CoreOptRecent updateParam = new CoreOptRecent();
        updateParam.setOptType(optType);
        updateParam.setTime(System.currentTimeMillis());
        if(coreStoreMapper.update(updateParam,updateWrapper)==0){
            CoreOptRecent optRecent = new CoreOptRecent();
            optRecent.setId(IDUtils.snowID());
            optRecent.setResourceId(resourceId);
            optRecent.setResourceType(resourceType);
            optRecent.setOptType(optType);
            optRecent.setTime(System.currentTimeMillis());
            optRecent.setUid(AuthUtils.getUser().getUserId());
            coreStoreMapper.insert(optRecent);
        }
    }

}
