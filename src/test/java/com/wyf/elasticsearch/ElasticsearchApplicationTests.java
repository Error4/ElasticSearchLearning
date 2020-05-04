package com.wyf.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.wyf.elasticsearch.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ElasticsearchApplicationTests {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    //创建Index
    @Test
    void createIndex() throws IOException {
        // 1、创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("user");
        // 2、客户端执行请求 IndicesClient,请求后获得响应
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    // 测试获取索引,判断其是否存在
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("jd");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 测试删除索引
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("user");
        // 删除
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    //创建文档
    @Test
    void testAddDocument() throws IOException {
        //创建对象
        User user = new User("张三", 3);
        //创建请求
        IndexRequest request = new IndexRequest("user");
        //规则 put /user/_doc/1
        request.id("1");
        //设置超时时间
        request.timeout(TimeValue.timeValueSeconds(1));
        //request.timeout("1s");
        //将我们的数据放入请求 json
        request.source(JSON.toJSONString(user), XContentType.JSON);
        //客户端发送请求 , 获取响应的结果
        IndexResponse indexResponse = client.index(request,
                RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString()); //打印文档的内容
        System.out.println(indexResponse.status()); // 对应我们命令返回的状态 ,第一次创建，返回CREATED
    }
    // 判断文档是否存在
    @Test
    void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("user", "1");
        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 获得文档的信息
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("user", "1");
        GetResponse getResponse = client.get(getRequest,
                RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString()); // 打印文档的内容
        System.out.println(getResponse); // 返回的全部内容是和命令式一样的
    }


    // 更新文档的信息
    @Test
    void testUpdateRequest() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("user", "1");
        updateRequest.timeout("1s");
        User user = new User("张三", 18);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse updateResponse = client.update(updateRequest,
                RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    // 删除文档记录
    @Test
    void testDeleteRequest() throws IOException {
        DeleteRequest request = new DeleteRequest("user","1");
        request.timeout("1s");
    DeleteResponse deleteResponse = client.delete(request,
            RequestOptions.DEFAULT);
    System.out.println(deleteResponse.status());
}

    // 特殊的，实际一般都会批量插入数据！
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userList = new ArrayList<User>();
        userList.add(new User("张三", 3));
        userList.add(new User("李四", 3));
        userList.add(new User("王五", 3));
        for (int i = 0; i < userList.size(); i++) {
            // 批量更新和批量删除，就在这里修改对应的请求就可以了
            bulkRequest.add(new IndexRequest("user").id("" + (i + 1))
                    .source(JSON.toJSONString(userList.get(i)), XContentType.JSON));
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest,
                RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures()); // 是否失败，返回 false 代表
    }

    /*查询
     SearchRequest 搜索请求
     SearchSourceBuilder 条件构造
    HighlightBuilder 构建高亮
    TermQueryBuilder 精确查询
    MatchAllQueryBuilder
    xxxQueryBuilder 与命令行时的查询类型一一对应*/
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("user");
        // 构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询条件，我们可以使用 QueryBuilders 工具来实现
        // QueryBuilders.termQuery 精确
        // QueryBuilders.matchAllQuery() 匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name",
                "张三");
        //MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();

        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest,
                RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("=================================");
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }

    @Test
    void parse() throws IOException {
        String url = "https://search.jd.com/Search?keyword=java&enc=utf-8";
        //解析网页，返回的Document就是JS页面对象
        Document docunment = Jsoup.parse(new URL(url), 3000);
        //获取需要的标签ID
        Element element = docunment.getElementById("J_goodsList");
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            System.out.println(img);
            System.out.println(price);
            System.out.println(title);
        }

    }

}
