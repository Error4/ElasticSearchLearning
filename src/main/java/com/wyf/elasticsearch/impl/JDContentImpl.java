package com.wyf.elasticsearch.impl;

import com.alibaba.fastjson.JSON;
import com.wyf.elasticsearch.pojo.JdContent;
import com.wyf.elasticsearch.service.JDContentService;
import com.wyf.elasticsearch.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JDContentImpl implements JDContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private HtmlParseUtil htmlParseUtil;

    //解析数据放入ES
    @Override
    public boolean parseContent(String keyword) throws IOException {
        List<JdContent> jdContents = htmlParseUtil.parseJD(keyword);
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("1m");
        for (JdContent jdContent : jdContents) {
            bulkRequest.add(new IndexRequest("jd").source(JSON.toJSONString(jdContent), XContentType.JSON));
        }
        BulkResponse bulkResponse= restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }
    //获取数据实现搜索
    @Override
    public List<Map<String,Object>> search(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo<1){
            pageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //匹配数据
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.timeout(new TimeValue(50, TimeUnit.SECONDS));
        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            list.add(hit.getSourceAsMap());
        }
        return list;
    }

    @Override
    public List<Map<String,Object>> searchHignLight(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo<1){
            pageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //匹配数据
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.timeout(new TimeValue(50, TimeUnit.SECONDS));
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            //解析高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            //将原来的字段替换为高亮的字段设置
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if (title!=null){
                Text[] fragments = title.fragments();
                String temValue = "";
                for (Text fragment : fragments) {
                    temValue+=fragment;
                }
                sourceAsMap.put("title",temValue);//替换
            }
            list.add(sourceAsMap);
        }
        return list;
    }

}
