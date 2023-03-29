package io.dataease.api.panel;

import io.dataease.api.panel.vo.PanelTreeNodeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface PanelTreeAPi {

    @GetMapping("/tree/{keyword}")
    List<PanelTreeNodeVO> tree(@PathVariable("keyword") String keyword);


}
