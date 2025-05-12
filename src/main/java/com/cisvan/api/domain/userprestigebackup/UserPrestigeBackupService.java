package com.cisvan.api.domain.userprestigebackup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.userprestige.UserPrestige;
import com.cisvan.api.domain.userprestige.UserPrestigeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPrestigeBackupService {

    private final UserPrestigeRepository userPrestigeRepository;
    private final UserPrestigeBackupRepository backupRepository;

    @Scheduled(cron = "0 00 14 * * *")  // Todos los días a las 14:00
    @Transactional
    public void backupUserPrestige() {
        System.out.println("===> Iniciando el respaldo de prestigio de usuarios");

        try {
            List<UserPrestige> allPrestige = userPrestigeRepository.findAll();
            List<UserPrestigeBackup> backups = new ArrayList<>();

            for (UserPrestige prestige : allPrestige) {
                UserPrestigeBackup backup = UserPrestigeBackup.builder()
                        .userId(prestige.getUserId())
                        .currentRank(prestige.getCurrentRank())
                        .weightedScore(prestige.getWeightedScore())
                        .totalLikes(prestige.getTotalLikes())
                        .commentsWith10(prestige.getCommentsWith10())
                        .commentsWith15(prestige.getCommentsWith15())
                        .commentsWith50(prestige.getCommentsWith50())
                        .trendDirection(prestige.getTrendDirection())
                        .lastScoreEvaluated(prestige.getLastScoreEvaluated())
                        .lastLikeCheckpoint(prestige.getLastLikeCheckpoint())
                        .backupTimestamp(LocalDateTime.now())
                        .build();

                backups.add(backup);
                System.out.println("✅ Respaldo preparado para el usuario: " + prestige.getUserId());
            }

            backupRepository.saveAll(backups);
            System.out.println("✅ Respaldo de prestigio de usuarios completado exitosamente.");
        } catch (Exception e) {
            System.err.println("❌ Error durante el respaldo: " + e.getMessage());
        }
    }
}
