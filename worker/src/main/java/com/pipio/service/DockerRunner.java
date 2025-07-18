package com.pipio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Component
public class DockerRunner {

    private final LogPublisher logPublisher;

    public DockerRunner(LogPublisher logPublisher) {
        this.logPublisher = logPublisher;
    }

    public int runStep(String runCommand, String jobId, String stepName, String baseImage) {
        try {
            String containerName = "job-" + jobId + "-" + stepName.replaceAll("[^a-zA-Z0-9-]", "-").replaceAll("-+", "-").toLowerCase();

            String safeCommand = runCommand.replace("\"", "\\\"");

            ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm",
                "--name", containerName,
                baseImage,
                "sh", "-c", safeCommand
            );

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            log.info("📥 Logs for {}:", containerName);
            while ((line = reader.readLine()) != null) {
                log.info("{}", line);
                logPublisher.publishLog(jobId, line); // ✅ Now works
            }

            int exitCode = process.waitFor();
            log.info("⚙️ {} exited with code {}", containerName, exitCode);

            return exitCode;
        } catch (Exception e) {
            log.error("❌ Error running Docker step", e);
            return -1;
        }
    }
}
