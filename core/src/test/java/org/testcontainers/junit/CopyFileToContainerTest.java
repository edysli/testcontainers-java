package org.testcontainers.junit;

import org.junit.Test;
import org.testcontainers.TestImages;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CopyFileToContainerTest {

    private static String containerPath = "/tmp/mappable-resource/";

    private static String fileName = "test-resource.txt";

    @Test
    public void checkFileCopied() throws IOException, InterruptedException {
        try (
            GenericContainer<?> container = new GenericContainer<>(TestImages.TINY_IMAGE)
                .withCommand("sleep", "3000")
                .withCopyFileToContainer(MountableFile.forClasspathResource("/mappable-resource/"), containerPath)
        ) {
            container.start();
            String filesList = container.execInContainer("ls", "/tmp/mappable-resource").getStdout();
            assertThat(filesList).as("file list contains the file").contains(fileName);
        }
    }

    @Test
    public void shouldUseCopyForReadOnlyClasspathResources() throws Exception {
        try (
            GenericContainer<?> container = new GenericContainer<>(TestImages.TINY_IMAGE)
                .withCommand("sleep", "3000")
                .withClasspathResourceMapping("/mappable-resource/", containerPath, BindMode.READ_ONLY)
        ) {
            container.start();
            String filesList = container.execInContainer("ls", "/tmp/mappable-resource").getStdout();
            assertThat(filesList).as("file list contains the file").contains(fileName);
        }
    }

    @Test
    public void shouldUseCopyOnlyWithReadOnlyClasspathResources() {
        String resource = "/test_copy_to_container.txt";
        GenericContainer<?> container = new GenericContainer<>(TestImages.TINY_IMAGE)
            .withClasspathResourceMapping(resource, "/readOnly", BindMode.READ_ONLY)
            .withClasspathResourceMapping(resource, "/readOnlyShared", BindMode.READ_ONLY, SelinuxContext.SHARED)
            .withClasspathResourceMapping(resource, "/readWrite", BindMode.READ_WRITE);

        Map<MountableFile, String> copyMap = container.getCopyToFileContainerPathMap();
        assertThat(copyMap).as("uses copy for read-only").containsValue("/readOnly");
        assertThat(copyMap).as("uses copy for read-only with Selinux").containsValue("/readOnlyShared");
        assertThat(copyMap).as("uses mount for read-write").doesNotContainValue("/readWrite");
    }

    @Test
    public void shouldCreateFoldersStructureWithCopy() throws Exception {
        String resource = "/test_copy_to_container.txt";
        try (
            GenericContainer<?> container = new GenericContainer<>(TestImages.TINY_IMAGE)
                .withCommand("sleep", "3000")
                .withClasspathResourceMapping(resource, "/a/b/c/file", BindMode.READ_ONLY)
        ) {
            container.start();
            String filesList = container.execInContainer("ls", "/a/b/c/").getStdout();
            assertThat(filesList).as("file list contains the file").contains("file");
        }
    }
}
