package com.securevault.backend;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    
public CredentialService(
        CredentialRepository credentialRepository,
        UserRepository userRepository,
        EncryptionService encryptionService,
        AuditLogService auditLogService) {

    this.credentialRepository = credentialRepository;
    this.userRepository = userRepository;
    this.encryptionService = encryptionService;
    this.auditLogService = auditLogService;
}



    // =========================
    // VALIDATE CREDENTIAL CATEGORY
    // =========================

    private void validateCategory(String category) {

        if (category == null || category.isBlank()) {
            throw new RuntimeException(
                    "Credential category is required");
        }

        if (!category.equals("Website Login") &&
                !category.equals("Email") &&
                !category.equals("Banking") &&
                !category.equals("Social Media") &&
                !category.equals("Application Accounts") &&
                !category.equals("API Keys") &&
                !category.equals("Secure Notes")) {

            throw new RuntimeException(
                    "Invalid credential category");
        }
    }

    // =========================
    // CREATE CREDENTIAL
    // =========================

    public Credential createCredential(
            Credential credential,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        validateCategory(credential.getCategory());

        credential.setUser(user);

        // Encrypt password before saving
        credential.setPassword(
                encryptionService.encrypt(
                        credential.getPassword()
                )
        );

        
Credential savedCredential =
        credentialRepository.save(credential);

auditLogService.logActivity(
        email,
        "CREDENTIAL_CREATED",
        "A new credential was created"
);

return savedCredential;

    }

    // =========================
    // GET USER CREDENTIALS
    // =========================

    public List<Credential> getUserCredentials(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        // Decrypt password when retrieving
        credentials.forEach(credential ->
                credential.setPassword(
                        encryptionService.decrypt(
                                credential.getPassword()
                        )
                )
        );

        return credentials;
    }

    // =========================
    // SEARCH USER CREDENTIALS
    // =========================

    public List<Credential> searchCredentials(
            String email,
            String search) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (search == null || search.isBlank()) {
            return getUserCredentials(email);
        }

        String searchText = search.trim();

        List<Credential> credentials =
                credentialRepository
                        .findByUserAndTitleContainingIgnoreCaseOrUserAndUsernameContainingIgnoreCaseOrUserAndWebsiteContainingIgnoreCaseOrUserAndCategoryContainingIgnoreCase(
                                user,
                                searchText,
                                user,
                                searchText,
                                user,
                                searchText,
                                user,
                                searchText
                        );

        // Decrypt passwords for response
        credentials.forEach(credential ->
                credential.setPassword(
                        encryptionService.decrypt(
                                credential.getPassword()
                        )
                )
        );

        return credentials;
    }

    // =========================
    // FAVORITE / UNFAVORITE CREDENTIAL
    // =========================

    public Credential toggleFavorite(
            Long id,
            String email) {

        Credential credential = credentialRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Credential not found"));

        // Verify ownership
        if (!credential.getUser()
                .getEmail()
                .equals(email)) {

            throw new RuntimeException(
                    "You are not authorized to modify this credential");
        }

        // Toggle favorite status
        credential.setFavorite(
                !credential.isFavorite()
        );

        Credential savedCredential =
                credentialRepository.save(credential);
                
        auditLogService.logActivity(
        email,
        "FAVORITE_TOGGLED",
        "Credential favorite status changed"
);

        // Decrypt password for response
        savedCredential.setPassword(
                encryptionService.decrypt(
                        savedCredential.getPassword()
                )
        );

        return savedCredential;
    }

    // =========================
    // GET FAVORITE CREDENTIALS
    // =========================

    public List<Credential> getFavoriteCredentials(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        // Keep only favorite credentials
        credentials.removeIf(
                credential -> !credential.isFavorite()
        );

        // Decrypt passwords
        credentials.forEach(credential ->
                credential.setPassword(
                        encryptionService.decrypt(
                                credential.getPassword()
                        )
                )
        );

        return credentials;
    }

    // =========================
    // GET SINGLE CREDENTIAL
    // =========================

    public Credential getCredentialById(
            Long id,
            String email) {

        Credential credential = credentialRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Credential not found"));

        boolean isOwner =
                credential.getUser().getEmail().equals(email);

        if (!isOwner) {

            // Check shared user access
            if (credential.getSharedUser() == null ||
                    !credential.getSharedUser()
                            .getEmail()
                            .equals(email)) {

                throw new RuntimeException(
                        "You are not authorized to access this credential");
            }

            // Check sharing status
            if (!"ACTIVE".equals(
                    credential.getSharingStatus())) {

                throw new RuntimeException(
                        "Credential sharing is not active");
            }

            // Check expiration
            if (credential.getExpiration() != null &&
                    credential.getExpiration()
                            .isBefore(LocalDateTime.now())) {

                throw new RuntimeException(
                        "Credential sharing has expired");
            }

            // Validate permission
            if (!"VIEW_ONLY".equals(
                    credential.getPermissionLevel()) &&
                    !"EDIT".equals(
                            credential.getPermissionLevel()) &&
                    !"FULL_MANAGEMENT".equals(
                            credential.getPermissionLevel())) {

                throw new RuntimeException(
                        "Invalid permission level");
            }
        }

        // Decrypt password when retrieving
        credential.setPassword(
                encryptionService.decrypt(
                        credential.getPassword()
                )
        );

        return credential;
    }

    // =========================
    // UPDATE CREDENTIAL
    // =========================

    public Credential updateCredential(
            Long id,
            Credential updatedCredential,
            String email) {

        Credential credential = credentialRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Credential not found"));

        boolean isOwner =
                credential.getUser().getEmail().equals(email);

        boolean isSharedUser =
                credential.getSharedUser() != null &&
                credential.getSharedUser()
                        .getEmail()
                        .equals(email);

        // Owner can always update
        if (!isOwner) {

            if (!isSharedUser) {
                throw new RuntimeException(
                        "You are not authorized to update this credential");
            }

            // Sharing must be active
            if (!"ACTIVE".equals(
                    credential.getSharingStatus())) {

                throw new RuntimeException(
                        "Credential sharing is not active");
            }

            // Check expiration
            if (credential.getExpiration() != null &&
                    credential.getExpiration()
                            .isBefore(LocalDateTime.now())) {

                throw new RuntimeException(
                        "Credential sharing has expired");
            }

            // Only EDIT and FULL_MANAGEMENT can update
            if (!"EDIT".equals(
                    credential.getPermissionLevel()) &&
                    !"FULL_MANAGEMENT".equals(
                            credential.getPermissionLevel())) {

                throw new RuntimeException(
                        "You do not have permission to edit this credential");
            }
        }

        validateCategory(
                updatedCredential.getCategory());

        credential.setTitle(
                updatedCredential.getTitle());

        credential.setUsername(
                updatedCredential.getUsername());

        // Encrypt updated password
        credential.setPassword(
                encryptionService.encrypt(
                        updatedCredential.getPassword()
                )
        );

        credential.setWebsite(
                updatedCredential.getWebsite());

        credential.setCategory(
                updatedCredential.getCategory());

        credential.setNotes(
                updatedCredential.getNotes());

        credential.setFavorite(
                updatedCredential.isFavorite()
        );

        
Credential savedCredential =
        credentialRepository.save(credential);

// Record audit activity
auditLogService.logActivity(
        email,
        "CREDENTIAL_UPDATED",
        "A credential was updated"
);

// Decrypt password for response
savedCredential.setPassword(
        encryptionService.decrypt(
                savedCredential.getPassword()
        )
);

return savedCredential;

    }

    // =========================
    // DELETE CREDENTIAL
    // =========================

    public void deleteCredential(
            Long id,
            String email) {

        Credential credential = credentialRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Credential not found"));

        boolean isOwner =
                credential.getUser().getEmail().equals(email);

        boolean isSharedUser =
                credential.getSharedUser() != null &&
                credential.getSharedUser()
                        .getEmail()
                        .equals(email);

        // Owner can always delete
       
if (isOwner) {
    credentialRepository.delete(credential);

    auditLogService.logActivity(
            email,
            "CREDENTIAL_DELETED",
            "A credential was deleted"
    );

    return;
}


        // Shared user access check
        if (!isSharedUser) {
            throw new RuntimeException(
                    "You are not authorized to delete this credential");
        }

        if (!"ACTIVE".equals(
                credential.getSharingStatus())) {

            throw new RuntimeException(
                    "Credential sharing is not active");
        }

        if (credential.getExpiration() != null &&
                credential.getExpiration()
                        .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Credential sharing has expired");
        }

        // Only FULL_MANAGEMENT can delete
        if (!"FULL_MANAGEMENT".equals(
                credential.getPermissionLevel())) {

            throw new RuntimeException(
                    "You do not have permission to delete this credential");
        }

        
credentialRepository.delete(credential);

auditLogService.logActivity(
        email,
        "CREDENTIAL_DELETED",
        "A shared credential was deleted"
);

    }

    // =========================
    // SHARE CREDENTIAL
    // =========================

    public Credential shareCredential(
            Long credentialId,
            String ownerEmail,
            String sharedUserEmail,
            String permissionLevel,
            LocalDateTime expiration) {

        Credential credential = credentialRepository
                .findById(credentialId)
                .orElseThrow(() ->
                        new RuntimeException("Credential not found"));

        // Verify owner
        if (!credential.getUser()
                .getEmail()
                .equals(ownerEmail)) {

            throw new RuntimeException(
                    "Only the credential owner can share it");
        }

        User sharedUser = userRepository
                .findByEmail(sharedUserEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Shared user not found"));

        // Owner cannot share with himself
        if (ownerEmail.equals(sharedUserEmail)) {

            throw new RuntimeException(
                    "Owner cannot share credential with himself");
        }

        // Validate permission
        if (!"VIEW_ONLY".equals(permissionLevel) &&
                !"EDIT".equals(permissionLevel) &&
                !"FULL_MANAGEMENT".equals(permissionLevel)) {

            throw new RuntimeException(
                    "Invalid permission level");
        }

        // Expiration must be in future
        if (expiration != null &&
                expiration.isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Expiration must be a future date");
        }

        credential.setSharedUser(sharedUser);
        credential.setPermissionLevel(
                permissionLevel);
        credential.setSharingStatus("ACTIVE");
        credential.setExpiration(expiration);

        return credentialRepository.save(credential);
    }

    // =========================
    // GET SHARED CREDENTIALS
    // =========================

    public List<Credential> getSharedCredentials(
            String email) {

        User sharedUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Credential> credentials =
                credentialRepository
                        .findBySharedUser(sharedUser);

        credentials.removeIf(credential ->
                !"ACTIVE".equals(
                        credential.getSharingStatus()) ||
                (credential.getExpiration() != null &&
                 credential.getExpiration()
                         .isBefore(LocalDateTime.now()))
        );

        // Decrypt passwords for valid shared credentials
        credentials.forEach(credential ->
                credential.setPassword(
                        encryptionService.decrypt(
                                credential.getPassword()
                        )
                )
        );

        return credentials;
    }

    // =========================
    // REMOVE SHARING
    // =========================

    public Credential removeSharing(
            Long credentialId,
            String ownerEmail) {

        Credential credential = credentialRepository
                .findById(credentialId)
                .orElseThrow(() ->
                        new RuntimeException("Credential not found"));

        // Only owner can remove sharing
        if (!credential.getUser()
                .getEmail()
                .equals(ownerEmail)) {

            throw new RuntimeException(
                    "Only the credential owner can remove sharing");
        }

        credential.setSharingStatus("REMOVED");
        credential.setSharedUser(null);
        credential.setPermissionLevel(null);
        credential.setExpiration(null);

        return credentialRepository.save(credential);
    }
}