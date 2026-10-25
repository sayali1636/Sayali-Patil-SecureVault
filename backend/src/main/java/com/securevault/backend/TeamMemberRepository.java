package com.securevault.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository
        extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeam(Team team);

    Optional<TeamMember> findByTeamAndUser(
            Team team,
            User user
    );
}