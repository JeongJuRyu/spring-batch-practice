package com.tmax.superstorebatch.config;

import com.tmax.superstorebatch.Coffee;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;


@Configuration
@Slf4j
public class CommonConfiguration {

    @Bean
    public Job chunkProcessingJob(JobRepository jobRepository, Step testStep, PlatformTransactionManager transactionManager) {
        return new JobBuilder("testJob", jobRepository)
                .start(chunkBaseStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step testStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("testStep", jobRepository)
                .tasklet(taskLet(), transactionManager)
                .build();
    }

    @Bean
    public Step chunkBaseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkBaseStep", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<String> itemWriter() {
        return items -> items.forEach(log::info);
    }

    private ItemProcessor<? super String, String> itemProcessor() {
        return item-> item + ", Spring Batch";
    }

    private ItemReader<String> itemReader() {
        return new ListItemReader<>(getItems());
    }

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
