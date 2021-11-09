package io.dataease.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Augmenter;
import org.springframework.core.env.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


/**
 * 打印浏览器
 */
public class WebDriverUtils {

    private static String driverPath;


    public static void print(String url, String filePath) {

        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (StringUtils.isBlank(driverPath)) {
            driverPath = CommonBeanFactory.getBean(Environment.class).getProperty("dataease.chrome-driver-path", String.class, "/usr/bin/chromedriver");
        }

        String driverPath = "/usr/local/sbin/chromedriver";
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("no-sandbox");
        options.addArguments("disable-gpu");
        options.addArguments("disable-features=NetworkService");
        options.addArguments("ignore-certificate-errors");
        options.addArguments("silent-launch");
        options.addArguments("disable-application-cache");
        options.addArguments("disable-web-security");
        options.addArguments("no-proxy-server");
        options.addArguments("disable-dev-shm-usage");

        WebDriver driver = null;

        try {
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, driverPath);
            driver = new ChromeDriver(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        driver.get(url);
        WebDriver augmentedDriver = new Augmenter().augment(driver);
        File screenshot = ((TakesScreenshot) augmentedDriver) .getScreenshotAs(OutputType.FILE);

        File file = new File(filePath);
        copy(screenshot, file);
        driver.quit();
    }

    private static void copy(File src, File dest) {
        try
        {
            FileInputStream fis = new FileInputStream(src);//创建输入流对象
            FileOutputStream fos = new FileOutputStream(dest); //创建输出流对象
            byte datas[] = new byte[1024*8];//创建搬运工具
            int len = 0;//创建长度
            while((len = fis.read(datas))!=-1)//循环读取数据
            {
                fos.write(datas,0,len);
            }
            fis.close();//释放资源
            fis.close();//释放资源
        } catch (Exception e){

            e.printStackTrace();
        }

    }
}
