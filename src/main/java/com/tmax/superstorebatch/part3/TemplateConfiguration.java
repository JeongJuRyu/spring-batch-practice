//package com.tmax.superstorebatch.part3;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Configuration
//@Slf4j
//public class TemplateConfiguration {
//    @Bean
//    public Job job(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
//        return new JobBuilder("templateJob", jobRepository)
//                .incrementer(new RunIdIncrementer())
//                .start(this.step(jobRepository, platformTransactionManager))
//                .build();
//    }
//
//    @Bean
//    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
//        return new StepBuilder("templateStep", jobRepository)
//                .<, >chunk(10, platformTransactionManager)
//                .reader()
//                .writer()
//                .build();
//    }
//}
