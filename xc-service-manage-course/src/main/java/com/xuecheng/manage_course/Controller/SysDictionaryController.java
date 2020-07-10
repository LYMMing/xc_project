package com.xuecheng.manage_course.Controller;

import com.xuecheng.api.course.SysDictionaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys")
public class SysDictionaryController implements SysDictionaryControllerApi {

    @Autowired
    SysDictionaryService sysDictionaryService;

    /**
     * 根据标号查询系统信息
     * @param dType
     * @return
     */
    @GetMapping("/dictionary/get/{dType}")
    @Override
    public SysDictionary getByType(@PathVariable("dType") String dType) {
        return sysDictionaryService.getByType(dType);
    }
}
