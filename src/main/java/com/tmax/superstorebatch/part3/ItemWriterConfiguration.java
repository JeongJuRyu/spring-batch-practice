package com.tmax.superstorebatch.part3;

import com.tmax.superstorebatch.part3.entity.Person;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ItemWriterConfiguration {
    private final DataSource dataSource;
    private final EntityManagerFactory emf;

    @Bean
    public Job itemWriterJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder("itemWriterJob", jobRepository)
                .incrementer(new RunIdIncrementer())
//                .start(jdbcBatchItemWriterStep(jobRepository, platformTransactionManager))
                .start(jpaItemWriterStep(jobRepository, platformTransactionManager))
                .build();
    }

//    private ItemWriter<Person> csvFileItemWriter() {
//        BeanWrapperFieldExtractor<Object> fieldExtractor = new BeanWrapperFieldExtractor<>();
//        fieldExtractor.setNames(new String[]{"id", "name", "age", "address"});
//
//        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
//        lineAggregator.setDelimiter(",");
//        lineAggregator.setFieldExtractor(fieldExtractor);
//
//        return null;
//    }

    @Bean
    public Step jpaItemWriterStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new StepBuilder("jpaItemWriterStep", jobRepository)
                .<Person, Person>chunk(10, platformTransactionManager)
                .reader(itemReader())
                .writer(jpaItemWriter())
                .build();
    }

    private ItemWriter<Person> jpaItemWriter() throws Exception {
        JpaItemWriter<Person> itemWriter = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(emf)
                .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    public Step jdbcBatchItemWriterStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("jdbcBatchItemWriterStep", jobRepository)
                .<Person, Person>chunk(10, platformTransactionManager)
                .reader(itemReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    private ItemWriter<Person> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<Person> itemWriter = new JdbcBatchItemWriterBuilder<Person>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into person(name, age, address) values (:name, :age, :address")
                .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
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
