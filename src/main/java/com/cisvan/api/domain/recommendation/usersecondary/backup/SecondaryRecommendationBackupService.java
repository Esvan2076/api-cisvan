package com.cisvan.api.domain.recommendation.usersecondary.backup;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.recommendation.usersecondary.UserSecondaryRecommendation;
import com.cisvan.api.domain.recommendation.usersecondary.UserSecondaryRecommendationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecondaryRecommendationBackupService {

    private final UserSecondaryRecommendationRepository userSecondaryRecommendationRepository; // Para leer las originales
    private final SecondaryRecommendationBackupRepository backupRepository;                  // Para guardar los respaldos

    /**
     * Realiza un respaldo de todas las UserSecondaryRecommendation existentes.
     * Se ejecuta diariamente a las 02:30 AM del servidor (para diferenciarlo del otro backup).
     * Asumiendo un RQNF similar al 77 para estas recomendaciones.
     */
    @Scheduled(cron = "0 30 2 * * *") // Todos los días a las 02:30:00 AM
    @Transactional
    public void backupSecondaryRecommendations() {
        System.out.println("===> Iniciando el respaldo de recomendaciones secundarias...");
        LocalDateTime backupTime = LocalDateTime.now();

        try {
            List<UserSecondaryRecommendation> allSecondaryRecommendations = userSecondaryRecommendationRepository.findAll();

            if (allSecondaryRecommendations.isEmpty()) {
                System.out.println("===> No se encontraron recomendaciones secundarias para respaldar.");
                return;
            }

            List<SecondaryRecommendationBackup> backupsToSave = new ArrayList<>();

            for (UserSecondaryRecommendation originalRec : allSecondaryRecommendations) {
                SecondaryRecommendationBackup backup = SecondaryRecommendationBackup.builder()
                        .originalSecondaryRecId(originalRec.getId())
                        .userId(originalRec.getUserId())
                        .tconst(originalRec.getTconst())
                        .rankForUser(originalRec.getRankForUser())
                        .crossMatchCountForUser(originalRec.getCrossMatchCountForUser())
                        .aggregatedMatchScoreForUser(originalRec.getAggregatedMatchScoreForUser())
                        .originalLastCalculatedAt(originalRec.getLastCalculatedAt())
                        .backupTakenAt(backupTime)
                        .build();
                backupsToSave.add(backup);
            }

            if (!backupsToSave.isEmpty()) {
                backupRepository.saveAll(backupsToSave);
                System.out.println("✅ Respaldo de " + backupsToSave.size() + " recomendaciones secundarias completado exitosamente.");
            } else {
                System.out.println("===> No se prepararon ítems válidos para el respaldo de recomendaciones secundarias.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error durante el respaldo de recomendaciones secundarias: " + e.getMessage());
            e.printStackTrace();
        }
    }
}