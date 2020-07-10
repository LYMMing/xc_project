package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.ResponseResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class CourseServiceTest {
    @Autowired
    CourseService courseService;
//    @Test
//    public void findCourseListPage() {
//        courseService.findCourseListPage(1,10,new CourseListRequest());
//    }

    @Test
    public void getCourseBaseById(){
        CourseBase courseBase = courseService.getCourseBaseById("297e7c7c62b888f00162b8a7dec20000");
        System.out.println(courseBase);
    }

    @Test
    public void updateCourseBase(){
        CourseBase courseBase = new CourseBase();
        courseBase.setName("test01课程");
        courseService.updateCourseBase("402885816240d276016240f7e5000002",courseBase);
    }
}