package com.cisvan.api.domain.recommendation.userrecommendationset.backup;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.recommendation.userrecomendationitem.UserRecommendationItem;
import com.cisvan.api.domain.recommendation.userrecomendationitem.UserRecommendationItemRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor // Usando Lombok para la inyección de dependencias por constructor
public class PrimaryRecommendationBackupService {

    private final UserRecommendationItemRepository userRecommendationItemRepository; // Para leer los ítems originales
    private final PrimaryRecommendationItemBackupRepository backupRepository;       // Para guardar los respaldos

    /**
     * Realiza un respaldo de todos los UserRecommendationItem existentes.
     * Se ejecuta diariamente a las 02:00 AM del servidor.
     * RQNF 77: El sistema debe guardar un respaldo de las recomendaciones primarias cada
     * 24 horas a las 2:00am del servidor.
     */
    @Scheduled(cron = "0 30 2 * * *") // Todos los días a las 02:00:00 AM
    @Transactional // Es bueno que las operaciones de BD sean transaccionales
    public void backupPrimaryRecommendationItems() {
        System.out.println("===> [RQNF 77] Iniciando el respaldo de ítems de recomendaciones primarias...");
        LocalDateTime backupTime = LocalDateTime.now(); // Usar el mismo timestamp para todos los ítems de este lote

        try {
            // 1. Obtener todos los ítems de recomendaciones primarias actuales
            // ¡Cuidado si esta tabla es muy grande! Podrías necesitar paginación o streaming.
            // Para un respaldo diario completo, findAll() es lo más directo si el tamaño es manejable.
            List<UserRecommendationItem> allPrimaryItems = userRecommendationItemRepository.findAll();

            if (allPrimaryItems.isEmpty()) {
                System.out.println("===> No se encontraron ítems de recomendaciones primarias para respaldar.");
                return;
            }

            List<PrimaryRecommendationItemBackup> backupsToSave = new ArrayList<>();

            for (UserRecommendationItem originalItem : allPrimaryItems) {
                if (originalItem.getRecommendationSet() == null || originalItem.getRecommendationSet().getUserId() == null) {
                    System.err.println("    ❌ Ítem de recomendación primaria con ID " + originalItem.getId() + 
                                       " no tiene un UserRecommendationSet o userId asociado. Omitiendo respaldo para este ítem.");
                    continue; 
                }

                PrimaryRecommendationItemBackup backup = PrimaryRecommendationItemBackup.builder()
                        .originalItemId(originalItem.getId())
                        .originalSetId(originalItem.getRecommendationSet().getId())
                        .userId(originalItem.getRecommendationSet().getUserId()) // Obtener userId del Set asociado
                        .tconst(originalItem.getTconst())
                        .rankInSet(originalItem.getRank())
                        .matchCount(originalItem.getMatchCount())
                        .matchScore(originalItem.getMatchScore())
                        .titleRatingAtRecommendation(originalItem.getTitleRatingAtRecommendation())
                        .itemSavedAt(originalItem.getSavedAt()) // Timestamp de cuando se creó el ítem original
                        .backupTakenAt(backupTime) // Timestamp de cuándo se está tomando este respaldo
                        .build();
                
                backupsToSave.add(backup);
            }

            if (!backupsToSave.isEmpty()) {
                backupRepository.saveAll(backupsToSave);
                System.out.println("✅ [RQNF 77] Respaldo de " + backupsToSave.size() + " ítems de recomendaciones primarias completado exitosamente.");
            } else {
                System.out.println("===> No se prepararon ítems válidos para el respaldo.");
            }

        } catch (Exception e) {
            System.err.println("❌ [RQNF 77] Error durante el respaldo de ítems de recomendaciones primarias: " + e.getMessage());
            e.printStackTrace(); // Importante para depurar
        }
    }
}