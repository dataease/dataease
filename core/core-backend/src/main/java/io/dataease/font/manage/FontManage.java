package io.dataease.font.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.font.dto.FontDto;
import io.dataease.exception.DEException;
import io.dataease.font.dao.auto.entity.CoreFont;
import io.dataease.font.dao.auto.mapper.CoreFontMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FontManage {

    private static String path = "/opt/dataease2.0/data/font/";
    @Resource
    private CoreFontMapper coreFontMapper;

    public List<FontDto> list(FontDto fontDto) {
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        List<CoreFont> coreFonts = coreFontMapper.selectList(queryWrapper);
        List<FontDto> fontDtos = new ArrayList<>();
        for (CoreFont coreFont : coreFonts) {
            FontDto dto = new FontDto();
            BeanUtils.copyBean(dto, coreFont);
            fontDtos.add(dto);
        }

        return fontDtos;
    }

    public FontDto create(FontDto fontDto) {
        fontDto.setId(IDUtils.snowID());
        CoreFont coreFont = new CoreFont();
        BeanUtils.copyBean(coreFont, fontDto);
        coreFontMapper.insert(coreFont);
        return fontDto;
    }


    public FontDto edit(FontDto fontDto) {
        fontDto.setId(IDUtils.snowID());
        CoreFont coreFont = new CoreFont();
        BeanUtils.copyBean(coreFont, fontDto);
        coreFontMapper.updateById(coreFont);
        return fontDto;
    }

    public void delete(Long id) {
        coreFontMapper.deleteById(id);
        //TODO delete file
    }

    public void changeDefault(FontDto fontDto) {
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", fontDto.getId());
        CoreFont record = new CoreFont();
        record.setIsDefault(fontDto.getIsDefault());
        coreFontMapper.update(record, queryWrapper);
    }

    public void upload(MultipartFile file, long fontID) {
        String filename = file.getOriginalFilename();
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", fontID);
        CoreFont record = new CoreFont();
        record.setFileName(filename);
        record.setFileTransName(filename);
        coreFontMapper.update(record, queryWrapper);
        String fileUuid = UUID.randomUUID().toString();
        saveFile(file, fileUuid);
    }

    private static String saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        String fileTransName = "";
        try {
            String filename = file.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            String filePath = path + fileNameUUID + "." + suffix;
            File f = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
            fileTransName = fileNameUUID + "." + suffix;
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return fileTransName;
    }

}
