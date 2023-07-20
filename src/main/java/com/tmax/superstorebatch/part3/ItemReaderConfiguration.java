package com.tmax.superstorebatch.part3;

import com.tmax.superstorebatch.part3.entity.Person;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class ItemReaderConfiguration {
    private final EntityManagerFactory emf;
    private final DataSource dataSource;

//    @Bean
//    public Job chunkProcessingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("testJob", jobRepository)
//                .start(chunkBaseStep(null, jobRepository, transactionManager))
//                .build();
//    }

    @Bean
    public Step testStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("testStep", jobRepository)
                .tasklet(taskLet(), transactionManager)
                .build();
    }

    @Bean
    public Job itemReaderJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new JobBuilder("itemReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(this.jdbcStep(jobRepository, transactionManager))
                .next(jpaStep(jobRepository, transactionManager))
                .build();
    }

//    @Bean
//    public Step customItemReaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new StepBuilder("customItemReaderStep", jobRepository)
//                .<Person, Person>chunk(10, transactionManager)
//                .reader(new CustomItemReader<String>(getItems()))
//                .writer(itemWriter())
//                .build();
//    }
    @Bean
    public Step jdbcStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("jdbcStep", jobRepository)
                .<Person, Person>chunk(10, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Step jpaStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("jpaStep", jobRepository)
                .<Person, Person>chunk(10, transactionManager)
                .reader(this.jpaCursorItemReader())
                .writer(itemWriter())
                .build();
    }
    private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception {
        JpaCursorItemReader<Person> itemReader = new JpaCursorItemReaderBuilder<Person>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(emf)
                .queryString("select p from Person p")
                .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }

    private JdbcCursorItemReader<Person> jdbcCursorItemReader() throws Exception {
        JdbcCursorItemReader<Person> itemReader = new JdbcCursorItemReaderBuilder<Person>()
                .name("jdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("select id, name, age, address from person")
                .rowMapper((rs, rowNum) -> new Person(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)))
                .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }
//    private FlatFileItemReader<Person> csvFileItemReader(){
//        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
//        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
//        tokenizer.setNames("id", "name", "age", "address");
//        lineMapper.setLineTokenizer(tokenizer);
//        lineMapper.setFieldSetMapper(fieldSet -> {
//            int id = fieldSet.readInt("id");
//            String name = fieldSet.readString("name");
//            String age = fieldSet.readString("age");
//            String address = fieldSet.readString("address");
//
//            return new Person(id, name, age, address);
//        });
//
//        new FlatFileItemReaderBuilder<Person>()
//                .name("csvFileItemReader")
//                .encoding("UTF-8")
//                .resource(new ClassPathResource("test.csv"));
//    }

    private ItemWriter<Person> itemWriter() {
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
