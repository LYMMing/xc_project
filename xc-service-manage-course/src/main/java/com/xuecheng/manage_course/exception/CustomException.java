package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * 各系统添加自己需要的异常集合，而不是写在common中
 */

//控制器增强
@ControllerAdvice
public class CustomException extends ExceptionCatch {
    static{
        //因为spring security查询令牌，发现无权限报错
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
