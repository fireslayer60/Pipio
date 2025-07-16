package com.pipio.service;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class DockerRunner {

    public static int runStep(String runCommand, String jobId, String stepName, String baseImage) {
        try {
            String containerName = "job-" + jobId + "-" + stepName.replaceAll("[^a-zA-Z0-9-]", "-").replaceAll("-+", "-").toLowerCase();


            // Example: docker run --rm --name job-1-step1 alpine sh -c "echo hello"
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
