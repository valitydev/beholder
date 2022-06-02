package dev.vality.beholder.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.beholder.model.Region;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.List;

@UtilityClass
public class FileUtil {

    public static List<Region> readRegions(Resource resource) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {
                });
    }
}
