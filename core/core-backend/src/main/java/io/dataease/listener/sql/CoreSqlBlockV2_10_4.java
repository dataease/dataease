package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.map.dao.auto.mapper.AreaRepository;
import io.dataease.visualization.dao.auto.entity.VisualizationBackground;
import io.dataease.visualization.dao.auto.mapper.VisualizationBackgroundRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationSubjectRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CoreSqlBlockV2_10_4 implements CoreSqlBlock {

    @Resource
    private AreaRepository areaRepository;
    @Resource
    private VisualizationSubjectRepository visualizationSubjectRepository;
    @Resource
    private VisualizationBackgroundRepository visualizationBackgroundRepository;


    @Override
    public Version getVersion() {
        return new Version("2.10.4");
    }

    @Override
    public void execute() {
        visualizationSubjectRepository.updateNameById("10001", "chart.light_theme");
        visualizationSubjectRepository.updateNameById("10002", "chart.dark_theme");
        areaRepository.deleteById("156320571");
        areaRepository.deleteById("156500200");


        areaRepository.updatePid("156500100", "156500200");
        areaRepository.updateNameById("156500101", "万州区");


        visualizationBackgroundRepository.deleteAll();
        List<VisualizationBackground> defaultBackgrounds = Arrays.asList(
                new VisualizationBackground("board_1", "1", "default", "img/board", "board/board_1.svg"),
                new VisualizationBackground("board_2", "2", "default", "img/board", "board/board_2.svg"),
                new VisualizationBackground("board_3", "3", "default", "img/board", "board/board_3.svg"),
                new VisualizationBackground("board_4", "4", "default", "img/board", "board/board_4.svg"),
                new VisualizationBackground("board_5", "5", "default", "img/board", "board/board_5.svg"),
                new VisualizationBackground("board_6", "6", "default", "img/board", "board/board_6.svg"),
                new VisualizationBackground("board_7", "7", "default", "img/board", "board/board_7.svg"),
                new VisualizationBackground("board_8", "8", "default", "img/board", "board/board_8.svg"),
                new VisualizationBackground("board_9", "9", "default", "img/board", "board/board_9.svg")
        );
        visualizationBackgroundRepository.saveAll(defaultBackgrounds);
    }

}
