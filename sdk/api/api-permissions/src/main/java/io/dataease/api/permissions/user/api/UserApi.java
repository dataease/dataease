package io.dataease.api.permissions.user.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.*;
import io.dataease.api.permissions.user.vo.*;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.KeywordRequest;
import io.dataease.request.BaseGridRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.USER;


@DeApiPath(value = "/user", rt = USER)
public interface UserApi {

    @DePermit("m:read")
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<UserGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @DePermit({"m:read", "#p0 + ':read'"})
    @GetMapping("/queryById/{id}")
    UserFormVO queryById(@PathVariable("id") Long id);


    @GetMapping("/personInfo")
    UserFormVO personInfo();

    @DePermit("m:read")
    @PostMapping("/create")
    void create(@RequestBody UserCreator creator);

    @DePermit({"m:read", "#p0.id + ':manage'"})
    @PostMapping("/edit")
    void edit(@RequestBody UserEditor editor);

    @DePermit({"m:read", "#p0 + ':manage'"})
    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);

    @DePermit({"m:read", "#p0 + ':manage'"})
    @PostMapping("/batchDel")
    void batchDel(@RequestBody List<Long> ids);

    @PostMapping("/role/option")
    List<UserItemVO> optionForRole(@RequestBody UserRequest request);

    @PostMapping("/role/selected/{goPage}/{pageSize}")
    IPage<UserItemVO> selectedForRole(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody UserRequest request);

    @PostMapping("/switch/{oId}")
    String switchOrg(@PathVariable("oId") Long oId);

    @GetMapping("/info")
    CurUserVO info();

    @PostMapping("/byCurOrg")
    List<UserItem> byCurOrg(@RequestBody KeywordRequest request);

    @GetMapping("/userCount")
    int userCount();

    @PostMapping("/switchLanguage")
    void switchLanguage(@RequestBody LangSwitchRequest request);

    @PostMapping("/excelTemplate")
    void excelTemplate();

    @PostMapping("/batchImport")
    UserImportVO batchImport(@RequestPart(value = "file") MultipartFile file);


    @GetMapping("/errorRecord/{key}")
    void errorRecord(@PathVariable("key") String key);

    @GetMapping("/clearErrorRecord/{key}")
    void clearErrorRecord(@PathVariable("key") String key);

    @DePermit({"m:read"})
    @GetMapping("/defaultPwd")
    String defaultPwd();

    @DePermit({"m:read", "#p0 + ':manage'"})
    @PostMapping("/resetPwd/{id}")
    void resetPwd(@PathVariable("id") Long id);

    @DePermit({"m:read", "#p0.id + ':manage'"})
    @PostMapping("/enable")
    void enable(@RequestBody EnableSwitchRequest request);

    @PostMapping("/modifyPwd")
    void modifyPwd(@RequestBody ModifyPwdRequest request);


}
