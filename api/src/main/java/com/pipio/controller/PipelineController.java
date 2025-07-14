package com.pipio.controller;


import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import java.util.Optional; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import com.pipio.dto.PipelineDefinition;
import com.pipio.dto.PipelineResponse;
import com.pipio.dto.StageDefinition;
import com.pipio.dto.StepDefinition;
import com.pipio.model.Pipeline;
import com.pipio.model.Stage;
import com.pipio.model.Step;
import com.pipio.repository.PipelineRepository;
import com.pipio.service.JobService;
import com.pipio.service.PipelineService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineRepository pipelineRepo;
    private final PipelineService pipelineService;
    private final JobService jobService;

    @PostMapping
    public ResponseEntity<?> createPipeline(@RequestParam("file") MultipartFile file,
                                            @RequestParam("name") String name, @RequestParam(value = "repoUrl", required = false) String repoUrl) throws Exception {
        
        Long pipelineId = pipelineService.createPipelineFromYaml(file, name, repoUrl);
        return ResponseEntity.ok(pipelineId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPipeline(@PathVariable Long id) {
        return pipelineRepo.findById(id)
                .map(pipelineService::convertToResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<PipelineResponse>> getAllPipeline() {
        List<Pipeline> pipes = pipelineRepo.findAll();
        List<PipelineResponse> pipeResonses = new ArrayList<>();
        for(Pipeline pipe : pipes){
            pipeResonses.add(pipelineService.convertToResponseDto(pipe));
        }
        //System.out.println(pipeResonses);



        return ResponseEntity.ok(pipeResonses);
    }
    

    @PostMapping("/{id}/trigger")
    public ResponseEntity<?> triggerPipeline(@PathVariable Long id) {
        Optional<Pipeline> pipelineOpt = pipelineRepo.findById(id);
        if (pipelineOpt.isEmpty()) return ResponseEntity.notFound().build();

        Pipeline pipeline = pipelineOpt.get();
        
        jobService.enqueueJob(pipeline); // Step 2
        return ResponseEntity.ok("Job enqueued");
    }
}

