package com.app.dbTools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Db配置类
 */
public class DbConfig {
    private Properties properties;

    public DbConfig() {
        properties = new Properties();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("database.properties");

        if(input != null){
            try {
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getJdbcUrl() {
        return properties.getProperty("jdbcUrl");
    }

    public String getUsername() {
        return properties.getProperty("username");
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    public int getMaximumPoolSize() {
        return Integer.parseInt(properties.getProperty("maximumPoolSize"));
    }
}
