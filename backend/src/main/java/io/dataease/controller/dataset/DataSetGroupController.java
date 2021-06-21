package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.dataset.ExtractDataService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/20 8:29 下午
 */
@RestController
@RequestMapping("dataset/group")
public class DataSetGroupController {
    @Resource
    private DataSetGroupService dataSetGroupService;
    @Resource
    private ExtractDataService extractDataService;

    @PostMapping("/save")
    public DataSetGroupDTO save(@RequestBody DatasetGroup datasetGroup) {
        return dataSetGroupService.save(datasetGroup);
    }

    @PostMapping("/tree")
    public List<DataSetGroupDTO> tree(@RequestBody DataSetGroupRequest datasetGroup) {
        return dataSetGroupService.tree(datasetGroup);
    }

    @PostMapping("/treeNode")
    public List<DataSetGroupDTO> treeNode(@RequestBody DataSetGroupRequest datasetGroup) {
        return dataSetGroupService.treeNode(datasetGroup);
    }

    @PostMapping("/delete/{id}")
    public void tree(@PathVariable String id) throws Exception {
        dataSetGroupService.delete(id);
    }

    @PostMapping("/getScene/{id}")
    public DatasetGroup getScene(@PathVariable String id) {
        return dataSetGroupService.getScene(id);
    }

    @PostMapping("/isKettleRunning")
    public boolean isKettleRunning() {
        return extractDataService.isKettleRunning();
    }
}
