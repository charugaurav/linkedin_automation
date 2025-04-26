package com.gaur.LinkedInAutomation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaur.LinkedInAutomation.service.SendConnectionRequests;
import com.gaur.LinkedInAutomation.service.SendMessageToFirstConnections;
import com.gaur.LinkedInAutomation.service.SendMessageToNewestConnections;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LinkedInAutomation {

    private static final String CREDENTIALS_FILE = "/Users/gaurgupr/Desktop/Projects/LinkedInAutomation/src/main/java/com/gaur/LinkedInAutomation/credentials.json";
    private static final String AUTH_DATA_PATH = "/Users/gaurgupr/Desktop/Projects/LinkedInAutomation/auth-data";

    private final ExecutorService executor;

    public SendConnectionRequests sendConnectionRequests;
    private SendMessageToFirstConnections sendMessageToFirstConnections;
    private SendMessageToNewestConnections sendMessageToNewestConnections;

    LinkedInAutomation() {
        sendConnectionRequests = new SendConnectionRequests();
        sendMessageToFirstConnections = new SendMessageToFirstConnections();
        sendMessageToNewestConnections = new SendMessageToNewestConnections();
        executor = Executors.newFixedThreadPool(4);
    }

    // Function to send messages
    public void sendMessages() {
        try (Playwright playwright = Playwright.create()) {
            for (int i = 1; i <= 4; i++) {
                BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                        .setHeadless(false);
                BrowserContext browser = playwright.chromium().launchPersistentContext(Paths.get(AUTH_DATA_PATH), options);
                Page page = browser.newPage();

                // Step 1: Login
                String companyName = "american express";

                // Step 2: Extract recruiters from connections
                saveLoginSession(page);
                List<String> recruiterProfiles = sendMessageToFirstConnections.sendMessageToFirstConnections(page, companyName, i);

                for (String profile : recruiterProfiles) {
                    System.out.println("Messaged recruiter: " + profile);
                }

                page.close();
                browser.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessagesToNewestConnections() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(false);
            BrowserContext browser = playwright.chromium().launchPersistentContext(Paths.get(AUTH_DATA_PATH), options);
            Page page = browser.newPage();

            // Step 1: Login
            String companyName = "american express";

            // Step 2: Extract recruiters from connections
            saveLoginSession(page);
            sendMessageToNewestConnections.sendMessageToNewestConnections(page);
            page.close();
            browser.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendConnections() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(false);
            BrowserContext browser = playwright.chromium().launchPersistentContext(Paths.get(AUTH_DATA_PATH), options);
            Page page = browser.newPage();

            // Step 1: Login
            //observe.ai, dream11
            List<String> companyNames = Arrays.asList("juspay","razorpay","swiggy");

            for (String company : companyNames) {

                int totalConnectionsSend = 0;
                LinkedHashSet<String> recruiterProfiles = new LinkedHashSet<>();
                List<Integer> pages = Arrays.asList(1,2,3,4);

                for (int i : pages) {
                    if (totalConnectionsSend >= 15) {
                        page.close();
                        break;
                    }
                    page = saveLoginSession(page);
                    int totalSend = sendConnectionRequests.sendConnectionRequests(page, company, Arrays.asList(i), totalConnectionsSend);
                    totalConnectionsSend += totalSend;
                    System.out.println("Total connection sent to company " + company + ": " + totalConnectionsSend);
                    page.close(); // Close individual page, not entire browser
                }

                for (String s : recruiterProfiles) {
                    System.out.println("Recruiter profile: " + s);
                }
            }
            browser.close(); // Close browser after all iterations
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        LinkedInAutomation obj = new LinkedInAutomation();
        Thread sendConnections = new Thread(() -> obj.sendMessagesToNewestConnections());

//        Thread sendMessages = new Thread(() -> obj.sendMessages());

        sendConnections.start();
//        sendMessages.start();

        try {
            // Wait for both threads to complete
            sendConnections.join();
//            sendMessages.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Page saveLoginSession(Page page) throws IOException {
        try {

            // Try navigating to LinkedIn homepage
            page.navigate("https://www.linkedin.com/feed/");
            page.waitForTimeout(3000);

            // If redirected to login page, then login is needed
            if (page.url().contains("/login")) {
                System.out.println("No active session found, logging in...");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(new File(CREDENTIALS_FILE));
                String email = node.get("email").asText();
                String password = node.get("password").asText();

                page.fill("#username", email);
                page.fill("#password", password);
                page.click("button[type='submit']");
                page.waitForTimeout(5000);

                System.out.println("Login completed and session saved.");
            } else {
                System.out.println("Already logged in. Skipping login.");
            }
            // Let user manually close if needed (e.g., CAPTCHA)
            // context.close(); // You may skip this if you want the session to persist
        } catch (IOException e) {
            return page;
        }
        return page;
    }


//    public String getCompanyCodeFromCompanyName(Page page, String companyName){
//        try{
//            String searchUrl = "https://www.linkedin.com/search/results/companies/?keywords=" + companyName;
//            page.navigate(searchUrl);
//            page.waitForTimeout(4000);
//
//            Locator companyLink = page.locator("a[href*='/company/']:visible").first();
//            String companyUrl = companyLink.getAttribute("href");
//            page.navigate(companyUrl);
//            page.waitForTimeout(4000);
//
//            // Get first search result
//            String htmlContent = page.content();
//            String pattern = "companyIds=(\\d+)";
//            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(htmlContent);
//            String companyId = matcher.find() ? matcher.group(1) : null;
//
//            if (companyId != null) {
//                System.out.println("Company ID for " + companyName + ": " + companyId);
//                return companyId;
//            } else {
//                return null;
//            }
//        } catch(Exception e){
//            return null;
//        }
//    }
}