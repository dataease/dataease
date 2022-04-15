package io.dataease.ext;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.InputStream;

public interface ExtFileContentMapper {
    @Select(value = {
            "SELECT file ",
            "FROM file_content ",
            "WHERE file_id = #{id, jdbcType=VARCHAR}"
    })
    InputStream selectZipBytes(@Param("id") String id);
}
