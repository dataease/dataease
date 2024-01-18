package io.dataease.rmonitor.mapper;

import io.dataease.rmonitor.mapper.entity.DatasetFreeResource;
import io.dataease.rmonitor.mapper.entity.DsFreeResource;
import io.dataease.rmonitor.mapper.entity.VisualFreeResource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResourceMonitorMapper {

    @Select("select count(id) from core_datasource")
    int dsCount();

    @Select("select count(id) from core_dataset_group")
    int datasetCount();

    @Select("select count(id) from data_visualization_info")
    int vCount();

    @Select("select id, name, pid, type, status from core_datasource")
    List<DsFreeResource> queryFreeDs();

    @Select("select id, name, pid, node_type from core_dataset_group")
    List<DatasetFreeResource> queryFreeDataset();

    @Select("select id, name, pid, node_type, type from data_visualization_info")
    List<VisualFreeResource> queryFreeVusial();

    @Delete("delete from core_datasource")
    void delFreeDs();

    @Delete("delete from core_dataset_group")
    void delFreeDataset();
    @Delete("delete from data_visualization_info")
    void delFreeVisual();

}
