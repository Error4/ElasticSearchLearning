package com.wyf.elasticsearch.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface JDContentService {
    //解析数据放入ES
    public boolean parseContent(String keywords) throws IOException;
    //执行搜索
    public List<Map<String,Object>> search(String keyword, int pageNo, int pageSize) throws IOException;
    //执行高亮搜索
    public List<Map<String,Object>> searchHignLight(String keyword, int pageNo, int pageSize) throws IOException;

}
