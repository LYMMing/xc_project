package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.FeignClient.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    //课程预览功能需要数据
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;




    /**
     * 根据课程id查询课程计划
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId){
        if(StringUtils.isEmpty(courseId)){
            return null;
        }
        return teachplanMapper.selectList(courseId);
    }

    /**
     * 添加课程计划
     * 前端传的数据是没有根id
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        if(teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()
        ) || StringUtils.isEmpty(teachplan.getPname()) ){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //取课程id
        String courseid = teachplan.getCourseid();
        //取父节点
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            //获取课程根节点
            parentid = this.getTeachplanRoot(courseid);
        }
        //根据父节点,1.设置子节点级别。对parentid进行校验
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
        if (!teachplanOptional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String parentGrade = teachplanOptional.get().getGrade();
        if(parentGrade.equals("1")){    //父节点
            teachplan.setGrade("2");
        }else if(parentGrade.equals("2")){
            teachplan.setGrade("3");
        }
        //2.设置父节点，状态
        teachplan.setParentid(parentid);
        teachplan.setStatus("0");   //未发布
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     *获取课程根节点，如果没有则添加根节点
     * @param courseId
     * @return
     */
    private String getTeachplanRoot(String courseId){
        //校验课程id
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        CourseBase courseBase = optional.get();
        //取出课程计划根id
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId,"0");
        if (teachplanList == null || teachplanList.size() == 0){
            //新增一个根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setPname(courseBase.getName());
            teachplan.setParentid("0");
            teachplan.setGrade("1");    //1级
            teachplan.setStatus("0");   //未发布
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }

    /**
     * 根据用户id，课程分页查询，可放条件
     * 利用PageHelper实现自动添加sql分页
     * courseListRequest暂时没用到
     * @param page
     * @param size
     * @return
     */
    @Transactional
    public QueryResponseResult findCourseListPage(int page, int size, CourseListRequest courseListRequest,String uid){
        PageHelper.startPage(page,size);
        //用了沒值的CourseListRequest
        CourseListRequest courseListRequest1 =new CourseListRequest();
//        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest1);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(uid);
        //封装响应数据
        QueryResult queryResult = new QueryResult();
        queryResult.setList(courseListPage.getResult());          //获得数据列表
        queryResult.setTotal(courseListPage.getTotal());   //获得数据数量
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    /**
     * 获取课程基础信息
     * 没有课程则返回空
     */
    @Transactional
    public CourseBase getCourseBaseById(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /**
     * 修改课程消息
     */
    @Transactional
    public ResponseResult updateCourseBase(String courseId,CourseBase courseBase){
        //检验课程id
        CourseBase cbCheck = this.getCourseBaseById(courseId);
        if(cbCheck == null){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        //复制前端不能更改的信息
        courseBase.setId(courseId);
        courseBase.setCompanyId(cbCheck.getCompanyId());
        courseBase.setTeachmode(cbCheck.getTeachmode());
        courseBase.setStatus(cbCheck.getStatus());
        courseBase.setUserId(cbCheck.getUserId());
        courseBaseRepository.save(courseBase);
        return ResponseResult.SUCCESS();
    }

    /**
     * 查询课程营销
     * 在没添加时可能为空，正常现象，不用报异常
     */
    @Transactional
    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /**
     * 修改课程营销
     * 在没有时，先添加
     */
    @Transactional
    public ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket) {
        CourseMarket cmCheck = this.getCourseMarketById(courseId);
        //为空，添加一个
        if(cmCheck == null){
            courseMarketRepository.save(courseMarket);
            String id = courseMarket.getId();
        }
        //否则更新
        cmCheck.setCharge(courseMarket.getCharge());
        cmCheck.setValid(courseMarket.getValid());
        cmCheck.setEndTime(courseMarket.getEndTime());
        cmCheck.setQq(courseMarket.getQq());
        cmCheck.setPrice(courseMarket.getPrice());
        courseMarketRepository.save(cmCheck);
        return ResponseResult.SUCCESS();
    }

    /**
     * 添加课程
     * @param courseBase
     * @return
     */
    @Transactional
    public ResponseResult addCourseBase(CourseBase courseBase,String uid){
        if(courseBase == null || courseBase.getSt()== null || courseBase.getGrade()==null || courseBase.getMt()==null ||
                courseBase.getName()==null || courseBase.getStudymodel()==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        courseBase.setUserId(uid);
        courseBaseRepository.save(courseBase);
        return ResponseResult.SUCCESS();
    }

    /**
     * 保存课程图片
     * @param courseId
     * @param pic
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        if(courseId == null || pic == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        CoursePic coursePic;
        //一个课程只有一个图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            //所以若课程已有图片，更新
            coursePic = optional.get();
        }else{
            //没有图片，新增
            coursePic = new CoursePic();
            coursePic.setCourseid(courseId);
        }
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return ResponseResult.SUCCESS();
    }

    /**
     * 根据课程id查询图片
     * @param courseId
     * @return
     */
    @Transactional
    public CoursePic findCoursePic(String courseId) {
        if (courseId == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId){
        long num = coursePicRepository.deleteByCourseid(courseId);
        if(num > 0){
            return ResponseResult.SUCCESS();
        }
        return ResponseResult.FAIL();
    }

    /**
     * 课程详情页数据模型查询
     * @param id
     * @return
     */
    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(picOptional.get());
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    /**
     * 课程预览
     * @param courseId
     * @return
     */
    public CoursePublishResult preview(String courseId){
        if(courseId == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //查询课程
        CourseBase one = this.getCourseBaseById(courseId);
        if(one == null){
            ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        }
        //封装课程信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setPageName(courseId+".html");
        cmsPage.setPageAliase(one.getName());
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);

        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //获取页面id当文件名，拼装页面url
        String pageId = cmsPageResult.getCmsPage().getPageId();
        return new CoursePublishResult(CommonCode.SUCCESS,previewUrl+pageId);
    }


    /**
     * 课程发布
     */
    @Transactional
    public CoursePublishResult publish (String courseId){
        if(courseId == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //查询课程
        CourseBase one = this.getCourseBaseById(courseId);
        if(one == null){
            ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        }
        //封装课程信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setPageName(courseId+".html");
        cmsPage.setPageAliase(one.getName());
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);

        //远程请求cms发布页面
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //更新课程状态
        CourseBase courseBase = this.saveCoursePubState(courseId);
        //课程索引
        CoursePub coursePub = this.createCoursePub(courseId);
        this.saveCoursePub(courseId,coursePub);
        //课程缓存...

        //页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        //保存课程计划媒资信息
        this.saveTeachplanMediaPub(courseId);

        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    /**
     * 保存课程计划媒资信息
     * @param courseId
     */
    private void saveTeachplanMediaPub(String courseId){
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //将课程计划媒资信息存储待索引表
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for(TeachplanMedia teachplanMedia:teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub =new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }

    /**
     * 更新课程发布状态
     * @param courseId
     * @return
     */
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.getCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase one = courseBaseRepository.save(courseBase);
        return one;
    }

    /**
     * 创建coursePub对象
     */
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();

        //拼接基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //拼接课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //拼接课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }

        //拼接课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);
        return coursePub;

    }

    /**
     * 保存或更新CoursePub
     */
    private CoursePub saveCoursePub(String id, CoursePub coursePub){
        CoursePub coursePubNew = null;
        //查询，有则更新，无则创建
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }
        if(coursePubNew == null){
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        //设置主键
        coursePubNew.setId(id);
        //更新时间戳为最新时间
        coursePub.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePub.setPubTime(date);
        coursePubRepository.save(coursePub);
        return coursePub;

    }

    //保存媒资信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null || teachplanMedia.getMediaId() == null || teachplanMedia.getTeachplanId() == null || teachplanMedia.getMediaUrl() == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划id
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划,校验课程等级，只允许第三等级课程计划关联视频
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLANID_ERROR);
        }
        String grade = optional.get().getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //保存teachplanMedia
        teachplanMediaRepository.save(teachplanMedia);
        return new ResponseResult(CommonCode.SUCCESS);

    }

}
