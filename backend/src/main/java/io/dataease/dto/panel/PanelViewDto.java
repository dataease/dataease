package io.dataease.dto.panel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class PanelViewDto {

    private String id;

    private String pid;

    private String type;

    private String name;

    private List<PanelViewDto> children;

    public void addChild(PanelViewDto dto){
        children = Optional.ofNullable(children).orElse(new ArrayList<>());
        children.add(dto);
    }
}
