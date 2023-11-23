package net.querybuilder4j.config;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class ConfigFileReaderTest {

    @Test
    public void read_fileFoundDeserializesObjectCorrectly() {
        URL fileUrl = ConfigFileReaderTest.class.getClassLoader().getResource("qb.yml");
        assert fileUrl != null;
        ConfigFileReader configFileReader = new ConfigFileReader(fileUrl.getPath());

        QbConfig qbConfig = configFileReader.read();

        assertNotNull(qbConfig);
    }

    @Test
    public void constructor_noSpecifiedFilePathResultsInDefaultFilePath() {
        ConfigFileReader configFileReader = new ConfigFileReader();

        assertEquals(ConfigFileReader.DEFAULT_CONFIG_FILE_PATH, configFileReader.getConfigFilePath());
    }

    @Test
    public void read_fileNotFoundExceptionThrowsException() {
        assertThrows(
                RuntimeException.class,
                () -> {
                    ConfigFileReader configFileReader = new ConfigFileReader("/this/path/does/not/exist/qb.yml");
                    configFileReader.read();
                }
        );
    }

}