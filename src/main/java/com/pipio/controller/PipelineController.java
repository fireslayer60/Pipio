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
                                            @RequestParam("name") String name) throws Exception {
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(PipelineDefinition.class, options);
        Yaml yaml = new Yaml(constructor);
        PipelineDefinition def = yaml.load(file.getInputStream());

        Pipeline pipeline = new Pipeline();
        pipeline.setName(name);

        List<Stage> stageList = new ArrayList<>();
        for (StageDefinition sdef : def.getStages()) {
            Stage stage = new Stage();
            stage.setName(sdef.getName());
            stage.setPipeline(pipeline);

            List<Step> stepList = new ArrayList<>();
            for (StepDefinition stepDef : sdef.getSteps()) {
                Step step = new Step();
                step.setRunCommand(stepDef.getRun());
                step.setStage(stage);
                stepList.add(step);
            }
            stage.setSteps(stepList);
            stageList.add(stage);
        }

        pipeline.setStages(stageList);
        //asa
        pipelineRepo.save(pipeline);

        return ResponseEntity.ok(pipeline.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPipeline(@PathVariable Long id) {
        return pipelineRepo.findById(id)
                .map(pipelineService::convertToResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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

