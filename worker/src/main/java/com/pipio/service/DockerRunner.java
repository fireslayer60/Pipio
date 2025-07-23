package com.pipio.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pipio.service.JobLogService;

@Slf4j
@Component
public class DockerRunner {

    private final LogPublisher logPublisher;
    

    public DockerRunner(LogPublisher logPublisher) {
        this.logPublisher = logPublisher;
        
    }

    public int runStep(String runCommand, String jobId, String stepName, String baseImage, Map<String, String> envSecrets) {
        try {
            String containerName = "job-" + jobId + "-" + stepName.replaceAll("[^a-zA-Z0-9-]", "-").replaceAll("-+", "-").toLowerCase();

            String safeCommand = runCommand.replace("\"", "\\\"");

            // Build the docker command
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("run");
            command.add("--rm");
            command.add("--name");
            command.add(containerName);

            // ✅ Add environment variable flags
            for (Map.Entry<String, String> entry : envSecrets.entrySet()) {
                command.add("-e");
                command.add(entry.getKey() + "=" + entry.getValue());
            }

            command.add(baseImage);
            command.add("sh");
            command.add("-c");
            command.add(safeCommand);

            // Launch the process
            ProcessBuilder pb = new ProcessBuilder(command);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            log.info("📥 Logs for {}:", containerName);
            while ((line = reader.readLine()) != null) {
                log.info("{}", line);
                logPublisher.publishLog(jobId, line); // ✅ Now works
                logPublisher.saveLog((long)(Integer.parseInt(jobId)), line);
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
