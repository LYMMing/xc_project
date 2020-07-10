package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    //搜索type下的全部记录
    @Test
    public void testSearchAll() throws IOException, ParseException {
        //1.设置搜索条件前
        //创建查询对象，设置索引库名
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //2.设置搜索条件
        //设置搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //source源字段过虑，第一个参数为查询的字段，第二个参数为不查询的字段
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel","timestamp"}, new String[]{});
        //给查询对象设置搜索源
        searchRequest.source(searchSourceBuilder);

        //3.执行搜索,获得结果
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //得到总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的多个文档
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();  //文档主键
            String type = hit.getType();
            String id = hit.getId();        //文档id
            float score = hit.getScore();      //文档匹配度
            String sourceAsString = hit.getSourceAsString();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            //日期格式化对象
            SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
        }
    }

    /**
     * 分页查询
     */
    @Test
    public void testPage() throws IOException {
        //1.设置搜索条件前
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //2.设置搜索条件
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //分页查询，设置起始下标，从0开始
        searchSourceBuilder.from(0);
        //每页显示个数
        searchSourceBuilder.size(2);
        searchRequest.source(searchSourceBuilder);

        //3.执行搜索,获得结果
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    //精确搜索,不会对查询内容进行关键字分解
    @Test
    public void testTerm() throws IOException {
        //1.设置搜索条件前
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //2.设置搜索条件，精确查询
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        searchRequest.source(searchSourceBuilder);

        //3.执行搜索,获得结果
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    //根据id查询
    @Test
    public void testSearchById() throws IOException {
        //1.设置搜索条件前
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //2.设置搜索条件
        String[] split = new String[]{"1","2"};     //同时查询多个id的文档
        List<String> idList = Arrays.asList(split);
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", idList));
        searchRequest.source(searchSourceBuilder);

        //3.执行搜索,获得结果
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    /**
     * 根据关键字搜索matchQuery
     * 会对查询内容进行关键字分解
     * 同时可设置匹配的关键字个数，通过Operator，和minimumShouldMatch
     * @throws IOException
     */
    @Test
    public void testMatchQuery() throws IOException {
        //1.设置搜索条件前
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //2.设置搜索条件，匹配关键字,
        // Operator.OR，Operator.AND，or是只需要匹配到一个词，AND需要匹配到所有词
        // minimumShouldMatch("80%")设置匹配占比，如果分3个词，3*0.8=2.4=2，向下取整，代表至少要匹配到2个词
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发").operator(Operator.AND));
//        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发").minimumShouldMatch("50%"));
        searchRequest.source(searchSourceBuilder);

        //3.执行搜索,获得结果
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    /**
     * 多字段关键字匹配
     * 还可以设置每个字段匹配的权重
     * @throws IOException
     */
    @Test
    public void testMoreField() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //multiMatchQuery
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring框架", "name", "description")
                .field("name",10));//提升boost
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    /**
     * 布尔查询
     * 实现将多个查询组合起来。
     * 3个参数must、should、must_not
     * must：表示必须，多个查询条件必须都满足。（通常使用must）
     * should：表示或者，多个查询条件只要有一个满足即可。
     * must_not：表示都不满足。
     * @throws IOException
     */
    @Test
    public void testBoolQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //multiMatchQuery，关键字查询
        MultiMatchQueryBuilder mmb = QueryBuilders.multiMatchQuery("spring框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        //TermQuery精确查询
        TermQueryBuilder tqb = QueryBuilders.termQuery("studymodel", "201001");
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //合并关键字查询，准确查询
        boolQueryBuilder.must(mmb);
        boolQueryBuilder.must(tqb);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    /**
     * filter过滤器
     * 可以实现项匹配，范围匹配，效率比前面的都高，因为不用计算得分
     * 通过bool查询器，设置
     * @throws IOException
     */
    @Test
    public void testFilter() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //multiMatchQuery，关键字查询
        MultiMatchQueryBuilder mmb = QueryBuilders.multiMatchQuery("spring框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //合并关键字查询，准确查询
        boolQueryBuilder.must(mmb);
        //添加过滤器
        //一个是类似精确查询,一个是范围查询
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    /**
     * 排序
     * @throws IOException
     */
    @Test
    public void testSort() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);
        //排序先按studymodel降序，相同的按price升序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
    }

    /**
     * 高亮，关键字
     * @throws IOException
     */
    @Test
    public void testHighLight() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "spring开发").operator(Operator.AND));
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eshight'>");//设置前缀
        highlightBuilder.postTags("</font>");//设置后缀
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields!=null){
                HighlightField nameField = highlightFields.get("name");
                if(nameField!=null){
                    //标记高亮后，长内容可能分段，需要组合
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }
                    //有就修改
                    name = stringBuffer.toString();
                }
            }
        }
    }
}
