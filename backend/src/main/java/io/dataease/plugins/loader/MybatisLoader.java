package io.dataease.plugins.loader;

import io.dataease.base.domain.MyPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MybatisLoader {



    @Autowired
    private MyScanner myScanner;



    public void loadMybatis(MyPlugin myPlugin) {
        if (!myPlugin.getLoadMybatis()) return;
        myScanner.scanner();
    }

}
