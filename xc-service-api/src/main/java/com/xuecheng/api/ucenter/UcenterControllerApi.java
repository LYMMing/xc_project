package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;

@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {
    /**
     * 根据账号查询用户信息
     * @param username
     * @return
     */
    public XcUserExt getUserext(String username);

    /**
     * 用户注册
     * @return
     */
    public ResponseResult register(XcUser xcUser);
}
