package io.dataease.api.spreadsheet;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.spreadsheet.dto.SpreadsheetCreator;
import io.dataease.api.spreadsheet.dto.SpreadsheetEditor;
import io.dataease.api.spreadsheet.vo.SpreadsheetTreeVO;
import io.dataease.api.spreadsheet.vo.SpreadsheetVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.BusiNodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.SPREADSHEET;

@Tag(name = "电子表格管理")
@ApiSupport(order = 969)
@DeApiPath(value = "/spreadsheet", rt = SPREADSHEET)
public interface SpreadsheetApi {

    @Operation(summary = "创建文件夹")
    @DePermit({"#p0.pid+':manage'"})
    @PostMapping("/createFolder")
    SpreadsheetVO createFolder(@RequestBody SpreadsheetCreator creator);

    @Operation(summary = "创建电子表格")
    @DePermit({"#p0.pid+':manage'"})
    @PostMapping("/create")
    SpreadsheetVO create(@RequestBody SpreadsheetEditor editor);

    @Operation(summary = "更新电子表格")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/update")
    SpreadsheetVO update(@RequestBody SpreadsheetEditor editor);

    @Operation(summary = "根据ID查询电子表格")
    @GetMapping("/get/{id}")
    SpreadsheetVO get(@PathVariable("id") Long id);

    @Operation(summary = "获取电子表格详情")
    @DePermit({"#p0.id+':read'"})
    @PostMapping("/findById")
    SpreadsheetVO findById(@RequestBody SpreadsheetEditor request);

    @Operation(summary = "获取电子表格编辑稿")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/findEditById")
    SpreadsheetVO findEditById(@RequestBody SpreadsheetEditor request);

    @Operation(summary = "获取资源树")
    @PostMapping("/tree")
    List<SpreadsheetTreeVO> tree(@RequestBody BusiNodeRequest request);

    @Operation(summary = "移动资源")
    @DePermit({"#p0.id+':manage'", "#p0.pid+':manage'"})
    @PostMapping("/move")
    void move(@RequestBody SpreadsheetEditor editor);

    @Operation(summary = "重命名")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/rename")
    void rename(@RequestBody SpreadsheetEditor editor);

    @Operation(summary = "删除资源")
    @DePermit({"#p0+':manage'"})
    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);

    @Operation(summary = "名称检查")
    @PostMapping("/nameCheck")
    boolean nameCheck(@RequestBody SpreadsheetEditor editor);

    @Operation(summary = "更新发布状态")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/updateStatus")
    SpreadsheetVO updateStatus(@RequestBody SpreadsheetEditor editor);

    @Operation(summary = "恢复到发布版本")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/recoverToPublished")
    SpreadsheetVO recoverToPublished(@RequestBody SpreadsheetEditor editor);

}
