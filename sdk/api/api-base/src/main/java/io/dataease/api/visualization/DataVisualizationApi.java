package io.dataease.api.visualization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface DataVisualizationApi {
    /**
     * 查询数据可视化大屏
     *
     * @return
     */
    @GetMapping("/findById/{dvId}")
    DataVisualizationVO findById(@PathVariable("dvId") Long dvId);

    @PostMapping("/save")
    void save(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/update")
    void update(@RequestBody DataVisualizationBaseRequest request);

    @DeleteMapping("/deleteLogic/{dvId}")
    void deleteLogic(@PathVariable("dvId") Long dvId);


    @PostMapping("/tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request);

    @PostMapping("/savaOrUpdateBase")
    void savaOrUpdateBase(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/move")
    void move(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/nameCheck")
    void nameCheck(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/findRecent")
    List<DataVisualizationVO> findRecent(@RequestBody DataVisualizationBaseRequest request);

    @PostMapping("/copy")
    @JsonSerialize(using = ToStringSerializer.class)
    String copy(@RequestBody DataVisualizationBaseRequest request);

}
