package io.dataease.mobile.dto;


import lombok.Data;
import java.io.Serializable;

@Data
public class HomeItemDTO implements Serializable {

    private String id;
    private String title;
    private Long time;
}
