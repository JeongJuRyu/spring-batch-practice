package com.tmax.superstorebatch.part4;

import com.tmax.superstorebatch.part5.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet {

    private final int SIZE = 100;
    private final UserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<User> users = createUsers();

        Collections.shuffle(users);
        userRepository.saveAll(users);

        return RepeatStatus.FINISHED;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < SIZE; i++) {
            users.add(User.builder()
                            .orders(Collections.singletonList(Orders.builder()
                                        .amount(1000)
                                        .createdDate(LocalDate.of(2020, 11, 1))
                                        .itemName("item" + i)
                                        .build()))
                    .username("testUserName" + i)
                    .build());
        }

        for(int i = 0; i < SIZE; i++){
            users.add(User.builder()
                    .orders(Collections.singletonList(Orders.builder()
                            .amount(200000)
                            .createdDate(LocalDate.of(2020, 11, 2))
                            .itemName("item" + i)
                            .build()))
                    .username("testUserName" + i)
                    .build());
        }

        for(int i = 0; i < SIZE; i++){
            users.add(User.builder()
                    .orders(Collections.singletonList(Orders.builder()
                            .amount(300000)
                            .createdDate(LocalDate.of(2020, 11, 3))
                            .itemName("item" + i)
                            .build()))
                    .username("testUserName" + i)
                    .build());
        }

        for(int i = 0; i < SIZE; i++){
            users.add(User.builder()
                    .orders(Collections.singletonList(Orders.builder()
                            .amount(500000)
                            .createdDate(LocalDate.of(2020, 11, 4))
                            .itemName("item" + i)
                            .build()))
                    .username("testUserName" + i)
                    .build());
        }
        return users;
    }
}
