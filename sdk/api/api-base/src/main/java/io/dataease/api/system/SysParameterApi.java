package io.dataease.api.system;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.system.request.OnlineMapEditor;
import io.dataease.api.system.vo.SettingItemVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "系统设置:系统参数")
@ApiSupport(order = 799)
public interface SysParameterApi {

    @Operation(summary = "查询单个配置值")
    @Parameter(name = "key", description = "配置key", required = true, in = ParameterIn.PATH)
    @GetMapping("/singleVal/{key}")
    String singleVal(@PathVariable("key") String key);

    @Operation(summary = "保存在线地图")
    @PostMapping("/saveOnlineMap")
    void saveOnlineMap(@RequestBody OnlineMapEditor editor);

    @Operation(summary = "查询在线地图")
    @GetMapping("/queryOnlineMap")
    String queryOnlineMap();

    @Operation(summary = "查询基础设置(非xpack)")
    @GetMapping("basic/query")
    List<SettingItemVO> queryBasicSetting();

    @Operation(summary = "保存基础设置(非xpack)")
    @PostMapping("/basic/save")
    void saveBasicSetting(@RequestBody List<SettingItemVO> settingItemVOS);

    @Operation(summary = "查询超时时间(非xpack)")
    @GetMapping("/requestTimeOut")
    public Integer RequestTimeOut();

    @Hidden
    @GetMapping("/ui")
    List<Object> ui();

    @Hidden
    @GetMapping("/defaultLogin")
    Integer defaultLogin();

}
