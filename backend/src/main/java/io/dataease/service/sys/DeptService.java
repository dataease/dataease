package io.dataease.service.sys;

import io.dataease.base.domain.SysDept;
import io.dataease.base.domain.SysDeptExample;
import io.dataease.base.mapper.SysDeptMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class DeptService {

    private final static Integer DEPT_ROOT_LEVEL = 0;

    @Resource
    private SysDeptMapper sysDeptMapper;

    public List<SysDept> root(){
        SysDeptExample example = new SysDeptExample();
        example.createCriteria().andLevelEqualTo(DEPT_ROOT_LEVEL);
        example.setOrderByClause("dept_sort");
        List<SysDept> sysDepts = sysDeptMapper.selectByExample(example);
        return sysDepts;
    }

    public boolean add(SysDept sysDept){
        if (ObjectUtils.isEmpty(sysDept.getLevel())){
            sysDept.setLevel(DEPT_ROOT_LEVEL);
        }
        Date now = new Date();
        sysDept.setCreateTime(now);
        sysDept.setUpdateTime(now);
        sysDept.setCreateBy(null);
        sysDept.setUpdateBy(null);
        try {
            int insert = sysDeptMapper.insert(sysDept);
            if (insert == 1){
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

}
