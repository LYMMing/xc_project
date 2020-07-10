package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异常捕获类
 * @ControllerAdvice 控制器增强
 * @ExceptionHandler 异常处理器，
 */
@ControllerAdvice
public class ExceptionCatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    //使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder =
            ImmutableMap.builder();

    /**
     * 捕获CustomException自定义异常
     * @param c
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException c){
        //打印异常日志
        LOGGER.error("catch exception: {}",c.getMessage());
        //相应异常数据
        ResultCode resultCode = c.getResultCode();
        return new ResponseResult(resultCode);
    }

    /**
     * 捕获非自定义异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception e){
        LOGGER.error("catch exception: {}",e.getMessage());
        if(EXCEPTIONS == null){
            EXCEPTIONS = builder.build();   //用builder构建ImmutableMap
        }
        ResultCode resultCode = EXCEPTIONS.get(e.getClass());   //寻找异常是否被定义在map中
        ResponseResult responseResult;
        if (resultCode != null){    //找到设定的报错信息
            responseResult = new ResponseResult(resultCode);
        }else{      //在map中没有找到，统一返回系统繁忙
            responseResult = new ResponseResult(CommonCode.SERVER_ERROR);
        }
        return responseResult;
    }

    static{
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);   //这是post请求没有json导致的异常
    }
}
