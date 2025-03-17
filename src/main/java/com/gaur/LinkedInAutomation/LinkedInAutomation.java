package com.gaur.LinkedInAutomation;

import com.microsoft.playwright.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LinkedInAutomation {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            LinkedInAutomation automation = new LinkedInAutomation();

            // Step 1: Login
            automation.loginToLinkedIn(page, "gupta.gaurav4188@gmail.com", "");
            String companyName = "D.E Shaw";

//             Step 2: Extract recruiters from connections
            //List<String> recruiterProfiles = automation.extractRecruiters(page, companyName);


            automation.sendConnectionRequests(page, companyName);

            browser.close();
        }
    }

    public void sendConnectionRequests(Page page, String companyName) {
        List<String> connectionRequestsSent = new ArrayList<>();
        System.out.println("Searching for recruiters at: " + companyName);

        // Step 1: Navigate to LinkedIn search page for recruiters
        try {
            String searchUrl = "https://www.linkedin.com/search/results/people/?keywords=Recruiter%20" + companyName;
            page.navigate(searchUrl);
            page.waitForTimeout(5000);
        } catch (Exception e) {
            System.out.println("Error navigating to search page: " + e.getMessage());
            return;
        }

        HashSet<String> recruiterProfiles = new HashSet<>();
        recruiterProfiles.add("https://www.linkedin.com/in/ranjith-badri-0a205668/"); // Testing to be removed

        // Step 2: Loop through multiple pages (up to 10 pages)
        for (int pageNum = 1; pageNum <= 2; pageNum++) {
            try {
                System.out.println("Scraping page: " + pageNum);

                // Collect recruiter profiles from the current page
                List<Locator> recruiters = page.locator("//a[contains(@href, '/in/')]").all();
                for (Locator recruiter : recruiters) {
                    String profileUrl = recruiter.getAttribute("href");
                    if (profileUrl != null) {
                        recruiterProfiles.add(profileUrl);
                        System.out.println("Found Recruiter: " + profileUrl);
                    }
                }

                // Scroll and click "Next" button
                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                page.waitForTimeout(5000);

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

        // Step 3: Send connection requests
        for (String profileUrl : recruiterProfiles) {
            try {
                page.navigate(profileUrl);
                page.waitForTimeout(5000);

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
                            page.waitForTimeout(4000);
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
                                page.waitForTimeout(4000);
                            } else {
                                System.out.println("No connect button for: " + profileUrl);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error clicking 'More Actions' for " + recruiterName + ": " + e.getMessage());
                        continue; // Skip to next recruiter
                    }
                }

                for (Locator connectButton : connectButtons) {
                    try {
                        if (connectButton.isVisible()) {
                            connectButton.click();
                            page.waitForTimeout(5000);

                            // Click "Add a Note"
                            Locator addNoteButton = page.locator("button:has-text('Add a note')");
                            if (addNoteButton.isVisible()) {
                                addNoteButton.click();
                                page.waitForTimeout(5000);

                                // Fill in the note
                                String message = "With 4.5+ years total experience working currently as Associate at GS working as Backend Engineer in Java, Python, and Microservices system design, I believe I could be a great fit. Looking forward to connecting for opportunity!";
                                page.fill("textarea[name='message']", message);

                                // Click "Send"
                                page.click("button:has-text('Send')");
                                page.waitForTimeout(5000);

                                System.out.println("Connection request sent to: " + profileUrl);
                                connectionRequestsSent.add(profileUrl);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error sending connection request to " + recruiterName + ": " + e.getMessage());
                        continue; // Skip to next recruiter
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

    public List<String> extractRecruiters(Page page, String companyName) {
        page.navigate("https://www.linkedin.com/mynetwork/invite-connect/connections/");
        page.waitForTimeout(3000); // Wait for the page to load

        // Step 1: Click the "Search with Filters" link
        Locator filterLink = page.locator("a[href*='/search/results/people']");
        if (filterLink.isVisible()) {
            filterLink.click();
            page.waitForTimeout(6000); // Wait for filters page to load
        }

        // Step 2: Click the "Current company" filter button
        Locator currentCompanyFilterButton = page.locator("#searchFilter_currentCompany");
        if (currentCompanyFilterButton.isVisible()) {
            currentCompanyFilterButton.click();
            page.waitForTimeout(2000); // Wait for dropdown to appear
        }

        System.out.println("Please manually select the company and then press Enter in the console...");
        try {
            System.in.read(); // Pause execution until user presses Enter
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Filtered by company. Extracting recruiters...");

        // Step 4: Find the recruiter list
        List<Locator> recruiters = page.locator("//a[contains(@href, '/in/')]").all();
        HashSet<String> recruiterProfiles = new HashSet<>();

        for (Locator recruiter : recruiters) {
            String profileUrl = recruiter.getAttribute("href");
            if (profileUrl != null) {
                recruiterProfiles.add(profileUrl);
                System.out.println("Found Recruiter: " + profileUrl);
            }
        }

        for (String profileUrl : recruiterProfiles) {
            page.navigate(profileUrl);
            page.waitForTimeout(3000);

            Locator nameTag = page.locator("//a[contains(@href, '/overlay/about-this-profile/') and contains(@aria-label, '')]");
            String recruiterName = nameTag.getAttribute("aria-label");

            System.out.println("Recruiter Name: " + recruiterName);

            if (recruiterName == null) {
                System.out.println("Skipping profile: Could not extract recruiter name.");
            }

            String firstName = recruiterName.split(" ")[0];

            String connectButtonSelector = String.format(
                    "//button[contains(@aria-label, 'Message %s') and contains(@class, 'artdeco-button--primary')]"
//                + "[.//svg[@data-test-icon='connect-small']]"
//                + "[.//span[contains(@class, 'artdeco-button__text') and normalize-space()='Connect']]",  // Ensures it's the correct connect button
                    ,firstName
            );
            List<Locator> connectButtons = page.locator(connectButtonSelector).all();
            System.out.println("Found " + connectButtons.size() + " Connect buttons for " + recruiterName);

            for(int i=0; i < connectButtons.size(); i++) {
                Locator connectButton = connectButtons.get(i);
                System.out.println("Message button Visible: " + connectButton.isVisible());
                if (connectButton.isVisible()) {
                    connectButton.click();
                    page.waitForTimeout(5000);
                }

                String message = String.format("Hi %s,\n" +
                        "\n" +
                        "I recently saw some posts regarding openings for SWE2(4.5+YOE) roles %s.\n" +
                        "\n" +
                        "I'm currently looking to explore newer opportunities and would be interested in applying for Software Developer-2(Backend).\n" +
                        "\n" +
                        "Could you please help align my application for these roles, if there are openings?\n" +
                        "\n" +
                        "Based on my experience as a Associate(SDE-2) at Goldman Sachs, Amazon, and Samsung working as Backend Engineer with Java, Python, AWS, Microservices, System Design I believe I can be a good fit.\n" +
                        "\n" +
                        "I would like to bring your attention to my background which makes me suitable for the job role:\n" +
                        "\n" +
                        "* Name: Gaurav Gupta\n" +
                        "* Years of Experience: 4.5+ Years\n" +
                        "* Current Company: Goldman Sachs\n" +
                        "* Role in Current Company: Associate(Engineering)\n" +
                        "* Previous Company: Amazon (1year 8 months)\n" +
                        "* Role in Previous Company: SDE\n" +
                        "\n" +
                        "For more information, I'll attach my resume here.\n" +
                        "\n" +
                        "It would be a pleasure if I can hear back from you regarding any opportunities. \n" +
                        "\n" +
                        "Sincerely,\n" +
                        "Gaurav Gupta\n" +
                        "Contact No.+919779642202\n" +
                        "Email: gg.gaurav243@gmail.com", firstName, companyName);

                Locator messageBox = page.locator("div.msg-form__contenteditable");
                if (messageBox.isVisible()) {
                    messageBox.fill(message);

                    System.out.println("Message typed for: " + recruiterName);

                    String attachButtonSelector = String.format(
                            "button[aria-label='Attach a file to your conversation with %s']",
                            recruiterName
                    );

                    Locator attachButton = page.locator(attachButtonSelector);
                    if (attachButton.isVisible()) {
                        attachButton.click();
                        page.waitForTimeout(2000);

                        // Upload resume from local storage
                        page.setInputFiles("input[type='file']", Paths.get("/Users/gaurgupr/Desktop/Projects/LinkedInAutomation/src/main/resources/Gaurav Gupta - Resume.pdf"));
                        System.out.println("Resume attached for: " + recruiterName);
                        page.waitForTimeout(3000);

                        Locator sendButton = page.locator("//button[contains(@class, 'msg-form__send-button artdeco-button artdeco-button--1')]");
                        if (sendButton.isVisible()) {
                            sendButton.click();
                            page.waitForTimeout(3000);
                            System.out.println("Message sent to: " + recruiterName);
                        } else {
                            System.out.println("Send button not found for: " + recruiterName);
                        }
                    }
                }
            }

        }
        return null;
    }
}
