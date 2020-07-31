package com.blackboard.testing.lambda;

import static com.blackboard.testing.lambda.logger.LoggerContainer.LOGGER;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;

import com.blackboard.testing.db.DynamodbClient;
import com.blackboard.testing.driver.LambdaWebDriverThreadLocalContainer;
import com.blackboard.testing.lambda.logger.Logger;
import com.blackboard.testing.lambda.logger.LoggerContainer;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blackboard.testing.models.FbGroupContent;
import com.blackboard.testing.models.FbPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class LambdaTestHandler implements RequestHandler<TestRequest, TestResult> {

    private static TestResult testResult;

    public LambdaTestHandler() {
        testResult = new TestResult();
    }

    public TestResult handleRequest(TestRequest testRequest, Context context) {
        LoggerContainer.LOGGER = new Logger(context.getLogger());

        log.info("Starting .........");

        long checkpoint = DynamodbClient.getCheckpoint().getValue();

        LambdaWebDriverThreadLocalContainer container = new LambdaWebDriverThreadLocalContainer();

        WebDriver webDriver = container.getWebDriver();

        try {
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMinutes(5).getSeconds());

            webDriver.get("https://www.facebook.com/groups/950713841719944");

            webDriver.findElement(By.name("email")).sendKeys("ntson2@yahoo.com");
            webDriver.findElement(By.name("pass")).sendKeys("ilovesanmin" + Keys.ENTER);

            Thread.sleep(10000);
            Actions action = new Actions(webDriver);
            action.sendKeys(Keys.ESCAPE).perform();

            List<WebElement> seeMore = wait.until(presenceOfAllElementsLocatedBy(By.linkText("See More")));

            for (WebElement element : seeMore) {
                Thread.sleep(5000);
                element.click();
            }

            List<WebElement> webElements = wait.until(presenceOfAllElementsLocatedBy(By.className("_3ccb")));

            log.info("There are {} elements", webElements.size());

            Collections.reverse(webElements);

            for (WebElement element : webElements) {
                checkpoint = processOnePost(element, checkpoint);
                log.info("Saving checkpoint {} --------------------------------------------", checkpoint);
            }

        } catch (Exception e) {
            LOGGER.log(e);
        } finally {
            webDriver.quit();
        }

        return new TestResult();
    }

    private long processOnePost(WebElement element, long checkpoint) {
        try {
            WebElement timeElement = element.findElement(By.cssSelector("._5ptz.timestamp.livetimestamp"));
            String timeString = timeElement.getAttribute("title");
            String postedTimestampString = timeElement.getAttribute("data-utime");
            long postedTimestamp = Long.getLong(postedTimestampString);

            if (postedTimestamp < checkpoint) {
                // already processed this post
                return checkpoint;
            }
            List<WebElement> profile = element.findElements(By.className("profileLink"));
            String authorName;
            String authorProfile;
            String taggedName = null;
            String taggedLink = null;

            if (profile.size() == 0) {
                WebElement authorElement = element.findElement(By.cssSelector(".fwb.fcg")).findElement(By.cssSelector("a"));
                authorName = authorElement.getText();
                authorProfile = authorElement.getAttribute("href");
            } else {
                authorName = profile.get(0).getText();
                authorProfile = profile.get(0).getAttribute("href");

                if (profile.size() > 1) {
                    taggedName = profile.get(1).getText();
                    taggedLink = profile.get(1).getAttribute("href");
                }
            }

            String locationName = null;
            String locationLink = null;

            List<WebElement> contentElement1 = element.findElements(By.cssSelector(".text_exposed_root.text_exposed"));
            List<WebElement> contentElement2 = element.findElements(By.cssSelector("._5pbx.userContent._3576"));

            String content = contentElement1.isEmpty() ? (contentElement2.isEmpty() ? element.findElement(By.cssSelector(".mtm._5pco")).getText()
                    : contentElement2.get(0).getText())
                    : contentElement1.get(0).getText();

            long timestamp = System.currentTimeMillis();

            List<WebElement> photoElements = element.findElements(By.cssSelector("._5dec._xcx"));
            List<String> photos = photoElements.stream().map(e -> e.getAttribute("href")).collect(Collectors.toList());

            String link = null;
            List<WebElement> links = element.findElements(By.className("_5pcq"));
            if (links.isEmpty()) {
                link = element.findElement(By.cssSelector(".fsm.fwn.fcg")).getAttribute("href");
            } else {
                link = links.get(0).getAttribute("href");
            }

            FbGroupContent fbGroupContent = FbGroupContent.builder()
                    .id(UUID.randomUUID().toString())
                    .content(content)
                    .author(FbPage.builder().text(authorName).link(authorProfile).build())
                    .location(FbPage.builder().text(locationName).link(locationLink).build())
                    .tagged(FbPage.builder().text(taggedName).link(taggedLink).build())
                    .savedTimestamp(timestamp)
                    .postedTimeString(timeString)
                    .photos(photos)
                    .link(link)
                    .postedTimestamp(postedTimestamp)
                    .build();

            log.info("Scrapped: {}", fbGroupContent);

            DynamodbClient.insert(fbGroupContent);
            DynamodbClient.saveCheckpoint(postedTimestamp);
            return postedTimestamp;
        } catch (Exception e) {
            log.error("Error processing webelements {}", element.getText(), e);
            return checkpoint;
        }
    }

    public static void addAttachment(String fileName, byte[] attachment) {
        testResult.getAttachments().put(fileName, attachment);
    }
}
