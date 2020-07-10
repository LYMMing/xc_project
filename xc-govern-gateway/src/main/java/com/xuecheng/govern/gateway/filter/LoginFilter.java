package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * zuul过滤器
 * 所有的配置的请求都被拦截
 1、从cookie查询用户身份令牌是否存在，不存在则拒绝访问
 2、从http header查询jwt令牌是否存在，不存在则拒绝访问
 3、从Redis查询user_token令牌是否过期，过期则拒绝访问
 */
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    AuthService authService;

    /**
     返回字符串代表过滤器的类型，如下
     pre：    请求在被转发之前执行
     routing：在转发请求时调用
     error：  处理请求时发生错误调用
     post：   在routing和error过滤器之后调用
     */
    public String filterType() {
        return "pre";
    }


    /**
     * 此方法返回整型数值，通过此数值来定义过滤器的执行顺序，数字越小优先级越高。
     */
    public int filterOrder() {
        return 2;
    }

    /**
     * 返回true表示要执行此过虑器，否则不执行
     */
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤器的业务逻辑
     */
    public Object run() throws ZuulException {
        //zuul拿到请求的特定对象，从里面可以拿到请求数据和响应
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();
        //从cookie查询用户身份令牌是否存在
        String access_token = authService.getTokenFromCookie(request);
        if(access_token == null){
            access_deny();
            return null;
        }
        //从http header查询jwt令牌是否存在
        String jwt = authService.getJwtFromHeader(request);
        if(jwt == null){
            access_deny();
            return null;
        }
        //从Redis查询user_token令牌是否过期
        long expire = authService.getExpire(access_token);
        if(expire <0){
            access_deny();

        }

        return null;
    }

    //拒绝访问
    private void access_deny(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        // 拒绝访问，不在进行请求的转发
        requestContext.setSendZuulResponse(false);

        requestContext.setResponseStatusCode(200);// 设置响应状态码
        ResponseResult unauthenticated = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(unauthenticated);
        requestContext.setResponseBody(jsonString);
        response.setContentType("application/json;charset=UTF-8");
    }
}
