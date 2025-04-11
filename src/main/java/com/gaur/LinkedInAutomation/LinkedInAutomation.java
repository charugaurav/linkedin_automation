package com.gaur.LinkedInAutomation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaur.LinkedInAutomation.service.SendConnectionRequests;
import com.gaur.LinkedInAutomation.service.SendMessageToFirstConnections;
import com.gaur.LinkedInAutomation.service.SendMessageToNewestConnections;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class LinkedInAutomation {

    private int THREAD_SIZE;
    private ExecutorService executor;

    public SendConnectionRequests sendConnectionRequests;
    private SendMessageToFirstConnections sendMessageToFirstConnections;
    private SendMessageToNewestConnections sendMessageToNewestConnections;

    LinkedInAutomation() {
        sendConnectionRequests = new SendConnectionRequests();
        sendMessageToFirstConnections = new SendMessageToFirstConnections();
        sendMessageToNewestConnections = new SendMessageToNewestConnections();
    }

    // Function to send messages
    public void sendMessages() {
        try (Playwright playwright = Playwright.create()) {
            for (int i = 1; i <= 4; i++) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
                Page page = browser.newPage();

                // Step 1: Login
                String companyName = "american express";

                // Step 2: Extract recruiters from connections
                loginToLinkedIn(page);
                List<String> recruiterProfiles = sendMessageToFirstConnections.sendMessageToFirstConnections(page, companyName, i);
                browser.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessagesToNewestConnections() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            // Step 1: Login
            String companyName = "american express";

            // Step 2: Extract recruiters from connections
            loginToLinkedIn(page);
            sendMessageToNewestConnections.sendMessageToNewestConnections(page);
            browser.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendConnections() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

            // Step 1: Login
            //observe.ai, dream11
            List<String> companyName = Arrays.asList( "razorpay", "navi","siemens");
            Page page;

            for (String company : companyName) {
                Integer totalConnectionsSend = 0;
                LinkedHashSet<String> recruiterProfiles = new LinkedHashSet<>();
                for (int i = 1; i <= 4; i++) {
                    if (totalConnectionsSend >= 15) {
                        break;
                    }
                    page = browser.newPage();
                    loginToLinkedIn(page);
                    int totalSend = sendConnectionRequests.sendConnectionRequests(page, company, i, totalConnectionsSend);
                    totalConnectionsSend += totalSend;
                    System.out.println("Total connection Send to Company " + company + " : " + totalConnectionsSend);
                    page.close(); // Close individual page, not entire browser
                }
                for (String s : recruiterProfiles) {
                    System.out.println(s);
                }
            }
            browser.close(); // Close browser after all iterations
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        LinkedInAutomation obj = new LinkedInAutomation();
        Thread sendConnections = new Thread(new Runnable() {
            @Override
            public void run() {
                obj.sendConnections();
            }
        });

//        Thread sendMessages = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                obj.sendMessages();
//            }
//        });

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

    public static void loginToLinkedIn(Page page) throws IOException {
        page.navigate("https://www.linkedin.com/login");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(new File("/Users/gaurgupr/Desktop/Projects/LinkedInAutomation/src/main/java/com/gaur/LinkedInAutomation/credentials.json"));

        String email = node.get("email").asText();
        String password = node.get("password").asText();

        page.fill("#username", email);
        page.fill("#password", password);
        page.click("button[type='submit']");
        page.waitForTimeout(5000); // Wait for login to complete

        System.out.println("Login successful!");
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
