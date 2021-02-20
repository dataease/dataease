package io.dataease.controller;

import com.alibaba.fastjson.JSONObject;
import io.dataease.base.domain.User;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.controller.handler.annotation.NoResultHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/test")
public class TestController {


    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public Object testUpload(@RequestPart(value = "id") String id, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles
                             , @RequestPart(value = "user") User user, @RequestParam(value = "name") String name) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("file", file.getOriginalFilename());
        jsonObject.put("files", bodyFiles.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList()));
        return jsonObject;
    }

    @PostMapping(value = "/multipart", consumes = {"multipart/form-data"})
    public Object testMultipart(@RequestPart(value = "id") String id, @RequestPart(value = "user") User user, @RequestParam(value = "name") String name) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("user", user.getName());
        jsonObject.put("name", name);
        return jsonObject;
    }

    @PostMapping(value = "/wwwform", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object testWwwForm(String id, User user, String name) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("user", user.getName());
        jsonObject.put("name", name);
        return jsonObject;
    }

    @NoResultHolder
    @GetMapping(value = "/xml")
    public String getXmlString() {
        return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "\n" +
                "<bookstore>\n" +
                "\n" +
                "<book>\n" +
                "  <title lang=\"eng\">Harry Potter</title>\n" +
                "  <price>29.99</price>\n" +
                "</book>\n" +
                "\n" +
                "<book>\n" +
                "  <title lang=\"eng\">Learning XML</title>\n" +
                "  <price>39.95</price>\n" +
                "</book>\n" +
                "\n" +
                "</bookstore>";
    }

    @GetMapping(value = "/{str}")
    public Object getString(@PathVariable String str) throws InterruptedException {
        if (StringUtils.equals("error", str)) {
            throw new RuntimeException("test error");
        }
        if (StringUtils.equals("warning", str)) {
            return ResultHolder.error("test warning");
        }
        if (StringUtils.equals("user", str)) {
            return ResultHolder.success(SessionUtils.getUser());
        }
        if (StringUtils.equals("sleep", str)) {
            Thread.sleep(2000L);
            return ResultHolder.success(str);
        }
        return ResultHolder.success(str);
    }

}
