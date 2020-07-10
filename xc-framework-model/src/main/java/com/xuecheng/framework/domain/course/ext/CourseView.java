package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 课程详情页数据模型
 */
@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable {
    CourseBase courseBase;      //基础信息
    CourseMarket courseMarket;  //课程营销
    CoursePic coursePic;        //课程图片
    TeachplanNode teachplanNode;//教学计划
}
