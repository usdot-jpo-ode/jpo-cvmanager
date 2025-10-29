package us.dot.its.jpo.ode.api;

import j2735ffm.MessageFrameCodec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodecConfig {

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Value("${j2735.api.text-buffer-size}")
    private long textBufferSize;

    @Value("${j2735.api.uper-buffer-size}")
    private long uperBufferSize;

    @Value("${j2735.api.error-buffer-size}")
    private long errorBufferSize;

    @Value("${j2735.api.asn-codec-resource-path}")
    private String asnCodecResourcePath;

    private Path getAsnCodecResourcePathWithDefaults() {
        if (asnCodecResourcePath != null && !asnCodecResourcePath.isEmpty()) {
            return Paths.get(asnCodecResourcePath);
        }

        String libraryName = isWindows() ? "asnapplication.dll" : "libasnapplication.so";

        try {
            // Extract from classpath to temp directory
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("asn1/" + libraryName);

            if (inputStream == null) {
                throw new RuntimeException("Native library not found in classpath: " + libraryName);
            }

            Path tempFile = Files.createTempFile("asn-codec-", isWindows() ? ".dll" : ".so");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit(); // Clean up on JVM exit

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract native library", e);
        }
    }

    @Bean
    public MessageFrameCodec messageFrameCodec() {
        return new MessageFrameCodec(
                textBufferSize,
                uperBufferSize,
                errorBufferSize,
                getAsnCodecResourcePathWithDefaults());
    }
}
