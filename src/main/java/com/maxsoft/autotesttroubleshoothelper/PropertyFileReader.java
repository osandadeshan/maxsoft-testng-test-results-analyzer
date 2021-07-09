package com.maxsoft.autotesttroubleshoothelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.maxsoft.autotesttroubleshoothelper.Constants.EXTENT_PROPERTY_FILE_DIRECTORY;

/**
 * Project Name    : auto-test-troubleshoot-helper
 * Developer       : Osanda Deshan
 * Version         : 1.0.0
 * Date            : 07/02/2021
 * Time            : 11:31 AM
 * Description     : This is the property file reader class that is used to read the properties inside './src/test/resources/extent.properties'
 **/

public class PropertyFileReader {

    public static String getProperty(String propertyName) {
        String propertyValue = null;

        try (InputStream input = new FileInputStream(EXTENT_PROPERTY_FILE_DIRECTORY)) {
            Properties prop = new Properties();
            prop.load(input);
            propertyValue = prop.getProperty(propertyName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return propertyValue;
    }
}
