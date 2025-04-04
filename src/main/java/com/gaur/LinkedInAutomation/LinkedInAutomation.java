package com.gaur.LinkedInAutomation;

import com.microsoft.playwright.*;
import net.bytebuddy.dynamic.scaffold.MethodGraph;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static com.gaur.LinkedInAutomation.util.LinkedinAutomationConstants.messageTemplateForNewConnection;
import static com.gaur.LinkedInAutomation.util.LinkedinAutomationConstants.messageTemplateForRecruiter;


public class LinkedInAutomation {

    private static int THREAD_SIZE;
    private static ExecutorService executor;

    // Function to send messages
    public void sendMessages() {
        try (Playwright playwright = Playwright.create()) {
            for(int i = 1; i <= 4; i++){
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
                Page page = browser.newPage();

                LinkedInAutomation automation = new LinkedInAutomation();

                // Step 1: Login
                String companyName = "Wayfair";

                // Step 2: Extract recruiters from connections
                page = browser.newPage();
                automation.loginToLinkedIn(page, "gupta.gaurav4188@gmail.com", "");
                List<String> recruiterProfiles = automation.sendMessageToFirstConnections(page, companyName, i);
                browser.close();
            }
        }
    }

    public void sendConnections() {
        try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
                LinkedInAutomation automation = new LinkedInAutomation();

                // Step 1: Login
                //observe.ai, dream11
            List<String> companyName = Arrays.asList("Microsoft", "Visa", "Wayfair");
            Page page;

            for(String company: companyName){
                page = browser.newPage();
                automation.loginToLinkedIn(page, "gupta.gaurav4188@gmail.com", "noida@4188");
                automation.sendConnectionRequests(page, company);
            }
                browser.close();

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

    public void sendConnectionRequests(Page page, String companyName) {
        List<String> connectionRequestsSent = new ArrayList<>();
        System.out.println("Searching for recruiters at: " + companyName);

        // Step 1: Navigate to LinkedIn search page for recruiters
        try {
            String searchUrl = "https://www.linkedin.com/search/results/people/?keywords=Talent%20Acquisition%20" + companyName;
            page.navigate(searchUrl);
            page.waitForTimeout(5000);
        } catch (Exception e) {
            System.out.println("Error navigating to search page: " + e.getMessage());
            return;
        }

        LinkedHashSet<String> recruiterProfiles = new LinkedHashSet<>();

        // Step 2: Loop through multiple pages (up to 10 pages)
        for (int pageNum = 1; pageNum <= 4; pageNum++) {
            try {
                System.out.println("Scraping page: " + pageNum);

                // Collect recruiter profiles from the current page
                // Scroll and click "Next" button
                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                page.waitForTimeout(3000);

                List<Locator> recruiters = page.locator("//a[contains(@href, '/in/')]").all();
                for (Locator recruiter : recruiters) {
                    String profileUrl = recruiter.getAttribute("href");
                    String recruiterFirstName = recruiter.innerText().trim().split("\n")[0].split(" ")[0].toLowerCase(); // Extract text

                    if (profileUrl != null && profileUrl.toLowerCase().contains(recruiterFirstName)) {
                        recruiterProfiles.add(profileUrl);
                        System.out.println("Found Recruiter: " + profileUrl);
                    }
                }

                Locator nextPageButton = page.locator("//button[contains(@aria-label,'Next') and contains(@class,'artdeco-pagination__button--next')]");
                if (nextPageButton.isVisible()) {
                    nextPageButton.click();
                    page.waitForTimeout(5000);
                } else {
                    System.out.println("No more pages found. Stopping pagination.");
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error scraping page " + pageNum + ": " + e.getMessage());
                continue; // Skip to the next page
            }
        }

        System.out.println("Total recruiters found: " + recruiterProfiles.size());
        recruiterProfiles.remove("https://www.linkedin.com/in/gaurav-gupta-aertc127k/recent-activity/");

        // Step 3: Send connection requests
        for (String profileUrl : recruiterProfiles) {
            if(connectionRequestsSent.size() >= 10){
                connectionRequestsSent.forEach(url ->{
                    System.out.println(url);
                });
                return;
            }
            try {
                page.navigate(profileUrl);
                page.waitForTimeout(5000);

                Locator companyTagButton = page.locator("button[aria-label*='Current company']");
                String companyTag = companyTagButton.getAttribute("aria-label");

                if(companyTag.toLowerCase().contains(companyName.toLowerCase())){
                    Locator nameTag = page.locator("//a[contains(@href, '/overlay/about-this-profile/') and contains(@aria-label, '')]");
                    String recruiterName = nameTag.getAttribute("aria-label");

                    if (recruiterName == null) {
                        System.out.println("Skipping profile: Could not extract recruiter name.");
                        continue;
                    }

                    String connectButtonSelector = String.format(
                            "//button[contains(@aria-label, 'Invite %s to connect') and contains(@class, 'artdeco-button--primary')]",
                            recruiterName
                    );

                    List<Locator> connectButtons = page.locator(connectButtonSelector).all();
                    System.out.println("Found " + connectButtons.size() + " Connect buttons for " + recruiterName);

                    if (connectButtons.isEmpty()) {
                        // Try "More Actions" dropdown
                        try {
                            List<Locator> moreActionButtons = page.locator("//button[contains(@aria-label,'More actions') and contains(@class,'artdeco-dropdown__trigger')]").all();
                            Locator moreActionButton = null;
                            for (Locator tempL : moreActionButtons) {
                                if (tempL.isVisible()) {
                                    moreActionButton = tempL;
                                    break;
                                }
                            }
                            if (moreActionButton != null) {
                                moreActionButton.click();
                                page.waitForTimeout(3000);
                                System.out.println("More Action Button Clicked");

                                String innerConnect = String.format(
                                        "//div[contains(@aria-label,'Invite %s to connect') and contains(@role,'button')]",
                                        recruiterName
                                );
                                List<Locator> innerConnectButtons = page.locator(innerConnect).all();
                                Locator innerConnectButton = null;
                                for (Locator tempL : innerConnectButtons) {
                                    if (tempL.isVisible()) {
                                        innerConnectButton = tempL;
                                        break;
                                    }
                                }
                                if (innerConnectButton != null) {
                                    innerConnectButton.click();
                                    page.waitForTimeout(3000);

                                    Locator addNoteButton = page.locator("button:has-text('Add a note')");
                                    if (addNoteButton.isVisible()) {
                                        addNoteButton.click();
                                        page.waitForTimeout(3000);

                                        // Fill in the note
                                        String message = messageTemplateForNewConnection;
                                        page.fill("textarea[name='message']", message);

                                        // Click "Send"
                                        page.click("button:has-text('Send')");
                                        page.waitForTimeout(3000);

                                        System.out.println("Connection request sent to: " + profileUrl);
                                        connectionRequestsSent.add(profileUrl);
                                    }
                                } else {
                                    System.out.println("No connect button for: " + profileUrl);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error clicking 'More Actions' for " + recruiterName + ": " + e.getMessage());
                            continue; // Skip to next recruiter
                        }
                    } else {
                        Locator connectButton = null;
                        for (Locator tempL : connectButtons) {
                            if (tempL.isVisible()) {
                                connectButton = tempL;
                                break;
                            }
                        }
                        if (connectButton != null) {
                            connectButton.click();
                            page.waitForTimeout(3000);

                            Locator addNoteButton = page.locator("button:has-text('Add a note')");
                            if (addNoteButton.isVisible()) {
                                addNoteButton.click();
                                page.waitForTimeout(3000);

                                // Fill in the note
                                String message = messageTemplateForNewConnection;
                                page.fill("textarea[name='message']", message);

                                // Click "Send"
                                page.click("button:has-text('Send')");
                                page.waitForTimeout(3000);

                                System.out.println("Connection request sent to: " + profileUrl);
                                connectionRequestsSent.add(profileUrl);
                            }
                        } else {
                            System.out.println("No connect button for: " + profileUrl);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error processing recruiter profile " + profileUrl + ": " + e.getMessage());
                continue; // Skip to next recruiter
            }
        }

        // Print all successful connection requests
        System.out.println("Successful connection requests sent:");
        for (String connectionSent : connectionRequestsSent) {
            System.out.println(connectionSent);
        }
    }





    public void loginToLinkedIn(Page page, String email, String password) {
        page.navigate("https://www.linkedin.com/login");

        page.fill("#username", email);
        page.fill("#password", password);
        page.click("button[type='submit']");
        page.waitForTimeout(5000); // Wait for login to complete

        System.out.println("Login successful!");
    }

    public String getCompanyCodeFromCompanyName(Page page, String companyName){
        try{
            String searchUrl = "https://www.linkedin.com/search/results/companies/?keywords=" + companyName;
            page.navigate(searchUrl);
            page.waitForTimeout(4000);

            Locator companyLink = page.locator("a[href*='/company/']:visible").first();
            String companyUrl = companyLink.getAttribute("href");
            page.navigate(companyUrl);
            page.waitForTimeout(4000);

            // Get first search result
            String htmlContent = page.content();
            String pattern = "companyIds=(\\d+)";
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(htmlContent);
            String companyId = matcher.find() ? matcher.group(1) : null;

            if (companyId != null) {
                System.out.println("Company ID for " + companyName + ": " + companyId);
                return companyId;
            } else {
                return null;
            }
        } catch(Exception e){
            return null;
        }
    }

    public List<String> sendMessageToFirstConnections(Page page, String companyName, int i) {
        List<String> connectionsSendMessage = new ArrayList<>();
//        String companyId = getCompanyCodeFromCompanyName(page, companyName);
        LinkedHashSet<String> recruiterProfiles = new LinkedHashSet<>();

//        for(int i=1; i<=9; i++){
            try {
                String connectionURL = "https://www.linkedin.com/search/results/people/?keywords=" + companyName.toLowerCase()+ "&network=%5B%22F%22%5D&origin=FACETED_SEARCH&page="+i;
//                String connectionURL = "https://www.linkedin.com/search/results/people/?currentCompany=%5B%22" + companyId + "%22%5D&network=%5B%22F%22%5D&origin=FACETED_SEARCH&page="+i;
                page.navigate(connectionURL);
                page.waitForTimeout(5000); // Wait for the page to load
            } catch (Exception e) {
                System.out.println("Error navigating to company search page: " + e.getMessage());
                return connectionsSendMessage; // Skip execution if company page fails
            }

            System.out.println("Filtered by company. Extracting recruiters...");

            try {
                // Step 4: Find the recruiter list
                List<Locator> recruiters = page.locator("//a[contains(@href, '/in/')]").all();

                for (Locator recruiter : recruiters) {
                    try {
                        String profileUrl = recruiter.getAttribute("href");
                        String recruiterFirstName = recruiter.innerText().trim().split("\n")[0].split(" ")[0].toLowerCase(); // Extract text

                        if (profileUrl != null && profileUrl.toLowerCase().contains(recruiterFirstName)) {
                            recruiterProfiles.add(profileUrl);
                            System.out.println("Found Recruiter: " + profileUrl);
                        }
                    } catch (Exception e) {
                        System.out.println("Error extracting recruiter details: " + e.getMessage());
                        continue; // Skip to next recruiter
                    }
                }
            } catch (Exception e) {
                System.out.println("Error retrieving recruiter list: " + e.getMessage());
                return connectionsSendMessage; // Skip execution if list retrieval fails
            }


        for (String profileUrl : recruiterProfiles) {
            try {
                page.navigate(profileUrl);
                page.waitForTimeout(5000);
            } catch (Exception e) {
                System.out.println("Error navigating to recruiter profile: " + e.getMessage());
                continue; // Skip to next recruiter
            }

            Locator companyTagButton = page.locator("button[aria-label*='Current company']");
            String companyTag = companyTagButton.getAttribute("aria-label");

            if(companyTag.toLowerCase().contains(companyName.toLowerCase())){
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
                            String messageForRecruiter = String.format(messageTemplateForRecruiter, firstName, companyName);
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
                                connectionsSendMessage.add(profileUrl);
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
                    System.out.println("Total Connections Messaged: "+ connectionsSendMessage.size());
                    continue; // Skip to next recruiter
                }
            }
        }

        return connectionsSendMessage;
    }
}
