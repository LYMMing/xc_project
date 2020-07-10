package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class CategoryServiceTest {
    @Autowired
    CategoryService categoryService;

    @Test
    public void findList(){
        CategoryNode categoryNode =  categoryService.findList();
        System.out.println(categoryNode);
    }


}