package io.dataease.api.free;

import io.dataease.api.free.dto.*;
import io.dataease.api.free.vo.FreeRelationVO;
import io.dataease.api.free.vo.FreeVO;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Hidden
public interface FreeApi {

    @PostMapping("/query")
    List<FreeVO> query(@RequestBody FreeQueryRequest request);

    @PostMapping("/syncAll")
    void syncAll(@RequestBody FreeSyncRequest request);

    @PostMapping("/deleteAll")
    void deleteAll();

    @PostMapping("/syncBatch")
    void syncBatch(@RequestBody FreeBatchSyncRequest request);

    @PostMapping("/deleteBatch")
    void deleteBatch(@RequestBody FreeBatchDelRequest request);

    @PostMapping("/relation")
    FreeRelationVO relation(@RequestBody FreeRelationRequest request);
}
