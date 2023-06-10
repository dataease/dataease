package io.dataease.api.visualization;

import io.dataease.api.visualization.request.VisualizationSubjectRequest;
import io.dataease.api.visualization.vo.VisualizationSubjectVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface VisualizationSubjectApi {

    @PostMapping("/query")
    List<VisualizationSubjectVO> query(@RequestBody VisualizationSubjectRequest request);

    @PostMapping("/querySubjectWithGroup")
    List<VisualizationSubjectVO> querySubjectWithGroup(@RequestBody VisualizationSubjectRequest request);

    @PostMapping("/update")
    void update(@RequestBody VisualizationSubjectRequest request);

    @DeleteMapping("/delete/{id}")
    void delete(@PathVariable String id);

}
