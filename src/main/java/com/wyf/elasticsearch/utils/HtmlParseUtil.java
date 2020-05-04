package com.wyf.elasticsearch.utils;

import com.wyf.elasticsearch.pojo.JdContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {
    //京东搜索关键词Java的API
    private final String url = "https://search.jd.com/Search?keyword=";
    public List<JdContent> parseJD(String keyword) throws IOException {
        //解析网页，返回的Document就是JS页面对象
        Document docunment = Jsoup.parse(new URL(url+keyword), 3000);
        //获取需要的标签ID
        Element element = docunment.getElementById("J_goodsList");
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        List<JdContent> list = new ArrayList<JdContent>();
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("src");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            list.add(new JdContent(img,price,title));
        }
        return  list;
    }
}
