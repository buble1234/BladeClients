package win.blade.common.utils.browser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import win.blade.common.utils.aim.mode.WebScreen;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Автор: NoCap
 * Дата создания: 28.07.2025
 */
public class BrowserUtility {

    public static String loadHtmlFromFile(String filePath) {
        try {
            InputStream inputStream = WebScreen.class.getResourceAsStream(filePath);
            if (inputStream == null) {
                System.err.println("HTML file not found: " + filePath);
                return "<html><body><h1>Error: HTML file not found!</h1></body></html>";
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "<html><body><h1>Error loading HTML: " + e.getMessage() + "</h1></body></html>";
        }
    }

    public static String getHtmlFilePath(String filePath) {
        try {
            String htmlContent = BrowserUtility.loadHtmlFromFile(filePath);
            File tempFile = File.createTempFile("blade_html_", ".html");
            FileUtils.writeStringToFile(tempFile, htmlContent, StandardCharsets.UTF_8);
            return tempFile.getAbsolutePath().replace("\\", "/");
        } catch (Exception e) {
            e.printStackTrace();
            return "data:text/html;charset=utf-8,<html><body><h1>Error loading HTML</h1></body></html>";
        }
    }
}