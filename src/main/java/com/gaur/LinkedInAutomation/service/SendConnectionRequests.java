package com.gaur.LinkedInAutomation.service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.gaur.LinkedInAutomation.util.LinkedinAutomationConstants.messageTemplateForNewConnection;

public class SendConnectionRequests {
    public int sendConnectionRequests(Page page, String companyName, int pageNumber, Integer totalConnectionsSend) {
        List<String> connectionRequestsSent = new ArrayList<>();
        LinkedHashSet<String> recruiterProfiles = new LinkedHashSet<>();
        System.out.println("Searching for recruiters at: " + companyName);

        // Step 1: Navigate to LinkedIn search page for recruiters
        try {
            String searchUrl = "https://www.linkedin.com/search/results/people/?keywords=Talent%20Acquisition%20" + companyName + "&page=" + pageNumber;
            page.navigate(searchUrl);
            page.waitForTimeout(5000);
        } catch (Exception e) {
            System.out.println("Error navigating to search page: " + e.getMessage());
            return 0;
        }

        // Step 2: Loop through multiple pages (up to 10 pages)
//        for (int pageNum = 1; pageNum <= 4; pageNum++) {
            try {
                System.out.println("Scraping page: " + pageNumber);

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

//                Locator nextPageButton = page.locator("//button[contains(@aria-label,'Next') and contains(@class,'artdeco-pagination__button--next')]");
//                if (nextPageButton.isVisible()) {
//                    nextPageButton.click();
//                    page.waitForTimeout(5000);
//                } else {
//                    System.out.println("No more pages found. Stopping pagination.");
//                    break;
//                }
            } catch (Exception e) {
                System.out.println("Error scraping page " + pageNumber + ": " + e.getMessage());
            }


        System.out.println("Total recruiters found: " + recruiterProfiles.size());
        recruiterProfiles.remove("https://www.linkedin.com/in/gaurav-gupta-aertc127k/recent-activity/");

        // Step 3: Send connection requests
        for (String profileUrl : recruiterProfiles) {
            if(totalConnectionsSend >= 15) return 15;
            if(connectionRequestsSent.size() >= 15){
                connectionRequestsSent.forEach(url ->{
                    System.out.println(url);
                });
                return 15;
            }
            try {
                page.navigate(profileUrl);
                page.waitForTimeout(5000);

                Locator companyTagButton = page.locator("button[aria-label*='Current company']");
                if(!companyTagButton.isVisible()) {
                    page.waitForTimeout(5000);
                    continue;
                }
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
                                        totalConnectionsSend+=1;
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
                                totalConnectionsSend+=1;
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
        return totalConnectionsSend;
    }
}
