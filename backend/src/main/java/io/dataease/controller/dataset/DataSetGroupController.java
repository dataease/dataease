package io.dataease.controller.dataset;

import io.dataease.base.domain.DatasetGroup;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.service.dataset.DataSetGroupService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author gin
 * @Date 2021/2/20 8:29 下午
 */
@RestController
@RequestMapping("dataset/group")
public class DataSetGroupController {
    @Resource
    private DataSetGroupService dataSetGroupService;

    @PostMapping("/save")
    public DataSetGroupDTO save(@RequestBody DatasetGroup datasetGroup) {
        return dataSetGroupService.save(datasetGroup);
    }
}
