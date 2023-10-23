package io.dataease.api.visualization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.PANEL;

@DeApiPath(value = "/dataVisualization", rt = PANEL)
public interface DataVisualizationApi {
    /**
     * 查询数据可视化大屏
     *
     * @return
     */
    @GetMapping("/findById/{dvId}/{busiFlag}")
    @DePermit(value = {"#p0+':read'"}, busiFlag = "#p1")
    DataVisualizationVO findById(@PathVariable("dvId") Long dvId,@PathVariable("busiFlag") String busiFlag);

    @PostMapping("/saveCanvas")
    @DePermit(value = {"#p0.pid + ':manage'"}, busiFlag = "#p0.type")
    String saveCanvas(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/updateCanvas")
    @DePermit(value = {"#p0.id + ':manage'"}, busiFlag = "#p0.type")
    void updateCanvas(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/updateBase")
    @DePermit(value = {"#p0.id + ':manage'"}, busiFlag = "#p0.type")
    void updateBase(@RequestBody DataVisualizationBaseRequest request);

    @DeleteMapping("/deleteLogic/{dvId}/{busiFlag}")
    @DePermit(value = {"#p0+':manage'"}, busiFlag = "#p1")
    void deleteLogic(@PathVariable("dvId") Long dvId,@PathVariable("busiFlag") String busiFlag);

    @PostMapping("/tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request);

    @PostMapping("/move")
    @DePermit(value = {"#p0.id+':manage'", "#p0.pid+':manage'"}, busiFlag = "#p0.type")
    void move(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/nameCheck")
    void nameCheck(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/findRecent")
    List<VisualizationResourceVO> findRecent(@RequestBody VisualizationWorkbranchQueryRequest request);

    @PostMapping("/copy")
    @JsonSerialize(using = ToStringSerializer.class)
    @DePermit(value = {"#p0.id+':manage'", "#p0.pid+':manage'"}, busiFlag = "#p0.type")
    String copy(@RequestBody DataVisualizationBaseRequest request);

    @GetMapping("/findDvType/{dvId}")
    String findDvType(@PathVariable("dvId")Long dvId);
}
