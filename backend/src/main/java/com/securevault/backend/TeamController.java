package com.securevault.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // =========================
    // CREATE TEAM
    // =========================

    @PostMapping
    public ResponseEntity<?> createTeam(
            @RequestParam String teamName,
            Authentication authentication) {

        try {

            String ownerEmail =
                    authentication.getName();

            Team team =
                    teamService.createTeam(
                            teamName,
                            ownerEmail
                    );

            return ResponseEntity.ok(team);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // GET MY TEAMS
    // =========================

    @GetMapping
    public ResponseEntity<?> getMyTeams(
            Authentication authentication) {

        try {

            String ownerEmail =
                    authentication.getName();

            return ResponseEntity.ok(
                    teamService.getMyTeams(
                            ownerEmail
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // ADD TEAM MEMBER
    // =========================

    @PostMapping("/{teamId}/members")
    public ResponseEntity<?> addMember(
            @PathVariable Long teamId,
            @RequestParam String memberEmail,
            @RequestParam String permissionLevel,
            Authentication authentication) {

        try {

            String ownerEmail =
                    authentication.getName();

            TeamMember member =
                    teamService.addMember(
                            teamId,
                            ownerEmail,
                            memberEmail,
                            permissionLevel
                    );

            return ResponseEntity.ok(member);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // GET TEAM MEMBERS
    // =========================

    @GetMapping("/{teamId}/members")
    public ResponseEntity<?> getTeamMembers(
            @PathVariable Long teamId,
            Authentication authentication) {

        try {

            String ownerEmail =
                    authentication.getName();

            return ResponseEntity.ok(
                    teamService.getTeamMembers(
                            teamId,
                            ownerEmail
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // UPDATE TEAM MEMBER PERMISSION
    // =========================

    @PutMapping("/{teamId}/members/{memberId}/permission")
    public ResponseEntity<?> updateMemberPermission(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @RequestParam String permissionLevel,
            Authentication authentication) {

        try {

            String ownerEmail =
                    authentication.getName();

            TeamMember updatedMember =
                    teamService.updateMemberPermission(
                            teamId,
                            ownerEmail,
                            memberId,
                            permissionLevel
                    );

            return ResponseEntity.ok(updatedMember);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // ADD CREDENTIAL TO TEAM
    // =========================

    @PostMapping("/{teamId}/credentials")
    public ResponseEntity<?> addCredentialToTeam(
            @PathVariable Long teamId,
            @RequestBody Credential credential,
            Authentication authentication) {

        try {

            String ownerEmail =
                    authentication.getName();

            Credential savedCredential =
                    teamService.addCredentialToTeam(
                            teamId,
                            ownerEmail,
                            credential
                    );

            return ResponseEntity.ok(
                    savedCredential
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // GET TEAM CREDENTIALS
    // =========================

    @GetMapping("/{teamId}/credentials")
    public ResponseEntity<?> getTeamCredentials(
            @PathVariable Long teamId,
            Authentication authentication) {

        try {

            String userEmail =
                    authentication.getName();

            List<Credential> credentials =
                    teamService.getTeamCredentials(
                            teamId,
                            userEmail
                    );

            return ResponseEntity.ok(credentials);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // UPDATE TEAM CREDENTIAL
    // =========================

    @PutMapping(
            "/{teamId}/credentials/{credentialId}"
    )
    public ResponseEntity<?> updateTeamCredential(
            @PathVariable Long teamId,
            @PathVariable Long credentialId,
            @RequestBody Credential updatedCredential,
            Authentication authentication) {

        try {

            String userEmail =
                    authentication.getName();

            teamService.checkEditPermission(
                    teamId,
                    userEmail
            );

            Credential credential =
                    teamService.updateTeamCredential(
                            teamId,
                            credentialId,
                            userEmail,
                            updatedCredential
                    );

            return ResponseEntity.ok(credential);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // DELETE TEAM CREDENTIAL
    // =========================

    @DeleteMapping(
            "/{teamId}/credentials/{credentialId}"
    )
    public ResponseEntity<?> deleteTeamCredential(
            @PathVariable Long teamId,
            @PathVariable Long credentialId,
            Authentication authentication) {

        try {

            String userEmail =
                    authentication.getName();

            teamService.checkDeletePermission(
                    teamId,
                    userEmail
            );

            teamService.deleteTeamCredential(
                    teamId,
                    credentialId,
                    userEmail
            );

            return ResponseEntity.ok(
                    "Team credential deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}