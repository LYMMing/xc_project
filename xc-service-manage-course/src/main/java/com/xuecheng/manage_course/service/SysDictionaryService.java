package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用于数据字典中系统数据
 */
@Service
public class SysDictionaryService {
    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    /**
     * 通过编号获得数据字典中系统数据
     * @param
     * @return
     */
    public SysDictionary getByType(String dType){
        return sysDictionaryRepository.findByDType(dType);
    }
}
