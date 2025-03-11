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

        List<WebElement> connections = driver.findElements(By.cssSelector("span.entity-result__title-text"));

        List<String> recruiterNames = new ArrayList<>();
        for (WebElement connection : connections) {
            String name = connection.getText();
            if (name.toLowerCase().contains("recruiter") || name.toLowerCase().contains("hiring manager")) {
                recruiterNames.add(name);
            }
        }

        return recruiterNames;
    }

    public void closeDriver() {
        driver.quit();
    }
}

