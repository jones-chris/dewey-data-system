package com.deweydatasystem.config;

import com.deweydatasystem.utils.ExcludeFromJacocoGeneratedReport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@ExcludeFromJacocoGeneratedReport // todo:  Troubleshoot why Jacoco is ignoring unit test coverage for this class.
@Slf4j
public class ConfigFileReader {

    public static final String DEFAULT_CONFIG_FILE_PATH = "/qb/config/4ajr.yaml";

    @Getter
    private final String configFilePath;

    public ConfigFileReader() {
        this.configFilePath = ConfigFileReader.DEFAULT_CONFIG_FILE_PATH;
    }

    public ConfigFileReader(final String configFilePath) {
        this.configFilePath = Objects.requireNonNullElse(configFilePath, ConfigFileReader.DEFAULT_CONFIG_FILE_PATH);
    }

    public QbConfig read() {
        log.info("Reading 4ajr.yaml");

        try (var fileInputStream = new FileInputStream(this.configFilePath)) {
            String config = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);

            log.info("Found config");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

            JsonNode node = mapper.readTree(config);

            return mapper.readValue(node.toPrettyString(), QbConfig.class);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("4ajr.yaml not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
