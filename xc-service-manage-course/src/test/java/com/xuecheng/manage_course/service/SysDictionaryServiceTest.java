package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class SysDictionaryServiceTest {
    @Autowired
    SysDictionaryService sysDictionaryService;
    @Test
    public void getByType() {
        SysDictionary sysDictionary = sysDictionaryService.getByType("100");
        System.out.println(sysDictionary);
    }
}