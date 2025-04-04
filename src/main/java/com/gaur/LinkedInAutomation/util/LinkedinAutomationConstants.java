package com.gaur.LinkedInAutomation.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LinkedinAutomationConstants {
    public static String messageTemplateForRecruiter = "Hi %s,\n" +
            "\n" +
            "I recently saw some posts regarding openings for SDE2(4.5+YOE) roles %s.\n" +
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
            "Email: gg.gaurav243@gmail.com";

    public static String messageTemplateForNewConnection = "With 4.5+ years total experience working currently " +
            "as Associate at GS working as Backend Engineer in Java, Python, and Microservices system design, " +
            "I believe I could be a great fit. Looking forward to connecting for opportunity!";

    public static Set<String> blockedUrl = new HashSet<>(Arrays.asList(
       "https://www.linkedin.com/in/manjunath-tamaka-shreerama-0a877150/"
    ));
}
