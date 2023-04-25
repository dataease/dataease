package io.dataease.xpack.permissions.auth.manage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthMenu;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthMenuMapper;
import org.springframework.stereotype.Component;

@Component
public class MenuAuthManage extends ServiceImpl<PerAuthMenuMapper, PerAuthMenu> {
}
