package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * GridFS存取文件测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {


    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 使用GridFsTemplate存储文件
     * @throws FileNotFoundException
     */
    @Test
    public void testSet() throws FileNotFoundException {
        //要存储的文件
        File file = new File("C:/Users/Administrator/Desktop/index_category.ftl");
        //定义输入流
        FileInputStream inputStream = new FileInputStream(file);
        //向GridFs存储文件
        ObjectId objectId = gridFsTemplate.store(inputStream,"course.ftl");
        //得到文件ID
        System.out.println(objectId.toString());
    }

    /**
     * 查询文件
     * @throws IOException
     */
    @Test
    public void testGet() throws IOException {
        String fileId = "5ec14f4aa25bf34380425bc1";
        //根据id查文件
        com.mongodb.client.gridfs.model.GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建，用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        //获取流中的数据
        String s = IOUtils.toString(gridFsResource.getInputStream(),"UTF-8");
        System.out.println(s);
    }

    /**
     * 删除文件
     */
    @Test
    public void testDelFile(){
        //根据文件id删除fs.files和fs.chunks中的记录
        String fileId = "5ec14f4aa25bf34380425bc1";
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(fileId)));
    }
}