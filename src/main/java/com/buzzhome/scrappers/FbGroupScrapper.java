package com.buzzhome.scrappers;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;

import com.buzzhome.Constants;
import com.buzzhome.db.DynamodbClient;
import com.buzzhome.driver.LambdaWebDriverThreadLocalContainer;
import com.buzzhome.helpers.PriceDataParser;
import com.buzzhome.helpers.DistrictDataParser;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.buzzhome.models.Comment;
import com.buzzhome.models.FbGroupContent;
import com.buzzhome.models.FbPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class FbGroupScrapper implements RequestHandler<Object, String> {

    public String handleRequest(Object request, Context context) {

        log.info("Starting .........");

        long originalCheckpoint = DynamodbClient.getCheckpoint().getValue();

        LambdaWebDriverThreadLocalContainer container = new LambdaWebDriverThreadLocalContainer();

        WebDriver webDriver = container.getWebDriver();

        try {
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMinutes(5).getSeconds());

            webDriver.get(Constants.FB_GROUP);

            webDriver.findElement(By.name("email")).sendKeys(Constants.USERNAME);
            webDriver.findElement(By.name("pass")).sendKeys(Constants.PASSWORD+ Keys.ENTER);

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

            long checkpoint = originalCheckpoint;
            int numProcessed = 0;

            for (WebElement element : webElements) {
                long postedTimestamp = processOnePost(element, originalCheckpoint);
                if (postedTimestamp != -1) {
                    numProcessed++;
                }
                if (postedTimestamp > checkpoint) {
                    checkpoint = postedTimestamp;
                }
            }

            log.info("Processed {} records, saving checkpoint {} --------------------------------------------", numProcessed, checkpoint);
            if (numProcessed == webElements.size()) {
                log.warn("Possible lost of data due to low frequency scrapping");
            }
            DynamodbClient.saveCheckpoint(checkpoint);

        } catch (Exception e) {
            log.error("Error scrapping", e);
        } finally {
            webDriver.quit();
        }

        return "success";
    }

    private long processOnePost(WebElement element, long checkpoint) {
        try {
            WebElement timeElement = element.findElement(By.cssSelector("._5ptz.timestamp.livetimestamp"));
            String postedTimeString = timeElement.getAttribute("title");
            String postedTimestampString = timeElement.getAttribute("data-utime");
            long postedTimestamp = Long.parseLong(postedTimestampString);

            if (postedTimestamp <= checkpoint) {
                // already processed this post
                log.warn("Already processed this record with timestamp = {} and checkpoint = {}", postedTimestamp, checkpoint);
                return -1;
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

            double price = PriceDataParser.getPriceInUSD(content);
            Optional<String> district = DistrictDataParser.getDistrict(content);

            List<WebElement> commentWebElements = element.findElements(By.className("_72vr"));
            List<Comment> comments = commentWebElements.stream().map(
                    e -> Comment.builder()
                            .commenter(e.findElement(By.className("_6qw4")).getText())
                            .content(e.findElement(By.className("_3l3x")).getText())
                            .build())
                    .collect(Collectors.toList());

            FbGroupContent fbGroupContent = new FbGroupContent();
            fbGroupContent.setContent(content);
            fbGroupContent.setAuthor(toFbPageObject(authorName, authorProfile));
            fbGroupContent.setLocation(toFbPageObject(locationName, locationLink));
            fbGroupContent.setTagged(toFbPageObject(taggedName, taggedLink));
            fbGroupContent.setSavedTimestamp(timestamp);
            fbGroupContent.setPostedTimeString(postedTimeString);
            fbGroupContent.setPhotos(photos);
            fbGroupContent.setLink(link);
            fbGroupContent.setPostedTimestamp(postedTimestamp);
            fbGroupContent.setPrice(price);
            fbGroupContent.setComments(comments);
            if (district.isPresent()) {
                fbGroupContent.setDistrictLocation(district.get());
            }

            log.info("Scrapped: {}", fbGroupContent);

            DynamodbClient.insert(fbGroupContent);

            return postedTimestamp;
        } catch (Exception e) {
            log.error("Error processing webelements {}", element.getText(), e);
            return -1;
        }
    }

    private FbPage toFbPageObject(String authorName, String authorProfile) {
        FbPage fbPage = new FbPage();
        fbPage.setLink(authorProfile);
        fbPage.setText(authorName);

        return fbPage;
    }
}
