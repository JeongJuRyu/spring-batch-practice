package com.tmax.superstorebatch.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Configuration
@Slf4j
public class CommonConfiguration {

    @Bean
    public Job chunkProcessingJob(JobRepository jobRepository, Step testStep, PlatformTransactionManager transactionManager) {
        return new JobBuilder("testJob", jobRepository)
                .start(chunkBaseStep(null, jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step testStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("testStep", jobRepository)
                .tasklet(taskLet(), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step chunkBaseStep(
            @Value("#{jobParameters[chunkSize]}") String chunkSize,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkBaseStep", jobRepository)
                .<String, String>chunk(
                        StringUtils.hasText(chunkSize) ? Integer.parseInt(chunkSize) : 10
                        , transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .allowStartIfComplete(true)
                .build();
    }

    private ItemWriter<String> itemWriter() {
        return items -> log.info("items size : {}", items.size());
    }

    private ItemProcessor<? super String, String> itemProcessor() {
        return item-> item + ", Spring Batch";
    }

    private ItemReader<String> itemReader() {
        return new ListItemReader<>(getItems());
    }


    @Bean
    @StepScope
    public Tasklet taskLet() {
        return (contribution, chunkContext) -> {
            List<String> items = getItems();
            log.info("task item size :" + items.size());

            return RepeatStatus.FINISHED;
        };
    }

    private List<String> getItems() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(i + "Hello");
        }
        return items;
    }

}
