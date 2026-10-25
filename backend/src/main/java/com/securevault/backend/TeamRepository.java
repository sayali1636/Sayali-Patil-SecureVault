package com.securevault.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByOwner(User owner);
}