package com.cisvan.api.domain.userprestigebackup;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrestigeBackupRepository extends JpaRepository<UserPrestigeBackup, Long> {
    List<UserPrestigeBackup> findByUserId(Long userId);
}
