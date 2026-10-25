package com.securevault.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialRepository
        extends JpaRepository<Credential, Long> {

    // Owner's credentials
    List<Credential> findByUser(User user);

    // Credentials shared with a particular user
    List<Credential> findBySharedUser(User sharedUser);

    // Credentials belonging to a Team Vault
    List<Credential> findByTeam(Team team);

    // Search owner's credentials by title, username, website or category
    List<Credential> findByUserAndTitleContainingIgnoreCaseOrUserAndUsernameContainingIgnoreCaseOrUserAndWebsiteContainingIgnoreCaseOrUserAndCategoryContainingIgnoreCase(
            User user1,
            String title,
            User user2,
            String username,
            User user3,
            String website,
            User user4,
            String category
    );
}