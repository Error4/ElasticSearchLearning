package com.wyf.elasticsearch.controller;

import com.wyf.elasticsearch.impl.JDContentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class JDcontentController {
    @Autowired
    private JDContentImpl jdContentImpl;

    @GetMapping("/parse/{keyWord}")
    @ResponseBody
    public Boolean parse(@PathVariable("keyWord") String keyWord) throws IOException {
        return  jdContentImpl.parseContent(keyWord);
    }

    @GetMapping("/search/{keyWord}/{pageNo}/{pageSize}")
    @ResponseBody
    public List<Map<String,Object>> search(@PathVariable("keyWord") String keyWord,
                                           @PathVariable("pageNo") int pageNo,
                                           @PathVariable("pageSize") int pageSize ) throws IOException {
        return  jdContentImpl.search(keyWord, pageNo,pageSize);
    }

    @GetMapping("/searchHignLight/{keyWord}/{pageNo}/{pageSize}")
    @ResponseBody
    public List<Map<String,Object>> searchHignLight(@PathVariable("keyWord") String keyWord,
                                           @PathVariable("pageNo") int pageNo,
                                           @PathVariable("pageSize") int pageSize ) throws IOException {
        return  jdContentImpl.searchHignLight(keyWord, pageNo,pageSize);
    }
}
