package com.pipio.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipio.model.Pipeline;
import com.pipio.repository.PipelineRepository;
import com.pipio.service.GithubWebhookService;
import com.pipio.service.JobService;

@RestController
@RequestMapping("/api/webhooks/github")
public class GitHubWebhookController {

    private GithubWebhookService githubWebhookService;
    private PipelineRepository pipelineRepo;
    private JobService jobService;
    @Value("${github.webhook.secret}")
    private String githubSecret;
    public GitHubWebhookController(GithubWebhookService githubWebhookService,
                               PipelineRepository pipelineRepo,
                               JobService jobService) {
        this.githubWebhookService = githubWebhookService;
        this.pipelineRepo = pipelineRepo;
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String payload)throws Exception {

        if (!githubWebhookService.verifySignature(payload, signature, githubSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(payload);
        String fullRepoUrl = node.get("repository").get("html_url").asText();
     
        Optional<Pipeline> pipelineOpt = pipelineRepo.findByRepoUrl(fullRepoUrl);
        if (pipelineOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pipeline found for repo: " + fullRepoUrl);
        }

        Pipeline pipeline = pipelineOpt.get();

       
        jobService.enqueueJob(pipeline);

        return ResponseEntity.ok("✅ Job triggered for pipeline " + pipeline.getName());
    }
}

