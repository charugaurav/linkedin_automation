//package com.gaur.LinkedInAutomation.service;
//
//import com.microsoft.playwright.Locator;
//import com.microsoft.playwright.Page;
//
//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.List;
//
//public class SendMessageToNewestConnections {
//    public List<String> sendMessageToNewestConnections(Page page){
//        List<String> connectionsSendMessage = new ArrayList<>();
////        String companyId = getCompanyCodeFromCompanyName(page, companyName);
//        LinkedHashSet<String> recruiterProfiles = new LinkedHashSet<>();
//
////        for(int i=1; i<=9; i++){
//        try {
//            String connectionURL = "https://www.linkedin.com/mynetwork/invite-connect/connections/";
////                String connectionURL = "https://www.linkedin.com/search/results/people/?currentCompany=%5B%22" + companyId + "%22%5D&network=%5B%22F%22%5D&origin=FACETED_SEARCH&page="+i;
//            page.navigate(connectionURL);
//            page.waitForTimeout(5000); // Wait for the page to load
//        } catch (Exception e) {
//            System.out.println("Error navigating to Connection search page: " + e.getMessage());
//            return connectionsSendMessage; // Skip execution if company page fails
//        }
//
//        try {
//            for(int i = 0 ; i< 10; i++){
//                // Step 4: Find the recruiter list
//                List<Locator> recruiters = page.locator("//a[contains(@href, '/in/')]").all();
//
//                for (Locator recruiter : recruiters) {
//                    try {
//                        String profileUrl = recruiter.getAttribute("href");
//                        String recruiterFirstName = recruiter.innerText().trim().split("\n")[0].split(" ")[0].toLowerCase(); // Extract text
//
//                        if (profileUrl != null && profileUrl.toLowerCase().contains(recruiterFirstName)) {
//                            recruiterProfiles.add(profileUrl);
//                            System.out.println("Found Recruiter: " + profileUrl);
//                        }
//
//                    } catch (Exception e) {
//                        System.out.println("Error extracting recruiter details: " + e.getMessage());
//                        continue; // Skip to next recruiter
//                    }
//                }
//                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
//                page.waitForTimeout(5000);
//            }
//        } catch (Exception e) {
//            System.out.println("Error retrieving recruiter list: " + e.getMessage());
//            return connectionsSendMessage; // Skip execution if list retrieval fails
//        }
//        return null;
//    }
//}
//
//

package com.gaur.LinkedInAutomation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static com.gaur.LinkedInAutomation.util.LinkedinAutomationConstants.messageTemplateForRecruiter;

public class SendMessageToNewestConnections {
    public void sendMessageToNewestConnections(Page page) {
        LinkedHashSet<String> recruiterProfiles = new LinkedHashSet<>();

        try {
            String connectionURL = "https://www.linkedin.com/mynetwork/invite-connect/connections/";
            page.navigate(connectionURL);
            page.waitForTimeout(5000); // Wait for page to load
        } catch (Exception e) {
            System.out.println("Error navigating to Connection search page: " + e.getMessage());
        }

        try {
            // Scroll using keyboard (PageDown)
            for (int i = 0; i < 20; i++) {
                Locator loadMore = page.locator("span", new Page.LocatorOptions().setHasText("Load more"));
                if (loadMore.count() > 0) {
                    System.out.println("Clicking 'Load more' button...");
                    loadMore.first().click();
                    page.waitForTimeout(1000); // wait for new connections to load
                }
            }

            // After scrolling, collect recruiter profiles
            List<Locator> recruiters1 = page.locator("//a[contains(@href, '/in/')]").all();
            HashSet<Locator> recruiters = new HashSet<>(recruiters1);
            page.waitForTimeout(8000);

            for (Locator recruiter : recruiters) {
                try {
                    String profileUrl = recruiter.getAttribute("href");
                    String recruiterFirstName = recruiter.innerText().trim().split("\n")[0].split(" ")[0].toLowerCase();

                    if (profileUrl != null && profileUrl.toLowerCase().contains(recruiterFirstName)) {
                        recruiterProfiles.add(profileUrl);
                        System.out.println("Found Recruiter: " + profileUrl);
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting recruiter details: " + e.getMessage());
                }
            }

            sendMessagesInBatches(recruiterProfiles.stream().toList());
        } catch (Exception e) {
            System.out.println("Error retrieving recruiter list: " + e.getMessage());
        }
    }

    public void sendMessagesInBatches(List<String> recruiterUrls) throws IOException {
        int batchSize = 20;
        int total = recruiterUrls.size();
        Playwright playwright = Playwright.create();

        for (int i = 0; i < total; i += batchSize) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            // 1. Get the sublist (group of 10 recruiters)
            Page page = browser.newPage();
            loginToLinkedIn(page);
            List<String> batch = recruiterUrls.subList(i, Math.min(i + batchSize, total));
            sendMessages(batch, page);

            // 4. Close the page after use
            page.close();
            // optional: only if you're creating a new context
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

    private void sendMessages(List<String> recruiterProfiles, Page page) {
        for (String profileUrl : recruiterProfiles) {
            try {
                page.navigate(profileUrl);
                page.waitForTimeout(5000);
            } catch (Exception e) {
                System.out.println("Error navigating to recruiter profile: " + e.getMessage());
                continue; // Skip to next recruiter
            }

            if(true){
                String recruiterName = null;
                try {
                    Locator nameTag = page.locator("//a[contains(@href, '/overlay/about-this-profile/') and contains(@aria-label, '')]");
                    recruiterName = nameTag.getAttribute("aria-label");
                    System.out.println("Recruiter Name: " + recruiterName);

                    if (recruiterName == null) {
                        System.out.println("Skipping profile: Could not extract recruiter name.");
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting recruiter name: " + e.getMessage());
                    continue; // Skip to next recruiter
                }

                try {
                    String firstName = recruiterName.split(" ")[0];

                    String connectButtonSelector = String.format(
                            "//button[contains(@aria-label, 'Message %s') and contains(@class, 'artdeco-button--primary')]", firstName
                    );
                    List<Locator> connectButtons = page.locator(connectButtonSelector).all();
                    System.out.println("Found " + connectButtons.size() + " Connect buttons for " + recruiterName);

                    for (Locator connectButton : connectButtons) {
                        try {
                            System.out.println("Message button Visible: " + connectButton.isVisible());
                            if (connectButton.isVisible()) {
                                connectButton.click();
                                page.waitForTimeout(5000);
                            }
                        } catch (Exception e) {
                            System.out.println("Error clicking message button: " + e.getMessage());
                            continue; // Skip to next recruiter
                        }

                        try {
                            Locator messageBox = page.locator("div.msg-form__contenteditable");
                            String messageForRecruiter = String.format(messageTemplateForRecruiter, firstName, "");
                            if (messageBox.isVisible()) {
                                messageBox.fill(messageForRecruiter);
                                System.out.println("Message typed for: " + recruiterName);
                                page.waitForTimeout(2000);
                            } else {
                                System.out.println("Message box not found for: " + recruiterName);
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("Error typing message: " + e.getMessage());
                            continue;
                        }

                        try {
                            Locator attachButton = page.locator("button:has-text('Attach a file')");
                            if (attachButton.isVisible()) {
                                attachButton.click();
                                page.waitForTimeout(5000);

                                // Upload resume from local storage
                                page.setInputFiles("input[type='file']", Paths.get("/Users/gaurgupr/Desktop/Projects/LinkedInAutomation/src/main/resources/Gaurav Gupta - Resume.pdf"));
                                System.out.println("Resume attached for: " + recruiterName);
                                page.waitForTimeout(5000);
                            } else {
                                System.out.println("Attachment button not found for: " + recruiterName);
                            }
                        } catch (Exception e) {
                            System.out.println("Error attaching resume: " + e.getMessage());
                            continue;
                        }

                        try {
                            Locator sendButton = page.locator("//button[contains(@class, 'msg-form__send-button artdeco-button artdeco-button--1')]");
                            if (sendButton.isVisible()) {
                                sendButton.click();
                                page.waitForTimeout(5000);
                                System.out.println("Message sent to: " + recruiterName);

                                Locator closeButton = page.locator("//button[contains(@class, 'msg-overlay-bubble-header__control artdeco-button artdeco-button--circle')]");
                                if (closeButton.isVisible()) {
                                    closeButton.click();
                                    page.waitForTimeout(5000);
                                }
                                System.out.println(profileUrl);
                            } else {
                                System.out.println("Send button not found for: " + recruiterName);
                            }
                        } catch (Exception e) {
                            System.out.println("Error sending message: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Unexpected error in processing recruiter: " + e.getMessage());
                    continue; // Skip to next recruiter
                }
            }
        }
    }
}
