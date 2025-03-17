package com.gaur.LinkedInAutomation.service;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LinkedInScraper {

    private WebDriver driver;

    public LinkedInScraper() {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver"); // Update the path

        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless"); // Run without opening the browser
        options.addArguments("--disable-gpu");

        driver = new ChromeDriver(options);
    }

    public void loginToLinkedIn(String email, String password) {
        driver.get("https://www.linkedin.com/login");

        WebElement emailField = driver.findElement(By.id("username"));
        emailField.sendKeys(email);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(password);

        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        System.out.println("Successfully logged in!");
    }

    public List<String> getRecruitersFromConnections() {
        driver.get("https://www.linkedin.com/mynetwork/invite-connect/connections/");

        try {
            Thread.sleep(3000); // Wait for the page to load

            // Step 1: Click the "Search with Filters" link
            WebElement filterLink = driver.findElement(By.xpath("//a[contains(@href, '/search/results/people')]"));
            filterLink.click();
            Thread.sleep(3000); // Wait for the filters page to load

            // Step 2: Click the "Current company" filter button
            WebElement currentCompanyFilterButton = driver.findElement(By.id("searchFilter_currentCompany"));
            currentCompanyFilterButton.click();
            Thread.sleep(2000); // Wait for the dropdown to appear

            System.out.println("Please manually select the company and then press Enter in the console...");
            System.in.read(); // Pause execution until user presses Enter

            // Step 3: Click "Show results" button after manual selection
//            WebElement showResultsButton = driver.findElement(By.xpath("//button[.//span[contains(text(),'Show results')]]"));
//            showResultsButton.click();
//            Thread.sleep(3000); // Wait for results to load

            System.out.println("Filtered by company. Extracting recruiters...");

            // Step 4: Find the recruiter list
            WebElement recruiterList = driver.findElement(By.xpath("//ul[contains(@class, 'list-style-none')]"));
            List<WebElement> recruiterCards = recruiterList.findElements(By.tagName("li"));

            List<String> recruiterDetails = new ArrayList<>();

            for (WebElement recruiterCard : recruiterCards) {
                try {
                    // Extract recruiter name and profile link
                    WebElement nameElement = recruiterCard.findElement(By.xpath(".//a[contains(@class, 'NwvyTemnvomAkMRzZgdROYCGzuSuRHxpGE')]"));
                    String recruiterName = nameElement.getText();
                    String profileUrl = nameElement.getAttribute("href");

                    // Find and click the "Message" button
                    List<WebElement> messageButtons = recruiterCard.findElements(By.xpath(".//button[.//span[contains(text(),'Message')]]"));
                    if (!messageButtons.isEmpty()) {
                        WebElement messageButton = messageButtons.get(0);

                        if (messageButton.isDisplayed() && messageButton.isEnabled()) {
                            messageButton.click();
                            Thread.sleep(2000); // Wait for chat box to open
                            System.out.println("Opened message box for: " + recruiterName);
                        } else {
                            System.out.println("Message button not clickable for: " + recruiterName);
                        }
                    } else {
                        System.out.println("No message button found for: " + recruiterName);
                    }

                    // Store recruiter details
                    recruiterDetails.add(recruiterName + " - " + profileUrl);
                } catch (Exception e) {
                    System.out.println("Skipping a recruiter due to missing elements.");
                }
            }

            return recruiterDetails;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }





    public void closeDriver() {
        driver.quit();
    }
}

