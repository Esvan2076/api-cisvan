package com.cisvan.api.domain.userprestige;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.comment.CommentRepository;
import com.cisvan.api.domain.commentLike.CommentLikeRepository;
import com.cisvan.api.domain.notification.services.NotificationService;
import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;
import com.cisvan.api.domain.userprestige.mapper.UserPrestigeMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPrestigeService {

    private final UserPrestigeRepository userPrestigeRepository;
    private final UserPrestigeMapper userPrestigeMapper;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final CommentLikeRepository commentLikeRepository;

    public Optional<UserPrestige> getPrestigeByUserId(Long userId) {
        return userPrestigeRepository.findById(userId);
    }

    public Optional<UserPrestigeDTO> getPrestigeDTOByUserId(Long userId) {
        return userPrestigeRepository.findById(userId)
                .map(userPrestigeMapper::toDto);
    }

    @Transactional
    public void checkIfUserShouldReevaluatePrestige(Long userId) {
        System.out.println("Iniciando evaluación de prestigio para el usuario: " + userId);

        UserPrestige prestige = userPrestigeRepository.findById(userId)
            .orElseGet(() -> {
                System.out.println("No existe prestigio previo, creando uno nuevo...");
                return createInitialPrestige(userId);
            });

        int totalLikes = commentRepository.sumLikeCountByUserId(userId);
        int checkpoint = prestige.getLastLikeCheckpoint();
        int diff = Math.abs(totalLikes - checkpoint);

        System.out.printf("Likes actuales: %d, Checkpoint previo: %d, Diferencia: %d%n", totalLikes, checkpoint, diff);
        
        if (diff < 10) { //Cambiar a 1 para prueba
            System.out.println("No se requieren cambios, menos de 10 likes de diferencia.");
            return;
        }

        updatePrestigeStats(userId, prestige);

        short newRank = evaluateRank(prestige);
        short currentRank = prestige.getCurrentRank();

        System.out.printf("Rango actual: %d | Nuevo rango evaluado: %d%n", currentRank, newRank);

        // Detectar cambio de nivel
        if (newRank > currentRank) {
            prestige.setTrendDirection("U");
            prestige.setLastRankChange(LocalDateTime.now());

            System.out.println("Subida de rango detectada. Enviando notificación...");

            notificationService.notifyPrestigeLevelUp(prestige);
        } else if (newRank < currentRank) {
            prestige.setTrendDirection("D");
            prestige.setLastRankChange(LocalDateTime.now());
            System.out.println("Bajada de rango detectada.");
        } else {
            prestige.setTrendDirection("M");
            System.out.println("El rango se mantiene igual.");
        }

        // Actualizar estado
        prestige.setCurrentRank(newRank);
        prestige.setLastLikeCheckpoint(totalLikes);
        prestige.setUpdatedAt(LocalDateTime.now());

        userPrestigeRepository.save(prestige);

        System.out.println("Actualización de prestigio finalizada.");
    }


    @Transactional
    public UserPrestige createInitialPrestige(Long userId) {
        UserPrestige prestige = UserPrestige.builder()
            .userId(userId)
            .currentRank((short) 0)
            .weightedScore(BigDecimal.ZERO)
            .totalLikes(0)
            .commentsWith10(0)
            .commentsWith15(0)
            .commentsWith50(0)
            .lastRankChange(LocalDateTime.now())
            .trendDirection("M") // M = Mantenimiento
            .lastScoreEvaluated(BigDecimal.ZERO)
            .lastLikeCheckpoint(0)
            .updatedAt(LocalDateTime.now())
            .build();

        return userPrestigeRepository.save(prestige);
    }

    @Transactional
    public void updatePrestigeStats(Long userId, UserPrestige prestige) {
        // 1. Obtener todos los likes que ha recibido el usuario
        List<LocalDateTime> likeDates = commentLikeRepository.findAllLikeDatesForUser(userId);

        // 2. Calcular el score ponderado
        BigDecimal weightedScore = calculateWeightedScore(likeDates);

        // 3. Recontar datos importantes
        int totalLikes = likeDates.size();
        int commentsWith10 = commentRepository.countByUserIdWithMinLikes(userId, 10);
        int commentsWith15 = commentRepository.countByUserIdWithMinLikes(userId, 15);
        int commentsWith50 = commentRepository.countByUserIdWithMinLikes(userId, 50);

        // 4. Actualizar la entidad
        prestige.setTotalLikes(totalLikes);
        prestige.setCommentsWith10(commentsWith10);
        prestige.setCommentsWith15(commentsWith15);
        prestige.setCommentsWith50(commentsWith50);
        prestige.setWeightedScore(weightedScore);

        // Importante: actualizar el checkpoint
        prestige.setLastLikeCheckpoint(totalLikes);

        // Marcar como actualizado
        prestige.setUpdatedAt(LocalDateTime.now());

        userPrestigeRepository.save(prestige);
    }

    public short evaluateRank(UserPrestige p) {
        if (p.getTotalLikes() > 2000 && p.getCommentsWith50() >= 40) return 5;
        if (p.getTotalLikes() > 1000 && p.getCommentsWith50() >= 20) return 4;
        if (p.getTotalLikes() > 450 && p.getCommentsWith15() >= 20) return 3;
        if (p.getTotalLikes() > 100 && p.getCommentsWith10() >= 10) return 2;
        // if (p.getTotalLikes() > 0 && p.getCommentsWith10() >= 0) return 1; //Degradado para prueba
        if (p.getTotalLikes() > 25 && p.getCommentsWith10() >= 5) return 1;
        return 0;
    }

    public BigDecimal calculateWeightedScore(List<LocalDateTime> likeDates) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal score = BigDecimal.ZERO;

        for (LocalDateTime date : likeDates) {
            long days = ChronoUnit.DAYS.between(date, now);
            BigDecimal weight;

            if (days <= 7) {
                weight = BigDecimal.valueOf(1.25);
            } else if (days <= 30) {
                weight = BigDecimal.valueOf(1.00);
            } else if (days <= 60) {
                weight = BigDecimal.valueOf(0.75);
            } else if (days <= 180) {
                weight = BigDecimal.valueOf(0.50);
            } else {
                weight = BigDecimal.valueOf(0.25);
            }

            score = score.add(weight);
        }

        return score.setScale(2, RoundingMode.HALF_UP);
    }
}