package com.tmax.superstorebatch.part3;

import com.tmax.superstorebatch.part3.entity.Person;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SavePersonConfiguration {
    private final EntityManagerFactory emf;
    @Bean
    public Job savePersonJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder("savePersonJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(this.savePersonStep(null, jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    @JobScope
    // 쉘에서 실행 시 파라미터를 준다
    public Step savePersonStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {

        return new StepBuilder("savePersonStep", jobRepository)
                .<Person, Person >chunk(10, platformTransactionManager)
                .reader(itemReader())
                .processor(itemProcessor(allowDuplicate))
                .writer(itemWriter())
                .faultTolerant()
                .skip(NotFoundNameException.class)
                .skipLimit(2)
                .retry(NotFoundNameException.class)
                .retryLimit(3)
                .build();
    }

    // 중복 처리 프로세서, 빈 문자열에 대한 처리 프로세서 2개를 두고, 이를 CompositeItemProcessor 로 묶는다.
    // 또한 validation을 통과하지 못하는 경우 최대 3번을
    private ItemProcessor<? super Person, ? extends Person> itemProcessor(String allowDuplicate) throws Exception {
        DuplicateValidationProcessor<Person> duplicateValidationProcessor
                = new DuplicateValidationProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate));

        ItemProcessor<Person, Person> validationProcessor = item -> {
            if(item.isNotEmptyName()){
                return item;
            }
            throw new NotFoundNameException();
        };
        CompositeItemProcessor<Person, Person> itemProcessor = new CompositeItemProcessorBuilder<Person, Person>()
                .delegates(new PersonValidationRetryProcessor(), validationProcessor, duplicateValidationProcessor)
                .build();

        itemProcessor.afterPropertiesSet();
        return itemProcessor;
    }

    private ItemWriter<Person> itemWriter() throws Exception {
        JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(emf)
                .build();
        ItemWriter<Person> logItemWriter = items -> log.info("person.size : {}", items.size());

        // writer 는 등록한 순서대로 작동하니 순서에 주의해야 함
        CompositeItemWriter<Person> itemWriter = new CompositeItemWriterBuilder<Person>()
                .delegates(jpaItemWriter, logItemWriter)
                .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;

    }

    private ItemReader<Person> itemReader() throws Exception {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("name", "age", "address");
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> new Person(
            fieldSet.readString(0),
            fieldSet.readString(1),
            fieldSet.readString(2))
        );
        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
                .name("savePersonItemReader")
                .encoding("UTF-8")
                .linesToSkip(1)
                .resource(new ClassPathResource("person.csv"))
                .lineMapper(lineMapper)
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }
}
