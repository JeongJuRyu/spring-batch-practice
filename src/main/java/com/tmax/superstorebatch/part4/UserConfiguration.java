package com.tmax.superstorebatch.part4;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class UserConfiguration {
    private final UserRepository userRepository;
    private final EntityManagerFactory emf;

    @Bean
    public Job userJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder("userJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(this.userStep(jobRepository, platformTransactionManager))
                .next(this.userLevelUpStep(jobRepository, platformTransactionManager))
                .listener(new LevelUpJobExecutionListener(userRepository))
                .build();
    }

    @Bean
    public Step userStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("userStep", jobRepository)
                .tasklet(new SaveUserTasklet(userRepository), platformTransactionManager)
                .build();
    }

    @Bean
    public Step userLevelUpStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new StepBuilder("userLevelUpStep", jobRepository)
                .<User, User>chunk(100, platformTransactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<? super User> itemWriter() {
        return users -> {
            users.forEach(user -> {
                user.levelUp();
                userRepository.save(user);
            });
        };
    }

    private ItemProcessor<? super User, ? extends User> itemProcessor() {
        return user -> {
            if(user.availableLevelUp()){
                return user;
            }
            return null;
        };
    }

    private ItemReader<User> itemReader() throws Exception {
        JpaCursorItemReader<User> itemReader = new JpaCursorItemReaderBuilder<User>()
                .name("itemReader")
                .queryString("select u from Users u")
                .entityManagerFactory(emf)
                .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }
}
