package com.pipio;

import com.pipio.service.JobWorkerService;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class WorkerApplication implements CommandLineRunner {

    private final JobWorkerService jobWorkerService;

    public WorkerApplication(JobWorkerService jobWorkerService) {
        this.jobWorkerService = jobWorkerService;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WorkerApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE); 
        app.run(args);
    }

    @Override
    public void run(String... args) {
        jobWorkerService.startWorker();
    }
}
