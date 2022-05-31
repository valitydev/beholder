package dev.vality.beholder.converter;

import dev.vality.beholder.exception.BadFormatException;
import dev.vality.beholder.model.NetworkLog;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.logging.LogEntry;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class LogEntriesToNetworkLogsConverterTest {

    private static final LogEntriesToNetworkLogsConverter CONVERTER = new LogEntriesToNetworkLogsConverter();

    @Test
    void convertValidLogs() throws IOException {
        String start = getMessage("valid_start_network_log.json");
        String stop = getMessage("valid_finish_network_log.json");
        LogEntry startLogEntry = new LogEntry(Level.ALL, Instant.now().toEpochMilli(), start);
        LogEntry endLogEntry = new LogEntry(Level.ALL, Instant.now().toEpochMilli(), stop);
        List<NetworkLog> converted = CONVERTER.convert(List.of(startLogEntry, endLogEntry));

        assertNotNull(converted);
        assertEquals(1, converted.size());
        NetworkLog networkLog = converted.get(0);
        assertNotNull(networkLog.getStart());
        assertNotNull(networkLog.getEnd());
        assertTrue(networkLog.getStart() < networkLog.getEnd());
        assertEquals("https://checkout.test.com/v1/fonts/90d16760.woff2", networkLog.getResource());
    }

    @Test
    void convertInvalidLog() throws IOException {
        String invalid = getMessage("invalid_network_log.json");
        LogEntry logEntry = new LogEntry(Level.ALL, Instant.now().toEpochMilli(), invalid);
        assertThrows(BadFormatException.class, () -> CONVERTER.convert(List.of(logEntry)));
    }

    private String getMessage(String filePath) throws IOException {
        var resource = new ClassPathResource(filePath, getClass());
        return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
    }
}