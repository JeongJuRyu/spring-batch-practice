package com.tmax.superstorebatch.part3;

import com.tmax.superstorebatch.part3.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class ItemProcessorConfiguration {

    @Bean
    public Job itemProcessorJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder("itemProcessorJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(this.itemProcessorStep(jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step itemProcessorStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("itemProcessorStep", jobRepository)
                .<Person, Person>chunk(10, platformTransactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<Person> itemWriter() {
        return items ->
            items.forEach(x -> log.info("PERSON.ID : {}", x.getId()));
    }

    private ItemProcessor<? super Person, ? extends Person> itemProcessor() {
        return item -> {
            if(item.getId() % 2 == 0){
                return item;
            }
            return null;
        };
    }

    private ItemReader<Person> itemReader() {
        return new CustomItemReader<>(getItems());
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(Person.builder()
                    .name("test name + i")
                    .age("test age")
                    .address("test address")
                    .build());
        }
        return items;
    }
}
