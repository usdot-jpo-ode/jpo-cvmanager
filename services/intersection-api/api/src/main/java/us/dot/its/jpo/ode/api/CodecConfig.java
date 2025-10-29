package us.dot.its.jpo.ode.api;

import j2735ffm.MessageFrameCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
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

    private Path getAsnCodecResourcePath() throws IOException {

        // Extract binary from resources directory
        String libraryName = isWindows() ? "asnapplication.dll" : "libasnapplication.so";
        log.info("Loading native ASN.1 library from classpath resource: " + libraryName);
        try (InputStream in = getClass().getResourceAsStream("/asn1/" + libraryName)) {
            if (in == null) {
                throw new FileNotFoundException(libraryName + " not found in classpath resources");
            }

            Path tempFile = Files.createTempFile("asnapplication-", isWindows() ? ".dll" : ".so");
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.toAbsolutePath().toString());
            log.info("Loaded native DLL from extracted temp file: " + tempFile);
            return tempFile;
        }
    }

    @Bean
    public MessageFrameCodec messageFrameCodec() throws IOException {
        return new MessageFrameCodec(
                textBufferSize,
                uperBufferSize,
                errorBufferSize,
                getAsnCodecResourcePath());
    }
}
