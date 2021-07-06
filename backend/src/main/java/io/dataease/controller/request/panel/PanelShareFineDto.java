package io.dataease.controller.request.panel;

import io.dataease.commons.model.AuthURD;
import lombok.Data;

import java.io.Serializable;


@Data
public class PanelShareFineDto implements Serializable {


    private static final long serialVersionUID = -792964171742204428L;

    private  String resourceId;

    private AuthURD authURD;
}
