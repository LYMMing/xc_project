package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import org.hibernate.boot.jaxb.SourceType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void test(){
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/getmodel/5a791725dd573c3574ee333f",Map.class);
        System.out.println(forEntity);
    }

    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    @Test
    public void testFindPage(){
        int page = 0; //第0页开始
        int size = 10;//每页记录数
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    /**
     * 带条件的页面查询
     */
    @Test
    public void testFindPageByParams(){
        //分页参数
        int page = 0; //第0页开始
        int size = 10;//每页记录数
        Pageable pageable = PageRequest.of(page,size);
        //条件匹配器，没设置匹配器时，创建的条件实例按精确查找
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());

        //条件值
        CmsPage cmsPage = new CmsPage();
//        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        cmsPage.setPageAliase("轮播");

        //创建条件实例
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }

    @Test
    public void testInsert(){
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageName("测试页面");
        cmsPage.setSiteId("s01");
        cmsPage.setTemplateId("t01");
        cmsPage.setPageCreateTime(new Date());
        List<CmsPageParam> list = new ArrayList<>();
        CmsPageParam cmsPageParam =new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");
        list.add(cmsPageParam);
        cmsPage.setPageParams(list);
        CmsPage c = cmsPageRepository.save(cmsPage);
        System.out.println(c);
    }

    @Test
    public void testDelete(){
        cmsPageRepository.deleteById("5eb41fe8a25bf31b9c6ccd92");
    }

    /**
     * 查+存，这个存似乎因为主键一样，底层编程修改
     */
    @Test
    public void testUpdate(){
        Optional<CmsPage> optional = cmsPageRepository.findById("5eb41fe8a25bf31b9c6ccd92");
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            cmsPage.setPageName("修改后的测试页面");
            CmsPage c = cmsPageRepository.save(cmsPage);
            System.out.println(c);
        }
    }

    /**
     * 自定义
     */
    @Test
    public void testFindByPageName(){
        CmsPage cmsPage = cmsPageRepository.findByPageName("测试页面");
        System.out.println(cmsPage);
    }

}