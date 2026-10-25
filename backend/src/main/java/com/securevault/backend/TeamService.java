package com.securevault.backend;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final EncryptionService encryptionService;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository,
            CredentialRepository credentialRepository,
            EncryptionService encryptionService) {

        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
    }

    // =========================
    // CREATE TEAM
    // =========================

    public Team createTeam(
            String teamName,
            String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Team team = new Team(teamName, owner);

        Team savedTeam = teamRepository.save(team);

        TeamMember ownerMember =
                new TeamMember(
                        savedTeam,
                        owner,
                        "FULL_MANAGEMENT"
                );

        teamMemberRepository.save(ownerMember);

        return savedTeam;
    }

    // =========================
    // GET MY TEAMS
    // =========================

    public List<Team> getMyTeams(
            String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return teamRepository.findByOwner(owner);
    }

    // =========================
    // ADD TEAM MEMBER
    // =========================

    public TeamMember addMember(
            Long teamId,
            String ownerEmail,
            String memberEmail,
            String permissionLevel) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        if (!team.getOwner()
                .getEmail()
                .equals(ownerEmail)) {

            throw new RuntimeException(
                    "Only team owner can add members");
        }

        User member = userRepository.findByEmail(memberEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (member.getEmail().equals(ownerEmail)) {

            throw new RuntimeException(
                    "Owner is already a team member");
        }

        if (!permissionLevel.equals("VIEW_ONLY") &&
                !permissionLevel.equals("EDIT") &&
                !permissionLevel.equals("FULL_MANAGEMENT")) {

            throw new RuntimeException(
                    "Invalid permission level");
        }

        if (teamMemberRepository
                .findByTeamAndUser(team, member)
                .isPresent()) {

            throw new RuntimeException(
                    "User is already a team member");
        }

        TeamMember teamMember =
                new TeamMember(
                        team,
                        member,
                        permissionLevel
                );

        return teamMemberRepository.save(teamMember);
    }

    // =========================
    // GET TEAM MEMBERS
    // =========================

    public List<TeamMember> getTeamMembers(
            Long teamId,
            String ownerEmail) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        if (!team.getOwner()
                .getEmail()
                .equals(ownerEmail)) {

            throw new RuntimeException(
                    "Only team owner can view members");
        }

        return teamMemberRepository.findByTeam(team);
    }

    // =========================
    // UPDATE TEAM MEMBER PERMISSION
    // =========================

    public TeamMember updateMemberPermission(
            Long teamId,
            String ownerEmail,
            Long memberId,
            String permissionLevel) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        // Only team owner can change permissions
        if (!team.getOwner()
                .getEmail()
                .equals(ownerEmail)) {

            throw new RuntimeException(
                    "Only team owner can update member permission");
        }

        // Validate permission
        if (!permissionLevel.equals("VIEW_ONLY") &&
                !permissionLevel.equals("EDIT") &&
                !permissionLevel.equals("FULL_MANAGEMENT")) {

            throw new RuntimeException(
                    "Invalid permission level");
        }

        TeamMember member =
                teamMemberRepository
                        .findById(memberId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Team member not found"));

        // Make sure member belongs to this team
        if (!member.getTeam()
                .getId()
                .equals(teamId)) {

            throw new RuntimeException(
                    "Member does not belong to this Team");
        }

        member.setPermissionLevel(
                permissionLevel
        );

        return teamMemberRepository.save(member);
    }

    // =========================
    // ADD CREDENTIAL TO TEAM
    // =========================

    public Credential addCredentialToTeam(
            Long teamId,
            String ownerEmail,
            Credential credential) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        if (!team.getOwner()
                .getEmail()
                .equals(ownerEmail)) {

            throw new RuntimeException(
                    "Only team owner can add credentials");
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        credential.setUser(owner);
        credential.setTeam(team);

        // Encrypt password before storing
        if (credential.getPassword() != null &&
                !credential.getPassword().isBlank()) {

            credential.setPassword(
                    encryptionService.encrypt(
                            credential.getPassword()
                    )
            );
        }

        return credentialRepository.save(credential);
    }

    // =========================
    // GET TEAM CREDENTIALS
    // =========================

    public List<Credential> getTeamCredentials(
            Long teamId,
            String userEmail) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        checkTeamAccess(team, user);

        List<Credential> credentials =
                credentialRepository.findByTeam(team);

        // Decrypt password only for response
        for (Credential credential : credentials) {

            if (credential.getPassword() != null &&
                    !credential.getPassword().isBlank()) {

                credential.setPassword(
                        encryptionService.decrypt(
                                credential.getPassword()
                        )
                );
            }
        }

        return credentials;
    }

    // =========================
    // CHECK TEAM ACCESS
    // =========================

    private void checkTeamAccess(
            Team team,
            User user) {

        // Team owner has full access
        if (team.getOwner()
                .getId()
                .equals(user.getId())) {

            return;
        }

        // Check team membership
        teamMemberRepository
                .findByTeamAndUser(team, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "You do not have access to this Team Vault"));
    }

    // =========================
    // GET TEAM PERMISSION
    // =========================

    public String getTeamPermission(
            Long teamId,
            String userEmail) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Owner always has full management
        if (team.getOwner()
                .getId()
                .equals(user.getId())) {

            return "FULL_MANAGEMENT";
        }

        TeamMember member =
                teamMemberRepository
                        .findByTeamAndUser(team, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a team member"));

        return member.getPermissionLevel();
    }

    // =========================
    // CHECK EDIT PERMISSION
    // =========================

    public void checkEditPermission(
            Long teamId,
            String userEmail) {

        String permission =
                getTeamPermission(
                        teamId,
                        userEmail
                );

        if (!permission.equals("EDIT") &&
                !permission.equals("FULL_MANAGEMENT")) {

            throw new RuntimeException(
                    "You have VIEW_ONLY permission. Edit access denied.");
        }
    }

    // =========================
    // CHECK DELETE PERMISSION
    // =========================

    public void checkDeletePermission(
            Long teamId,
            String userEmail) {

        String permission =
                getTeamPermission(
                        teamId,
                        userEmail
                );

        if (!permission.equals("FULL_MANAGEMENT")) {

            throw new RuntimeException(
                    "Delete access denied. FULL_MANAGEMENT permission required.");
        }
    }

    // =========================
    // UPDATE TEAM CREDENTIAL
    // =========================

    public Credential updateTeamCredential(
            Long teamId,
            Long credentialId,
            String userEmail,
            Credential updatedCredential) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Team access check
        checkTeamAccess(team, user);

        // Service-level permission check
        checkEditPermission(
                teamId,
                userEmail
        );

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Credential not found"));

        // Make sure credential belongs to this team
        if (credential.getTeam() == null ||
                !credential.getTeam()
                        .getId()
                        .equals(teamId)) {

            throw new RuntimeException(
                    "Credential does not belong to this Team Vault");
        }

        if (updatedCredential.getTitle() != null) {

            credential.setTitle(
                    updatedCredential.getTitle()
            );
        }

        if (updatedCredential.getUsername() != null) {

            credential.setUsername(
                    updatedCredential.getUsername()
            );
        }

        if (updatedCredential.getWebsite() != null) {

            credential.setWebsite(
                    updatedCredential.getWebsite()
            );
        }

        if (updatedCredential.getCategory() != null) {

            credential.setCategory(
                    updatedCredential.getCategory()
            );
        }

        if (updatedCredential.getNotes() != null) {

            credential.setNotes(
                    updatedCredential.getNotes()
            );
        }

        // Encrypt updated password
        if (updatedCredential.getPassword() != null &&
                !updatedCredential.getPassword().isBlank()) {

            credential.setPassword(
                    encryptionService.encrypt(
                            updatedCredential.getPassword()
                    )
            );
        }

        Credential savedCredential =
                credentialRepository.save(credential);

        // Decrypt only for response
        if (savedCredential.getPassword() != null &&
                !savedCredential.getPassword().isBlank()) {

            savedCredential.setPassword(
                    encryptionService.decrypt(
                            savedCredential.getPassword()
                    )
            );
        }

        return savedCredential;
    }

    // =========================
    // DELETE TEAM CREDENTIAL
    // =========================

    public void deleteTeamCredential(
            Long teamId,
            Long credentialId,
            String userEmail) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Team access check
        checkTeamAccess(team, user);

        // Service-level permission check
        checkDeletePermission(
                teamId,
                userEmail
        );

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Credential not found"));

        // Make sure credential belongs to this team
        if (credential.getTeam() == null ||
                !credential.getTeam()
                        .getId()
                        .equals(teamId)) {

            throw new RuntimeException(
                    "Credential does not belong to this Team Vault");
        }

        credentialRepository.delete(credential);
    }
}