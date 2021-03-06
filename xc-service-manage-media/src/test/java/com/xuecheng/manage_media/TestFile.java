package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {

    /**
     * 测试分块
     */
    @Test
    public void testChunk(){

        try {
            File sourceFile = new File("F:/develop/video/lucene.mp4");
            String chunkPath = "F:/develop/video/chunk/";
            File chunkFolder = new File(chunkPath);
            if(!chunkFolder.exists()) {
                chunkFolder.mkdirs();
            }
            //分块大小
            long chunkSize = 1024*1024*1;
            //分块数量
            long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize );
            //缓冲区大小
            byte[] b = new byte[1024];
            //使用RandomAccessFile访问文件
            RandomAccessFile raf_read = null;
            raf_read = new RandomAccessFile(sourceFile, "r");
            //分块
            for(int i=0;i<chunkNum;i++){
                //创建分块文件
                File file = new File(chunkPath+i);
//                boolean newFile = file.createNewFile();
//                if(newFile){
                    //向分块文件中写数据
                    RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                    int len = -1;
                    while((len = raf_read.read(b))!=-1){
                        raf_write.write(b,0,len);
                        if(file.length()>=chunkSize){
                            break;
                        }
                    }
                    raf_write.close();
//                }
            }
            raf_read.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("F:/develop/video/chunk/");
        //合并文件
        File mergeFile = new File("F:/develop/video/lucene_cp.mp4");
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        //创建新的合并文件
        mergeFile.createNewFile();
        //用于写文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //指针指向文件顶端
        raf_write.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        //分块列表
        File[] fileArray = chunkFolder.listFiles();
        // 转成集合，便于排序
        List<File> fileList = new ArrayList<File>(Arrays.asList(fileArray));
        // 从小到大排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            }
        });
        //合并文件
        for(File chunkFile:fileList){
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            while((len=raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();

    }

}
