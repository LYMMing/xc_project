package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;


/**
 * 自定义异常类
 * 继承RuntimeException，而不是Exception。
 * 因为后者需要我自己try catch捕获，前者可统一由系统抛出，符合异常处理的架构
 */
public class CustomException extends RuntimeException{
    private ResultCode resultCode;

    public CustomException(ResultCode resultCode){
        super("错误代码："+ resultCode.code()+ " 错误信息："+resultCode.message());
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode(){
        return this.resultCode;
    }
}
