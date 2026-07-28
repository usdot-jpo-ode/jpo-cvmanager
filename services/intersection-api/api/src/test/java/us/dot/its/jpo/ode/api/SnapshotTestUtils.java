package us.dot.its.jpo.ode.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnapshotTestUtils {
    private static final String SNAPSHOT_DIR = "src/test/resources/snapshots";

    /**
     * Helper method to compare generated content with saved snapshot.
     * If snapshot doesn't exist, it will be created.
     * If UPDATE_SNAPSHOTS=true environment variable is set, snapshots will be
     * updated.
     */
    public static void assertMatchesSnapshot(String actualContent, String snapshotPath) throws IOException {
        Path path = Paths.get(SNAPSHOT_DIR + "/" + snapshotPath);
        boolean updateSnapshots = Boolean.parseBoolean(System.getenv().getOrDefault("UPDATE_SNAPSHOTS", "false"));

        // Create directory if it doesn't exist
        Files.createDirectories(path.getParent());
        boolean existedBefore = Files.exists(path);

        if (!existedBefore || updateSnapshots) {
            // Create or update snapshot
            Files.writeString(path, actualContent);
            log.info("Snapshot {}: {}", (existedBefore ? "updated" : "created"), snapshotPath);

            if (!updateSnapshots && !existedBefore) {
                fail("Snapshot file created. Please review and commit: " + snapshotPath);
            }
        } else {
            // Compare with existing snapshot
            String expectedContent = Files.readString(path);
            String normalizedExpected = normalizeLineEndings(expectedContent);
            String normalizedActual = normalizeLineEndings(actualContent);

            if (!normalizedExpected.equals(normalizedActual)) {
                // Print detailed diff
                String diff = generateDiff(normalizedExpected, normalizedActual);
                fail(String.format(
                        "Generated email content does not match snapshot: %s\n\n" +
                                "To update snapshots, run tests with UPDATE_SNAPSHOTS=true\n\n" +
                                "DIFF:\n%s\n\n" +
                                "EXPECTED:\n%s\n\n" +
                                "ACTUAL:\n%s",
                        snapshotPath,
                        diff,
                        normalizedExpected,
                        normalizedActual));
            }
        }
    }

    /**
     * Normalize line endings for cross-platform compatibility
     */
    private static String normalizeLineEndings(String content) {
        return content.replaceAll("\r\n", "\n").trim();
    }

    /**
     * Generate a unified diff between expected and actual content using
     * java-diff-utils
     */
    private static String generateDiff(String expected, String actual) {
        List<String> expectedLines = List.of(expected.split("\n"));
        List<String> actualLines = List.of(actual.split("\n"));

        Patch<String> patch = DiffUtils.diff(expectedLines, actualLines);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "expected",
                "actual",
                expectedLines,
                patch,
                3 // context lines
        );

        StringBuilder diff = new StringBuilder();
        diff.append("===================================\n");
        diff.append("Snapshot Unified Diff\n");
        diff.append("===================================\n");

        for (String line : unifiedDiff) {
            diff.append(line).append("\n");
        }

        return diff.toString();
    }
}