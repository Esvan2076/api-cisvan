package com.cisvan.api.domain.trending;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.trending.trendingsnapshot.TrendingSnapshot;
import com.cisvan.api.domain.trending.trendingsnapshot.TrendingSnapshotRepository;
import com.cisvan.api.domain.trending.trendingvisit.TrendingVisit;
import com.cisvan.api.domain.trending.trendingvisit.TrendingVisitRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrendingService {
    
    private final TrendingRepository trendingRepository;
    private final TrendingVisitRepository trendingVisitRepository;
    private final TrendingSnapshotRepository trendingSnapshotRepository;

    public Optional<Trending> getTrendingById(Long trendingId) {
        return trendingRepository.findById(trendingId);
    }

    public Optional<Trending> getTrendingsByTitleId(String tconst) {
        return trendingRepository.findByContentId(tconst);
    }

    public List<Trending> getTopTrendingContents() {
        return trendingRepository.findTop20ByOrderByScoreDesc();
    }

    @Transactional
    public void registerVisitPoint(Long userId, String tconst) {
        LocalDate today = LocalDate.now();
        boolean alreadyVisited = trendingVisitRepository.existsByUserIdAndContentIdAndVisitDate(userId, tconst, today);

        if (alreadyVisited) {
            System.out.printf("Visita ignorada: usuario %d ya visitó %s hoy%n", userId, tconst);
            return;
        }

        // Registrar la visita
        TrendingVisit visit = TrendingVisit.builder()
            .userId(userId)
            .contentId(tconst)
            .visitDate(today)
            .build();
        trendingVisitRepository.save(visit);

        // Sumar el punto
        Trending trending = trendingRepository.findByContentId(tconst)
            .orElseGet(() -> Trending.builder()
                .contentId(tconst)
                .score(0)
                .historicalScore(0L)
                .build());

        trending.setScore(trending.getScore() + 1);
        trending.setHistoricalScore(trending.getHistoricalScore() + 1);
        trendingRepository.save(trending);

        System.out.printf("[+1] punto por visita única para %s por usuario %d%n", tconst, userId);
    }

    @Transactional
    public void registerReviewPoints(Long userId, String tconst, boolean hasFieldRatings) {
        int totalPoints = 2 /* base rating */ + 5 /* review */;

        if (hasFieldRatings) {
            totalPoints += 3; // rating en campos específicos
        }

        Trending trending = trendingRepository.findByContentId(tconst)
            .orElseGet(() -> Trending.builder()
                .contentId(tconst)
                .score(0)
                .historicalScore(0L)
                .build());

        trending.setScore(trending.getScore() + totalPoints);
        trending.setHistoricalScore(trending.getHistoricalScore() + totalPoints);
        trendingRepository.save(trending);

        System.out.printf("Se asignaron %d puntos de tendencia a %s por review del usuario %d%n", totalPoints, tconst, userId);
    }

    @Transactional
    public void registerCommentPoints(Long userId, String tconst) {
        if (tconst == null) {
            // No aplica tendencia si no hay contenido asociado
            return;
        }

        int points = 4;

        Trending trending = trendingRepository.findByContentId(tconst)
            .orElseGet(() -> Trending.builder()
                .contentId(tconst)
                .score(0)
                .historicalScore(0L)
                .build());

        trending.setScore(trending.getScore() + points);
        trending.setHistoricalScore(trending.getHistoricalScore() + points);
        trendingRepository.save(trending);

        System.out.printf("Tendencia: +%d puntos a %s por comentario de usuario %d%n", points, tconst, userId);
    }

    @Transactional
    public void registerReplyPoints(Long userId, String tconst) {
        int points = 5;

        Trending trending = trendingRepository.findByContentId(tconst)
            .orElseGet(() -> Trending.builder()
                .contentId(tconst)
                .score(0)
                .historicalScore(0L)
                .build());

        trending.setScore(trending.getScore() + points);
        trending.setHistoricalScore(trending.getHistoricalScore() + points);
        trendingRepository.save(trending);

        System.out.printf("Tendencia: +%d puntos a %s por respuesta de usuario %d%n", points, tconst, userId);
    }

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void processDailyTrending() {
        System.out.println("Iniciando evaluación de tendencias...");

        // List<Trending> trendingCandidates = trendingRepository.findAllByScoreGreaterThan(100);

        List<Trending> trendingCandidates = trendingRepository.findAllByScoreGreaterThan(0);

        int totalCandidates = trendingCandidates.size();

        System.out.println("Total de contenidos candidatos con score > 0: " + totalCandidates);

        List<Trending> top100 = trendingCandidates.stream()
            .sorted(Comparator.comparingInt(Trending::getScore).reversed())
            .limit(100)
            .collect(Collectors.toList());

        int needed = 100 - top100.size();
        Set<String> alreadyInTop = top100.stream().map(Trending::getContentId).collect(Collectors.toSet());

        if (needed > 0) {
            System.out.println("Faltan " + needed + " entradas para completar el top 100. Se agregarán por votos.");

            // Obtener títulos por votos
            List<Object[]> fallbackTitles = trendingRepository.findFallbackTitlesForTrending(alreadyInTop, needed);

            for (Object[] row : fallbackTitles) {
                String tconst = (String) row[0];
                Integer score = (Integer) row[1]; // puede ser 0 por defecto
                top100.add(
                    Trending.builder().contentId(tconst).score(score != null ? score : 0).build()
                );
            }
        }

        // Guardar snapshot
        List<TrendingSnapshot> snapshots = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < top100.size(); i++) {
            Trending t = top100.get(i);
            snapshots.add(TrendingSnapshot.builder()
                .contentId(t.getContentId())
                .score(t.getScore())
                .snapshotDate(today)
                .rank(i + 1)
                .build());
        }

        // 1. Elimino todos los previos 
        trendingSnapshotRepository.deleteAll();
        System.out.println("Snapshots anteriores del día eliminados.");

        // 2. Guardar los nuevos snapshots
        trendingSnapshotRepository.saveAll(snapshots);
        System.out.println("Snapshots guardados: " + snapshots.size());

        // Resetear scores
        trendingRepository.incrementHistoricalAndResetScores();
        System.out.println("Proceso de tendencia finalizado con éxito.");

        System.out.println("Ejecutando procedimiento de recomendación final...");
        trendingRepository.generateFinalRecommendations();
        System.out.println("Procedimiento ejecutado correctamente.");
    }
}
