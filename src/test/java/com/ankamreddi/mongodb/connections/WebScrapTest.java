package com.ankamreddi.mongodb.connections;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.bonigarcia.wdm.WebDriverManager;

public class WebScrapTest
{

    WebDriver driver;
    MongoCollection<Document> webCollection;

    @BeforeSuite
    public void connectMongoDB()
    {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("autoDB");
        System.out.println(database.toString());

        // Create Collection
        webCollection = database.getCollection("web");

    }

    @BeforeTest
    public void setUp()
    {
        WebDriverManager.chromedriver().setup();

        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setAcceptInsecureCerts(true);
        ChromeOptions co = new ChromeOptions();
        co.addArguments("--headless");
        co.merge(dc);
        driver = new ChromeDriver(co);

    }

    @DataProvider
    public Object[][] getWebData()
    {
        return new Object[][]
        {
                { "https://amazon.com" },
                { "https://www.flipkart.com" } };
    }

    @Test(dataProvider = "getWebData")
    public void webScrapeTest(String appUrl)
    {
        driver.get(appUrl);
        String url = driver.getCurrentUrl();
        String title = driver.getTitle();

        // Links-Count
        int totalLinks = driver.findElements(By.tagName("a")).size();
        int imagesLinks = driver.findElements(By.tagName("img")).size();

        List<WebElement> linkList = driver.findElements(By.tagName("a"));
        List<String> linkAttrList = new ArrayList<String>();

        List<WebElement> imageList = driver.findElements(By.tagName("img"));
        List<String> imgAttrList = new ArrayList<String>();

        Document d1 = new Document();
        d1.append("url", url);
        d1.append("title", title);
        d1.append("totalLinks", totalLinks);
        d1.append("imagesLinks", imagesLinks);

        for (WebElement link : linkList)
        {
            String hrefValue = link.getAttribute("href");
            linkAttrList.add(hrefValue);
        }
        d1.append("linkAttrList", linkAttrList);

        for (WebElement link : imageList)
        {
            String srcValue = link.getAttribute("src");
            imgAttrList.add(srcValue);
        }
        d1.append("linkAttrList", imgAttrList);

        List<Document> docList = new ArrayList<Document>();
        docList.add(d1);
        webCollection.insertMany(docList);
    }

    @AfterTest
    public void tearDown()
    {
        driver.quit();
    }
}
