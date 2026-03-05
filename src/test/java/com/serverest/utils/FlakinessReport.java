package com.serverest.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FlakinessReport {
    public static void main(String[] args) throws Exception {
        Path reportDir = Path.of("target", "surefire-reports");
        int totalTests = 0;
        int flakyTests = 0;

        if (Files.exists(reportDir)) {
            File[] files = reportDir.toFile().listFiles((dir, name) -> name.startsWith("TEST-") && name.endsWith(".xml"));
            if (files != null) {
                for (File file : files) {
                    var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
                    document.getDocumentElement().normalize();
                    totalTests += document.getElementsByTagName("testcase").getLength();
                    // Surefire reports flaky tests as "flakyFailure" or "flakyError" in newer versions
                    // Or we check for rerunFailure which indicates a retry happened
                    int reruns = document.getElementsByTagName("rerunFailure").getLength() + 
                                 document.getElementsByTagName("rerunError").getLength() +
                                 document.getElementsByTagName("flakyFailure").getLength() +
                                 document.getElementsByTagName("flakyError").getLength();
                    
                    if (reruns > 0) {
                        flakyTests += reruns;
                        System.out.println("[Flaky Detector] Found flakiness in: " + file.getName());
                    }
                }
            }
        }

        String json = String.format(
                "{\"totalTests\":%d,\"flakyTests\":%d}",
                totalTests,
                flakyTests
        );

        Path output = Path.of("target", "flakiness-report.json");
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        Files.writeString(output, json, StandardCharsets.UTF_8);
        
        System.out.println("Flakiness Report generated: " + output.toAbsolutePath());
        if (flakyTests > 0) {
            System.out.println("⚠️ WARNING: " + flakyTests + " flaky tests detected!");
        } else {
            System.out.println("✅ No flaky tests detected.");
        }
    }
}
