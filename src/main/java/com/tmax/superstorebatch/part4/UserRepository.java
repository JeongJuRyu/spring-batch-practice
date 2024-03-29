package com.tmax.superstorebatch.part4;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByUpdatedDate(LocalDate localDate);
}
