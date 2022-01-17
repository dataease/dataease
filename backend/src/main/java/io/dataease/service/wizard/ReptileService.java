package io.dataease.service.wizard;

import io.dataease.commons.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2022/1/11
 * Description:
 */
@Service
public class ReptileService {
    String blogUrl = "https://blog.fit2cloud.com/?cat=321";

    public Map<String, String> lastActive() {
        Map<String, String> result = new HashMap();
        try {
            //爬取最新数据
            Document doc = Jsoup.parse(HttpClientUtil.get(blogUrl, null));
            Elements elementsContent = doc.getElementsByAttributeValue("rel", "bookmark");
            Elements elementsTime = doc.getElementsByTag("time");
            Element lastInfo = elementsContent.get(0);
            result.put("title",lastInfo.attr("title"));
            result.put("href",lastInfo.attr("href"));
            result.put("time",elementsTime.get(0).childNode(0).outerHtml());
        } catch (Exception e) {
            //ignore
            result.put("title","支持移动端展示，数据源新增对DB2的支持，DataEase开源数据可视化分析平台v1.6.0发布");
            result.put("href","https://blog.fit2cloud.com/?p=3200");
            result.put("time","2022年1月10日");
        }

        return result;

    }


}
