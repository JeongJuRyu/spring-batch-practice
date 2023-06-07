package com.tmax.superstorebatch.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class TemplateConfiguration {
    @Bean
    public Job templateJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new JobBuilder("templateJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(this.step(jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("templateStep", jobRepository)
                .<Person, Person>chunk(10, platformTransactionManager)
                .reader(new CustomItemReader<>(getItems()))
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<Person> itemWriter() {
        return chunk -> log.info(chunk.getItems().stream()
                .map(Person::getName)
                .collect(Collectors.joining(", ")));
    }


    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            items.add(new Person(i+1, "test name" + i, "test age", "test address"));
        }
        return items;
    }
}
