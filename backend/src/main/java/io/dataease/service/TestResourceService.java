package io.dataease.service;

import io.dataease.base.domain.TestResource;
import io.dataease.base.domain.TestResourceExample;
import io.dataease.base.mapper.TestResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResourceService {

    @Resource
    private TestResourceMapper testResourceMapper;

    public TestResource addTestResource(TestResource testResource) {
        testResource.setId(UUID.randomUUID().toString());
        testResource.setStatus("1");
        testResource.setCreateTime(System.currentTimeMillis());
        testResource.setUpdateTime(System.currentTimeMillis());
        testResourceMapper.insertSelective(testResource);
        return testResource;
    }

    public List<TestResource> getTestResourceList(String testResourcePoolId) {
        TestResourceExample testResourceExample = new TestResourceExample();
        testResourceExample.createCriteria().andTestResourcePoolIdEqualTo(testResourcePoolId);
        List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(testResourceExample);
        return testResources;
    }

    public void deleteTestResource(String testResourceId) {
        testResourceMapper.deleteByPrimaryKey(testResourceId);
    }

    public void updateTestResource(TestResource testResource) {
        testResource.setUpdateTime(System.currentTimeMillis());
        testResourceMapper.updateByPrimaryKeySelective(testResource);
    }

    public List<TestResource> getResourcesByPoolId(String resourcePoolId) {
        TestResourceExample example = new TestResourceExample();
        example.createCriteria().andTestResourcePoolIdEqualTo(resourcePoolId);
        return testResourceMapper.selectByExampleWithBLOBs(example);
    }

    public TestResource getTestResource(String resourceId) {
        return testResourceMapper.selectByPrimaryKey(resourceId);
    }
}
