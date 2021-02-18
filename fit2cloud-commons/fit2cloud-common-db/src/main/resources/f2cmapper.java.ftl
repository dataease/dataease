package ${package.Mapper};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};
<#if table.convert>
import org.apache.ibatis.annotations.Mapper;
</#if>


/**
    * <p>
    * ${table.comment!} Mapper 接口
    * </p>
    *
    * @author ${author}
    * @since ${date}
*/
<#if kotlin>
interface ${table.mapperName} : ${superMapperClass}<${entity}>
<#else>
<#if table.convert>
@Mapper
</#if>
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

}
</#if>
