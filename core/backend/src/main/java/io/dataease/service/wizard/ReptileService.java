package io.dataease.service.wizard;

import io.dataease.plugins.common.util.HttpClientConfig;
import io.dataease.plugins.common.util.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Author: wangjiahao
 * Date: 2022/1/11
 * Description:
 */
@Service
public class ReptileService {
    String blogUrl = "https://blog.fit2cloud.com/categories/dataease";

    String blogBaseUrl = "https://blog.fit2cloud.com";
    //获取最新的前几条数据
    private static int infoCount=5;

    public List lastActive() {
        List result = new ArrayList();
        try {
            HttpClientConfig config = new HttpClientConfig();
            config.setSocketTimeout(5000);
            //爬取最新数据
            Document doc = Jsoup.parse(HttpClientUtil.get(blogUrl, config));
            Elements elementsContent = doc.getElementsByClass("aspect-w-16");
            for(int i = 0;i<infoCount;i++){
                Element info = elementsContent.get(i).children().get(0);
                Map<String, String> infoMap = new HashMap();
                infoMap.put("title",info.attr("title"));
                infoMap.put("href",blogBaseUrl + info.attr("href"));
                result.add(infoMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //ignore
            Map<String, String> infoMap = new HashMap();
            infoMap.put("title","模板学堂丨妙用Tab组件制作多屏仪表板并实现自动轮播");
            infoMap.put("href","https://blog.fit2cloud.com/?p=7c524ae9-69f1-4687-a5a2-f27971047308");
            result.add(infoMap);
        }
        return result;
    }


}
