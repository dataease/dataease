package io.dataease.api.visualization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.PANEL;

@Tag(name = "可视化管理:基础")
@ApiSupport(order = 999)
@DeApiPath(value = "/dataVisualization", rt = PANEL)
public interface DataVisualizationApi {
    /**
     * 查询数据可视化大屏
     *
     * @return
     */
    @GetMapping("/findById/{dvId}/{busiFlag}")
    @DePermit(value = {"#p0+':read'"}, busiFlag = "#p1")
    @Operation(summary = "查询可视化资源")
    DataVisualizationVO findById(@PathVariable("dvId") Long dvId,@PathVariable("busiFlag") String busiFlag);

    @GetMapping("/findCopyResource/{dvId}/{busiFlag}")
    @Operation(summary = "查询临时复制资源")
    DataVisualizationVO findCopyResource(@PathVariable("dvId") Long dvId,@PathVariable("busiFlag") String busiFlag);


    @PostMapping("/saveCanvas")
    @DePermit(value = {"#p0.pid + ':manage'"}, busiFlag = "#p0.type")
    @Operation(summary = "画布保存")
    String saveCanvas(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/updateCanvas")
    @DePermit(value = {"#p0.id + ':manage'"}, busiFlag = "#p0.type")
    @Operation(summary = "画布更新")
    void updateCanvas(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/updateBase")
    @DePermit(value = {"#p0.id + ':manage'"}, busiFlag = "#p0.type")
    @Operation(summary = "可视化资源基础信息更新")
    void updateBase(@RequestBody DataVisualizationBaseRequest request);

    @DeleteMapping("/deleteLogic/{dvId}/{busiFlag}")
    @DePermit(value = {"#p0+':manage'"}, busiFlag = "#p1")
    @Operation(summary = "可视化资源删除")
    void deleteLogic(@PathVariable("dvId") Long dvId,@PathVariable("busiFlag") String busiFlag);

    @PostMapping("/tree")
    @Operation(summary = "查询可视化资源树")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request);

    @PostMapping("/move")
    @DePermit(value = {"#p0.id+':manage'", "#p0.pid+':manage'"}, busiFlag = "#p0.type")
    @Operation(summary = "移动可视化资")
    void move(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/nameCheck")
    @Operation(summary = "名称校验")
    void nameCheck(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/findRecent")
    @Operation(summary = "查询最近操作资源")
    List<VisualizationResourceVO> findRecent(@RequestBody VisualizationWorkbranchQueryRequest request);

    @PostMapping("/copy")
    @JsonSerialize(using = ToStringSerializer.class)
    @DePermit(value = {"#p0.id+':manage'", "#p0.pid+':manage'"}, busiFlag = "#p0.type")
    @Operation(summary = "复制")
    String copy(@RequestBody DataVisualizationBaseRequest request);

    @GetMapping("/findDvType/{dvId}")
    @Operation(summary = "查询可视化资源类型")
    String findDvType(@PathVariable("dvId")Long dvId);

    /**
     * 从模板解压可视化资源 模板来源包括 模板市场、内部模板管理
     *
     * @return
     */
    @PostMapping("/decompression")
    @Operation(summary = "解析可视化资源模板信息")
    DataVisualizationVO decompression(@RequestBody DataVisualizationBaseRequest request) throws Exception;

    /**
     * 从模板解压可视化资源 模板来源包括本地上传
     *
     * @return
     */
    @PostMapping("/decompressionLocalFile")
    @Operation(summary = "解析可视化资源模板文件信息")
    DataVisualizationVO decompressionLocalFile(@RequestPart(value = "file") MultipartFile file);

}
