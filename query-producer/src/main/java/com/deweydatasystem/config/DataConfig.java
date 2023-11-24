package com.deweydatasystem.config;

import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.config.ConfigFileReader;
import com.deweydatasystem.config.QbConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * The class that will instantiate a {@link QbConfig}.
 */
@Configuration
@Slf4j
public class DataConfig {

    @Bean
    public QbConfig getTargetDatabases() throws IOException {
        final String configFilePath = System.getProperty("CONFIG_FILE_PATH", ConfigFileReader.DEFAULT_CONFIG_FILE_PATH);
        return new ConfigFileReader(configFilePath).read();
    }

}
