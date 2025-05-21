package com.cisvan.api.domain.recommendation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.comment.CommentRepository;
import com.cisvan.api.domain.crew.Crew;
import com.cisvan.api.domain.crew.CrewRepository;
import com.cisvan.api.domain.principal.Principal;
import com.cisvan.api.domain.principal.repos.PrincipalRepository;
import com.cisvan.api.domain.recommendation.userrecomendationitem.UserRecommendationItem;
import com.cisvan.api.domain.recommendation.userrecommendationset.UserRecommendationSet;
import com.cisvan.api.domain.recommendation.userrecommendationset.UserRecommendationSetRepository;
import com.cisvan.api.domain.recommendation.usersecondary.UserSecondaryRecommendation;
import com.cisvan.api.domain.recommendation.usersecondary.UserSecondaryRecommendationRepository;
import com.cisvan.api.domain.reviews.dtos.TitleRecommendationDTO;
import com.cisvan.api.domain.reviews.dtos.TitleOrderingDTO;
import com.cisvan.api.domain.reviews.dtos.TitleReviewDTO;
import com.cisvan.api.domain.reviews.dtos.TitleReviewDTO.ActorScoreDTO;
import com.cisvan.api.domain.reviews.dtos.TitleReviewDTO.DirectorScoreDTO;
import com.cisvan.api.domain.reviews.dtos.TitleReviewDTO.GenreScoreDTO;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.RecommendedTitleDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.titlerating.TitleRating;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final TitleRepository titleRepository;
    private final TitleService titleService;
    private final CrewRepository crewRepository;
    private final CommentRepository commentRepository;
    private final PrincipalRepository principalRepository;
    private final TitleRatingRepository titleRatingRepository;
    private final UserRecommendationSetRepository userRecommendationSetRepository;
    private final UserSecondaryRecommendationRepository userSecondaryRecommendationRepository;

    final BigDecimal actorMultiplier = BigDecimal.valueOf(9.9);

    // Número de recomendaciones a generar
    private static final int RECOMMENDATION_LIMIT = 30;

    // Dentro de tu clase de servicio, define esta constante o un campo:
    private static final BigDecimal DEFAULT_ACTOR_MULTIPLIER_LOW_SCORE = new BigDecimal("4.0");

    private enum RecommendationCategory {
        DIRECTOR, ACTOR, GENRE
    }

    public List<RecommendedTitleDTO> getGenreBasedRecommendations(String tconst) {
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty())
            return List.of();

        Title baseTitle = titleOpt.get();
        String genresArrayLiteral = toPostgresArray(baseTitle.getGenres());

        // 1. Exact genre match
        List<Object[]> exactRows = titleRepository.findTop10ByGenresContainingAll(genresArrayLiteral, tconst);

        Set<String> seenTconsts = new HashSet<>();
        List<RecommendedTitleDTO> results = new ArrayList<>(mapToDtoList(exactRows, seenTconsts));

        // 2. Partial genre match (if needed)
        int remaining = 10 - results.size();
        if (remaining > 0) {
            seenTconsts.add(tconst);
            List<Object[]> partialRows = titleRepository.findTopByAnyMatchingGenreExcluding(
                    genresArrayLiteral,
                    new ArrayList<>(seenTconsts),
                    remaining);
            results.addAll(mapToDtoList(partialRows, seenTconsts));
        }

        return results;
    }

    private String toPostgresArray(List<String> genres) {
        return "{" + String.join(",", genres) + "}";
    }

    private List<RecommendedTitleDTO> mapToDtoList(List<Object[]> rows, Set<String> seenTconsts) {
        return rows.stream()
                .map((Object[] row) -> {
                    String tconst = (String) row[0];
                    seenTconsts.add(tconst);

                    String rawPosterUrl = (String) row[7];
                    String finalPosterUrl = (rawPosterUrl == null || rawPosterUrl.isEmpty())
                            ? "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg"
                            : (rawPosterUrl.startsWith("http") ? rawPosterUrl
                                    : "https://m.media-amazon.com/images/M/" + rawPosterUrl + "._V1_SX300.jpg");

                    return RecommendedTitleDTO.builder()
                            .tconst(tconst)
                            .titleType((String) row[1])
                            .primaryTitle((String) row[2])
                            .startYear(row[3] != null ? ((Number) row[3]).shortValue() : null)
                            .endYear(row[4] != null ? ((Number) row[4]).shortValue() : null)
                            .posterUrl(finalPosterUrl) // ✅ nuevo campo
                            .titleRating(TitleRating.builder()
                                    .tconst(tconst)
                                    .averageRating((BigDecimal) row[5])
                                    .numVotes((Integer) row[6])
                                    .build())
                            .build();
                }).toList();
    }

    @Async
    public void triggerRecommendationAlgorithm(TitleReviewDTO reviewDTO, Long userId) {
        try {
            // 1. Generar recomendaciones primarias (tu lógica existente)
            // 1. Generar recomendaciones primarias (tu lógica existente)
            List<TitleRecommendationDTO> primaryRecommendations = generateRecommendations(reviewDTO, userId);

            if (primaryRecommendations == null || primaryRecommendations.isEmpty()) {
                System.out.println("No se generaron recomendaciones primarias para el usuario: " + userId
                        + " basadas en la reseña de " + reviewDTO.getTconst());
                return;
            }

            // Imprimir las recomendaciones PRIMARIAS generadas
            System.out.println("Recomendaciones PRIMARIAS generadas para usuario " + userId + " (reseña de "
                    + reviewDTO.getTconst() + "):");
            primaryRecommendations.forEach(rec -> {
                // Aquí completamos el System.out.println
                System.out.println(
                        "  Título: " + rec.getTitle() +
                        " (tconst: " + rec.getTconst() +
                        ", rating Película: " + rec.getRating() + // Calificación del título recomendado
                        ") - MatchCount: " + rec.getMatchCount() +
                        ", MatchScore: " + rec.getMatchScore());
            });

            // 2. Crear y guardar el NUEVO conjunto de recomendaciones primarias
            UserRecommendationSet newPrimarySet = UserRecommendationSet.builder()
                    .userId(userId)
                    .generatedAt(LocalDateTime.now())
                    .build();
            short currentRank = 1;
            for (TitleRecommendationDTO dto : primaryRecommendations) {
                UserRecommendationItem item = UserRecommendationItem.builder()
                        .tconst(dto.getTconst())
                        .rank(currentRank++)
                        .matchCount(dto.getMatchCount())
                        .matchScore(dto.getMatchScore())
                        .titleRatingAtRecommendation(dto.getRating())
                        .build();
                newPrimarySet.addItem(item);
            }
            userRecommendationSetRepository.save(newPrimarySet);
            System.out.println("Recomendaciones primarias guardadas para usuario: " + userId + ", Set Primario ID: "
                    + newPrimarySet.getId());

            // 3. Contar el número TOTAL de UserRecommendationSet para este usuario
            // (incluyendo el actual)
            long totalPrimarySetsForUser = userRecommendationSetRepository.countByUserId(userId);

            if (totalPrimarySetsForUser == 1) {
                // RQNF 79: Única reseña/set primario. Copiar las primarias (hasta p_top_n) a
                // secundarias.
                System.out.println(
                        "Usuario " + userId + " tiene solo un set primario. Copiando primarias a secundarias.");
                List<UserSecondaryRecommendation> secondaryRecsToSave = new ArrayList<>();
                short secondaryRank = 1;
                LocalDateTime now = LocalDateTime.now();

                // Usamos la lista 'primaryRecommendations' (DTOs) que ya tenemos y está
                // ordenada
                for (TitleRecommendationDTO dto : primaryRecommendations) {
                    if (secondaryRank > 30) { // Limitar a 30 (o el p_top_n que uses)
                        break;
                    }
                    UserSecondaryRecommendation secRec = UserSecondaryRecommendation.builder()
                            .userId(userId)
                            .tconst(dto.getTconst())
                            .rankForUser(secondaryRank++)
                            .crossMatchCountForUser(1) // Solo este set está involucrado
                            .aggregatedMatchScoreForUser(dto.getMatchScore()) // Score del único set primario
                            .lastCalculatedAt(now)
                            .build();
                    secondaryRecsToSave.add(secRec);
                }
                if (!secondaryRecsToSave.isEmpty()) {
                    userSecondaryRecommendationRepository.saveAll(secondaryRecsToSave);
                }
                System.out.println(secondaryRecsToSave.size()
                        + " recomendaciones secundarias guardadas (directas desde primarias) para usuario: " + userId);

            } else if (totalPrimarySetsForUser > 1) {
                // Múltiples sets primarios. Ejecutar el algoritmo de agregación en la BD.
                System.out.println("Usuario " + userId + " tiene " + totalPrimarySetsForUser
                        + " sets primarios. Ejecutando agregación en BD para secundarias.");
                userSecondaryRecommendationRepository.executeUpdateUserSecondaryRecommendations(userId, 30);
                System.out.println(
                        "Recomendaciones secundarias actualizadas vía agregación en BD para usuario: " + userId);
            } else {

                System.out.println("Advertencia: totalPrimarySetsForUser es " + totalPrimarySetsForUser +
                        " para el usuario " + userId + ". No se actualizan secundarias por este camino.");
            }

            // 5. Opcional pero recomendado: Imprimir las recomendaciones secundarias
            // generadas/actualizadas
            List<UserSecondaryRecommendation> finalSecondaryRecommendations = userSecondaryRecommendationRepository
                    .findByUserIdOrderByRankForUserAsc(userId);

            if (finalSecondaryRecommendations.isEmpty()) {
                System.out.println("No se encontraron recomendaciones secundarias finales para el usuario: " + userId);
            } else {
                System.out.println("Recomendaciones SECUNDARIAS FINALES para el usuario " + userId + ":");
                finalSecondaryRecommendations.forEach(secRec -> {
                    // Para obtener el nombre del título, necesitarías el TitleRepository
                    // String titleName = titleRepository.findById(secRec.getTconst())
                    // .map(Title::getPrimaryTitle)
                    // .orElse("Título Desconocido");
                    System.out.println(
                            "  Rank: " + secRec.getRankForUser() +
                    // ", Nombre: " + titleName + // Descomenta si implementas la búsqueda del
                    // nombre
                                    " (tconst: " + secRec.getTconst() + ")" +
                                    ", CrossMatchCount: " + secRec.getCrossMatchCountForUser() +
                                    ", AggregatedScore: " + secRec.getAggregatedMatchScoreForUser());
                });
            }
        } catch (Exception e) {
            System.err.println("Error al generar recomendaciones: " + e.getMessage());
        }
    }

    public RecommendationScoresResult calculateReviewFieldScores(TitleReviewDTO reviewDTO) {
        BigDecimal score = reviewDTO.getScore();
        String tconst = reviewDTO.getTconst();

        if (score.compareTo(BigDecimal.valueOf(7)) < 0 && noSpecificRatings(reviewDTO)) {
            return RecommendationScoresResult.empty();
        }

        List<Pair<String, BigDecimal>> includeDirectors = new ArrayList<>();
        List<Pair<String, BigDecimal>> includeActors = new ArrayList<>();
        List<Pair<String, BigDecimal>> includeGenres = new ArrayList<>();
        List<Pair<String, BigDecimal>> excludeDirectors = new ArrayList<>();
        List<Pair<String, BigDecimal>> excludeActors = new ArrayList<>();
        List<Pair<String, BigDecimal>> excludeGenres = new ArrayList<>();

        // Obtener datos generales y sus baseWeights correspondientes
        Optional<Crew> crewOpt = crewRepository.findById(tconst);
        List<String> directorsGeneralIds = crewOpt
                .map(c -> c.getDirectors().stream().limit(3).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        List<Pair<String, BigDecimal>> directorsAllMovieItemsWithBaseWeight = directorsGeneralIds.stream()
                .map(d -> Pair.of(d, BigDecimal.valueOf(10)))
                .collect(Collectors.toList());

        List<Pair<Principal, BigDecimal>> actorsGeneralWithPositionalMultiplier = getUniqueActorsWithMultiplier(
                principalRepository.findActorsByTconst(tconst), score);
        List<Pair<String, BigDecimal>> actorsAllMovieItemsWithBaseWeight = actorsGeneralWithPositionalMultiplier
                .stream()
                .map(pair -> Pair.of(pair.getLeft().getNconst(), pair.getRight()))
                .collect(Collectors.toList());

        List<String> genresGeneralNames = titleRepository.findById(tconst).map(Title::getGenres)
                .orElse(Collections.emptyList());
        List<Pair<String, BigDecimal>> genresAllMovieItemsWithBaseWeight = genresGeneralNames.stream()
                .map(g -> Pair.of(g, BigDecimal.valueOf(9)))
                .collect(Collectors.toList());

        if (score.compareTo(BigDecimal.valueOf(7)) < 0) {
            // Calificación general NEGATIVA pero HAY calificaciones específicas:
            // SOLO procesar campos calificados explícitamente.
            processExplicitItemRatingsOnly(
                    reviewDTO.getDirectors(),
                    directorsAllMovieItemsWithBaseWeight,
                    includeDirectors, excludeDirectors,
                    BigDecimal.valueOf(2), BigDecimal.valueOf(0.20));
            processExplicitItemRatingsOnly(
                    reviewDTO.getActors(),
                    actorsAllMovieItemsWithBaseWeight,
                    includeActors, excludeActors,
                    BigDecimal.valueOf(1.9), BigDecimal.valueOf(0.19));
            processExplicitItemRatingsOnly(
                    reviewDTO.getGenres(),
                    genresAllMovieItemsWithBaseWeight,
                    includeGenres, excludeGenres,
                    BigDecimal.valueOf(1.75), BigDecimal.valueOf(0.7));
        } else {
            processSpecificFields(
                    reviewDTO.getDirectors(),
                    includeDirectors, excludeDirectors,
                    directorsAllMovieItemsWithBaseWeight,
                    BigDecimal.valueOf(2), BigDecimal.valueOf(0.20),
                    score);
            processSpecificFields(
                    reviewDTO.getGenres(),
                    includeGenres, excludeGenres,
                    genresAllMovieItemsWithBaseWeight,
                    BigDecimal.valueOf(1.75), BigDecimal.valueOf(0.7),
                    score);
            processSpecificFields(
                    reviewDTO.getActors(),
                    includeActors, excludeActors,
                    actorsAllMovieItemsWithBaseWeight,
                    BigDecimal.valueOf(1.9), BigDecimal.valueOf(0.19),
                    score);
        }

        includeDirectors.sort((a, b) -> b.getRight().compareTo(a.getRight()));
        includeActors.sort((a, b) -> b.getRight().compareTo(a.getRight()));
        includeGenres.sort((a, b) -> b.getRight().compareTo(a.getRight()));

        excludeDirectors.sort(Comparator.comparing(Pair::getRight));
        excludeActors.sort(Comparator.comparing(Pair::getRight));
        excludeGenres.sort(Comparator.comparing(Pair::getRight));

        return new RecommendationScoresResult(
                includeDirectors, includeActors, includeGenres,
                excludeDirectors, excludeActors, excludeGenres);
    }

    // DTO simple para resultados
    public static class RecommendationScoresResult {
        public final List<Pair<String, BigDecimal>> includeDirectors;
        public final List<Pair<String, BigDecimal>> includeActors;
        public final List<Pair<String, BigDecimal>> includeGenres;
        public final List<Pair<String, BigDecimal>> excludeDirectors;
        public final List<Pair<String, BigDecimal>> excludeActors;
        public final List<Pair<String, BigDecimal>> excludeGenres;

        public RecommendationScoresResult(
                List<Pair<String, BigDecimal>> includeDirectors,
                List<Pair<String, BigDecimal>> includeActors,
                List<Pair<String, BigDecimal>> includeGenres,
                List<Pair<String, BigDecimal>> excludeDirectors,
                List<Pair<String, BigDecimal>> excludeActors,
                List<Pair<String, BigDecimal>> excludeGenres) {
            this.includeDirectors = includeDirectors;
            this.includeActors = includeActors;
            this.includeGenres = includeGenres;
            this.excludeDirectors = excludeDirectors;
            this.excludeActors = excludeActors;
            this.excludeGenres = excludeGenres;
        }

        public static RecommendationScoresResult empty() {
            return new RecommendationScoresResult(
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    // Verifica si no hay campos específicos calificados
    private boolean noSpecificRatings(TitleReviewDTO reviewDTO) {
        return (reviewDTO.getDirectors() == null || reviewDTO.getDirectors().isEmpty()) &&
                (reviewDTO.getActors() == null || reviewDTO.getActors().isEmpty()) &&
                (reviewDTO.getGenres() == null || reviewDTO.getGenres().isEmpty());
    }

    /**
     * Procesa SOLAMENTE los ítems que fueron explícitamente calificados por el
     * usuario.
     * Se utiliza cuando la calificación general de la película es baja (<7),
     * y solo se considera el impacto de estas calificaciones específicas.
     * La lógica interna para calcular el score de un ítem específico es idéntica
     * a cómo se hace en processSpecificFields.
     */
    private <T> void processExplicitItemRatingsOnly(
            List<T> specificRatedItemsList,
            List<Pair<String, BigDecimal>> allMovieItemsWithBaseWeight,
            List<Pair<String, BigDecimal>> includeList,
            List<Pair<String, BigDecimal>> excludeList,
            BigDecimal boostFactorForHighSpecificRating, // Factor si la calificación específica es >= 8
            BigDecimal penaltyFactorForLowSpecificRating // Factor si la calificación específica es <= 6
    ) {
        if (specificRatedItemsList == null || specificRatedItemsList.isEmpty()) {
            return;
        }

        for (T specificItem : specificRatedItemsList) {
            String identifier = getFieldNconst(specificItem);
            BigDecimal specificScoreFromUser = getFieldScore(specificItem);

            // Encontrar el baseWeight para este ítem específico.
            // Para actores, allMovieItemsWithBaseWeight contendrá su multiplicador
            // posicional.
            BigDecimal baseWeightForItem = allMovieItemsWithBaseWeight.stream()
                    .filter(pair -> pair.getLeft().equals(identifier))
                    .map(Pair::getRight)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontró baseWeight para el ítem específico: " + identifier +
                                    " en allMovieItemsWithBaseWeight (processExplicitItemRatingsOnly)"));

            // "La normal" aplicada a la calificación específica: (calificación específica
            // del usuario) * (multiplicador base de la categoría)
            BigDecimal coreValue = specificScoreFromUser.multiply(baseWeightForItem);
            BigDecimal finalValue = coreValue; // Inicializar finalValue con coreValue

            // "Luego el multiplicador específico" (ajuste adicional si la calificación
            // específica es extrema)
            if (specificScoreFromUser.compareTo(BigDecimal.valueOf(7)) >= 0) { // Calificación específica positiva
                if (specificScoreFromUser.compareTo(BigDecimal.valueOf(8)) >= 0) { // Muy positiva
                    finalValue = coreValue.multiply(boostFactorForHighSpecificRating);
                }
                // Si es 7 <= specificScoreFromUser < 8, finalValue sigue siendo coreValue.
                includeList.add(Pair.of(identifier, finalValue));
            } else { // Calificación específica negativa (specificScoreFromUser < 7)
                if (specificScoreFromUser.compareTo(BigDecimal.valueOf(6)) <= 0) { // Muy negativa
                    finalValue = coreValue.multiply(penaltyFactorForLowSpecificRating);
                }
                // Si es 6 < specificScoreFromUser < 7, finalValue sigue siendo coreValue.
                // Como la calificación específica es < 7, va a la lista de exclusión.
                excludeList.add(Pair.of(identifier, finalValue));
            }
        }
        // Importante: Este método NO procesa ítems generales (no calificados
        // específicamente).
    }

    // Los otros métodos de utilería y getUniqueActorsWithMultiplier se quedan
    private <T> void processSpecificFields(
            List<T> specificRatedItemsList,
            List<Pair<String, BigDecimal>> includeList,
            List<Pair<String, BigDecimal>> excludeList,
            List<Pair<String, BigDecimal>> allMovieItemsWithBaseWeight, // (identificador, baseWeightDelItem [ej.
                                                                        // posicional para actor])
            BigDecimal boostFactorForHighSpecificRating,
            BigDecimal penaltyFactorForLowSpecificRating,
            BigDecimal titleOverallScore // Calificación general de la película
    ) {
        Set<String> processedIdentifiers = new HashSet<>();

        // 1. Ítems calificados específicamente
        if (specificRatedItemsList != null) {
            for (T specificItem : specificRatedItemsList) {
                String identifier = getFieldNconst(specificItem);
                BigDecimal specificScoreFromUser = getFieldScore(specificItem); // Calificación específica (1-10)
                processedIdentifiers.add(identifier);

                BigDecimal baseWeightForItem = allMovieItemsWithBaseWeight.stream()
                        .filter(pair -> pair.getLeft().equals(identifier))
                        .map(Pair::getRight)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No se encontró baseWeight para: " + identifier));

                // --- INICIO DE NUEVA LÓGICA "BASE + ADICIONAL" ---
                // Componente Base: (Calificación General Película * Multiplicador Normal del
                // Ítem)
                BigDecimal componentBase = titleOverallScore.multiply(baseWeightForItem);

                // Componente Específico Adicional: (Calificación Específica Usuario *
                // Multiplicador de Categoría Específico)
                BigDecimal categorySpecificMultiplier;
                if (specificItem instanceof ActorScoreDTO) {
                    categorySpecificMultiplier = new BigDecimal("9.9"); // Multiplicador específico para actores
                } else if (specificItem instanceof DirectorScoreDTO) {
                    categorySpecificMultiplier = new BigDecimal("10.0"); // Multiplicador específico para directores
                } else if (specificItem instanceof GenreScoreDTO) {
                    categorySpecificMultiplier = new BigDecimal("9.0"); // Multiplicador específico para géneros
                } else {
                    categorySpecificMultiplier = BigDecimal.ONE; // Fallback
                }

                BigDecimal componentSpecific = specificScoreFromUser.multiply(categorySpecificMultiplier);

                // Ajustar el componentSpecific si la calificación es extrema
                BigDecimal adjustedComponentSpecific = componentSpecific;
                if (specificScoreFromUser.compareTo(BigDecimal.valueOf(7)) >= 0) {
                    if (specificScoreFromUser.compareTo(BigDecimal.valueOf(8)) >= 0) { // Muy positiva
                        adjustedComponentSpecific = componentSpecific.multiply(boostFactorForHighSpecificRating);
                    }
                    // Si es 7.x, adjustedComponentSpecific sigue siendo componentSpecific
                } else { // < 7 (negativa)
                    if (specificScoreFromUser.compareTo(BigDecimal.valueOf(6)) <= 0) { // Muy negativa
                        adjustedComponentSpecific = componentSpecific.multiply(penaltyFactorForLowSpecificRating);
                    }
                    // Si es 6.x, adjustedComponentSpecific sigue siendo componentSpecific
                }

                BigDecimal finalValue = componentBase.add(adjustedComponentSpecific);
                // --- FIN DE NUEVA LÓGICA "BASE + ADICIONAL" ---

                // Decidir si va a includeList o excludeList basado en la calificación
                // específica del usuario
                if (specificScoreFromUser.compareTo(BigDecimal.valueOf(7)) >= 0) {
                    includeList.add(Pair.of(identifier, finalValue));
                } else {
                    excludeList.add(Pair.of(identifier, finalValue));
                }
            }
        }

        // 2. Ítems generales (no calificados específicamente) - Lógica sin cambios
        for (Pair<String, BigDecimal> generalItemPair : allMovieItemsWithBaseWeight) {
            String identifier = generalItemPair.getLeft();
            if (!processedIdentifiers.contains(identifier)) {
                BigDecimal baseWeightForItem = generalItemPair.getRight();
                BigDecimal finalValue = titleOverallScore.multiply(baseWeightForItem); // (pelicula * multiplicador)
                includeList.add(Pair.of(identifier, finalValue));
            }
        }
    }

    private BigDecimal getFieldScore(Object field) {
        if (field instanceof DirectorScoreDTO)
            return ((DirectorScoreDTO) field).getScore();
        if (field instanceof ActorScoreDTO)
            return ((ActorScoreDTO) field).getScore();
        if (field instanceof GenreScoreDTO)
            return ((GenreScoreDTO) field).getScore();
        return BigDecimal.ZERO;
    }

    private String getFieldNconst(Object field) {
        if (field instanceof DirectorScoreDTO)
            return ((DirectorScoreDTO) field).getNconst();
        if (field instanceof ActorScoreDTO)
            return ((ActorScoreDTO) field).getNconst();
        if (field instanceof GenreScoreDTO)
            return ((GenreScoreDTO) field).getGenre();
        return "";
    }

    // En tu método getUniqueActorsWithMultiplier:
    private List<Pair<Principal, BigDecimal>> getUniqueActorsWithMultiplier(List<Principal> actors, BigDecimal score) {
        // ... (código existente para uniqueActorsMap y uniqueActors.sort) ...
        Map<String, Principal> uniqueActorsMap = new HashMap<>();
        if (actors == null)
            actors = Collections.emptyList(); // Protección contra nulos

        for (Principal actor : actors) {
            // Añadir validación para datos de actor potencialmente nulos
            if (actor == null || actor.getNconst() == null || actor.getId() == null
                    || actor.getId().getOrdering() == null) {
                // Opcional: loggear una advertencia
                // System.err.println("Advertencia: Se encontró un Principal o sus campos
                // críticos nulos, se omitirá.");
                continue;
            }
            String nconst = actor.getNconst();
            Short ordering = actor.getId().getOrdering();

            if (uniqueActorsMap.containsKey(nconst)) {
                Principal existing = uniqueActorsMap.get(nconst);
                if (ordering < existing.getId().getOrdering()) {
                    uniqueActorsMap.put(nconst, actor);
                }
            } else {
                uniqueActorsMap.put(nconst, actor);
            }
        }

        List<Principal> uniqueActors = new ArrayList<>(uniqueActorsMap.values());
        uniqueActors.sort(Comparator.comparing(principal -> principal.getId().getOrdering()));

        List<Pair<Principal, BigDecimal>> orderedActorsWithMultiplier = new ArrayList<>();
        int position = 1;

        for (Principal actor : uniqueActors) {
            BigDecimal multiplier;

            if (score.compareTo(BigDecimal.valueOf(7)) >= 0) {
                switch (position) {
                    case 1:
                        multiplier = BigDecimal.valueOf(8);
                        break;
                    case 2:
                        multiplier = BigDecimal.valueOf(7.5);
                        break;
                    case 3:
                        multiplier = BigDecimal.valueOf(7);
                        break;
                    case 4:
                        multiplier = BigDecimal.valueOf(6.5);
                        break;
                    case 5:
                        multiplier = BigDecimal.valueOf(6);
                        break;
                    case 6:
                        multiplier = BigDecimal.valueOf(5);
                        break;
                    default:
                        multiplier = BigDecimal.valueOf(4);
                        break;
                }
            } else {
                // multiplier = actorMultiplier; // Línea original problemática
                multiplier = DEFAULT_ACTOR_MULTIPLIER_LOW_SCORE; // Usar la constante definida
            }

            orderedActorsWithMultiplier.add(Pair.of(actor, multiplier));
            position++;
        }
        return orderedActorsWithMultiplier;
    }

    public List<TitleRecommendationDTO> generateRecommendations(TitleReviewDTO reviewDTO, Long userId) {
        // Calcular los puntajes de la reseña
        RecommendationScoresResult scores = calculateReviewFieldScores(reviewDTO);

        System.out.println("Directores incluidos: " + scores.includeDirectors);
        System.out.println("Actores incluidos: " + scores.includeActors);
        System.out.println("Géneros incluidos: " + scores.includeGenres);

        System.out.println("Directores excluidos: " + scores.excludeDirectors);
        System.out.println("Actores excluidos: " + scores.excludeActors);
        System.out.println("Géneros excluidos: " + scores.excludeGenres);

        // Obtener los títulos ya vistos por el usuario (para excluirlos)
        List<String> viewedTitles = commentRepository.findTconstsByUserId(userId);

        // Si el usuario calificó mal el título y no proporcionó calificaciones
        // específicas
        if (reviewDTO.getScore().compareTo(BigDecimal.valueOf(7)) < 0 &&
                (scores.includeDirectors.isEmpty() && scores.includeActors.isEmpty()
                        && scores.includeGenres.isEmpty())) {
            return getTopRatedTitles(viewedTitles, RECOMMENDATION_LIMIT);
        }

        // Obtener recomendaciones basadas en los puntajes calculados
        List<TitleRecommendationDTO> recommendations = findRecommendationsByScores(scores, viewedTitles);

        // Aplicar penalizaciones basadas en elementos excluidos
        applyExclusionPenalties(recommendations, scores);

        // Ordenar las recomendaciones por: coincidencia > score > calificación
        sortRecommendations(recommendations);

        // Limitar el número de recomendaciones
        if (recommendations.size() > RECOMMENDATION_LIMIT) {
            recommendations = recommendations.subList(0, RECOMMENDATION_LIMIT);
        }

        // No hay nada
        // Si no se encontraron recomendaciones basadas en preferencias, recomendar los
        // mejores títulos
        if (recommendations.isEmpty()) {
            return getTopRatedTitles(viewedTitles, RECOMMENDATION_LIMIT);
        }

        return recommendations;
    }

    /**
     * Encuentra recomendaciones basadas en los puntajes calculados
     */
    private List<TitleRecommendationDTO> findRecommendationsByScores(
            RecommendationScoresResult scores, List<String> viewedTitles) {

        Map<String, TitleRecommendationDTO> allRecommendations = new HashMap<>();
        List<CategoryPriority> categoryPriorities = new ArrayList<>();

        // 1. Recolectar la información de cada categoría y su puntaje más alto
        // Las listas en 'scores' (includeDirectors, etc.) ya vienen ordenadas por
        // puntaje descendente
        // desde calculateReviewFieldScores.
        if (scores.includeDirectors != null && !scores.includeDirectors.isEmpty()) {
            categoryPriorities.add(new CategoryPriority(
                    RecommendationCategory.DIRECTOR,
                    scores.includeDirectors.get(0).getRight(), // El score del primer elemento es el más alto
                    scores.includeDirectors));
        }
        if (scores.includeActors != null && !scores.includeActors.isEmpty()) {
            categoryPriorities.add(new CategoryPriority(
                    RecommendationCategory.ACTOR,
                    scores.includeActors.get(0).getRight(),
                    scores.includeActors));
        }
        if (scores.includeGenres != null && !scores.includeGenres.isEmpty()) {
            categoryPriorities.add(new CategoryPriority(
                    RecommendationCategory.GENRE,
                    scores.includeGenres.get(0).getRight(),
                    scores.includeGenres));
        }

        // 2. Ordenar las categorías para que la que tiene el ítem con el mayor puntaje
        // global se procese primero
        Collections.sort(categoryPriorities);

        // 3. Procesar las categorías en el orden de prioridad determinado
        for (CategoryPriority prioritizedCategory : categoryPriorities) {
            switch (prioritizedCategory.category) {
                case RecommendationCategory.DIRECTOR:
                    // La lista prioritizedCategory.allScoresForCategory es scores.includeDirectors
                    List<Pair<String, BigDecimal>> topDirectors = getTopElements(
                            prioritizedCategory.allScoresForCategory, 5);
                    Map<String, TitleRecommendationDTO> directorRecommendations = findTitlesByDirectors(topDirectors,
                            viewedTitles);
                    mergeRecommendations(allRecommendations, directorRecommendations);
                    break;
                case RecommendationCategory.ACTOR:
                    // La lista prioritizedCategory.allScoresForCategory es scores.includeActors
                    List<Pair<String, BigDecimal>> topActors = getTopElements(prioritizedCategory.allScoresForCategory,
                            5);
                    Map<String, TitleRecommendationDTO> actorRecommendations = findTitlesByActors(topActors,
                            viewedTitles);
                    mergeRecommendations(allRecommendations, actorRecommendations);
                    break;
                case RecommendationCategory.GENRE:
                    // La lista prioritizedCategory.allScoresForCategory es scores.includeGenres
                    List<Pair<String, BigDecimal>> topGenres = getTopElements(prioritizedCategory.allScoresForCategory,
                            3);
                    Map<String, TitleRecommendationDTO> genreRecommendations = findTitlesByGenres(topGenres,
                            viewedTitles);
                    mergeRecommendations(allRecommendations, genreRecommendations);
                    break;
            }
        }

        // Si categoryPriorities está vacía (ninguna categoría tiene elementos en
        // 'include'),
        // el bucle no se ejecutará y se devolverá una lista vacía, lo cual es correcto.

        return new ArrayList<>(allRecommendations.values());
    }

    /**
     * Obtiene los N elementos con mayor puntaje de una lista
     */
    private List<Pair<String, BigDecimal>> getTopElements(List<Pair<String, BigDecimal>> elements, int limit) {
        if (elements.size() <= limit) {
            return new ArrayList<>(elements);
        }
        return elements.subList(0, limit);
    }

    /**
     * Encuentra títulos por directores
     */
    private Map<String, TitleRecommendationDTO> findTitlesByDirectors(
            List<Pair<String, BigDecimal>> directors, List<String> viewedTitles) {

        Map<String, TitleRecommendationDTO> recommendations = new HashMap<>();

        for (Pair<String, BigDecimal> director : directors) {
            String nconst = director.getLeft();
            BigDecimal score = director.getRight();

            // Consultar títulos dirigidos por este director
            List<String> directedTitles = crewRepository.findTitlesByDirector(nconst);

            for (String tconst : directedTitles) {
                // Excluir títulos ya vistos y episodios de TV
                if (viewedTitles.contains(tconst) || isTvEpisode(tconst)) {
                    continue;
                }

                // Crear o actualizar la recomendación
                TitleRecommendationDTO recommendation = recommendations.getOrDefault(
                        tconst, createBasicRecommendation(tconst));

                // Actualizar coincidencias y puntuación
                recommendation.setMatchCount(recommendation.getMatchCount() + 1);
                recommendation.setMatchScore(recommendation.getMatchScore().add(score));

                // Almacenar o actualizar en el mapa
                recommendations.put(tconst, recommendation);
            }
        }

        return recommendations;
    }

    /**
     * Encuentra títulos por actores
     */
    private Map<String, TitleRecommendationDTO> findTitlesByActors(
            List<Pair<String, BigDecimal>> actors, List<String> viewedTitles) {

        Map<String, TitleRecommendationDTO> recommendations = new HashMap<>();

        for (Pair<String, BigDecimal> actor : actors) {
            String nconst = actor.getLeft();
            BigDecimal score = actor.getRight();

            // Consultar títulos en los que este actor ha participado
            List<Pair<String, Short>> actorTitles = getTitleOrderingPairs(nconst);

            for (Pair<String, Short> titleInfo : actorTitles) {
                String tconst = titleInfo.getLeft();
                Short ordering = titleInfo.getRight();

                // Excluir títulos ya vistos y episodios de TV
                if (viewedTitles.contains(tconst) || isTvEpisode(tconst)) {
                    continue;
                }

                // Calcular puntaje ajustado basado en la importancia del actor (ordering)
                BigDecimal adjustedScore = adjustScoreByOrdering(score, ordering);

                // Crear o actualizar la recomendación
                TitleRecommendationDTO recommendation = recommendations.getOrDefault(
                        tconst, createBasicRecommendation(tconst));

                // Actualizar coincidencias y puntuación
                recommendation.setMatchCount(recommendation.getMatchCount() + 1);
                recommendation.setMatchScore(recommendation.getMatchScore().add(adjustedScore));

                // Almacenar o actualizar en el mapa
                recommendations.put(tconst, recommendation);
            }
        }

        return recommendations;
    }

    public List<Pair<String, Short>> getTitleOrderingPairs(String nconst) {
        List<TitleOrderingDTO> dtoList = principalRepository.findTitlesAndOrderingByNconst(nconst);
        return dtoList.stream()
                .map(dto -> Pair.of(dto.getTconst(), dto.getOrdering()))
                .collect(Collectors.toList());
    }

    /**
     * Ajusta el puntaje basado en la importancia del actor (menor ordering = más
     * importante)
     */
    private BigDecimal adjustScoreByOrdering(BigDecimal score, Short ordering) {
        // Si el actor es protagonista (ordering 1-3), mantener o aumentar el puntaje
        if (ordering <= 3) {
            return score;
        }
        // Si es secundario (4-6), reducir ligeramente
        else if (ordering <= 6) {
            return score.multiply(BigDecimal.valueOf(0.85));
        }
        // Si es papel menor, reducir significativamente
        else {
            return score.multiply(BigDecimal.valueOf(0.6));
        }
    }

    /**
     * Encuentra títulos por géneros
     */
    private Map<String, TitleRecommendationDTO> findTitlesByGenres(
            List<Pair<String, BigDecimal>> genres, List<String> viewedTitles) {

        Map<String, TitleRecommendationDTO> recommendations = new HashMap<>();

        // Si hay 2+ géneros, intentar primero encontrar títulos que contengan todos
        if (genres.size() >= 2) {
            List<String> genreNames = genres.stream()
                    .map(Pair::getLeft)
                    .collect(Collectors.toList());

            BigDecimal combinedScore = genres.stream()
                    .map(Pair::getRight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<String> multiGenreTitles = titleRepository.findTitlesWithAllGenres(
                    genreNames, viewedTitles);

            for (String tconst : multiGenreTitles) {
                // Excluir episodios de TV
                if (isTvEpisode(tconst)) {
                    continue;
                }

                // Crear o actualizar la recomendación
                TitleRecommendationDTO recommendation = recommendations.getOrDefault(
                        tconst, createBasicRecommendation(tconst));

                // Bonus por coincidir con múltiples géneros
                recommendation.setMatchCount(recommendation.getMatchCount() + genres.size());
                recommendation.setMatchScore(recommendation.getMatchScore().add(combinedScore));

                // Almacenar o actualizar en el mapa
                recommendations.put(tconst, recommendation);
            }
        }

        // Buscar títulos por género individual
        for (Pair<String, BigDecimal> genre : genres) {
            String genreName = genre.getLeft();
            BigDecimal score = genre.getRight();

            // Consultar títulos de este género
            List<String> genreTitles = titleRepository.findTitlesByGenre(genreName, viewedTitles);

            for (String tconst : genreTitles) {
                // Excluir episodios de TV
                if (isTvEpisode(tconst)) {
                    continue;
                }

                // Crear o actualizar la recomendación
                TitleRecommendationDTO recommendation = recommendations.getOrDefault(
                        tconst, createBasicRecommendation(tconst));

                // Actualizar coincidencias y puntuación solo si no se ha contado este género
                // antes
                // (evita contar los géneros múltiples dos veces)
                if (!recommendations.containsKey(tconst) || recommendation.getMatchCount() <= 1) {
                    recommendation.setMatchCount(recommendation.getMatchCount() + 1);
                    recommendation.setMatchScore(recommendation.getMatchScore().add(score));
                }

                // Almacenar o actualizar en el mapa
                recommendations.put(tconst, recommendation);
            }
        }

        return recommendations;
    }

    /**
     * Aplica penalizaciones basadas en los elementos excluidos
     */
    private void applyExclusionPenalties(List<TitleRecommendationDTO> recommendations,
            RecommendationScoresResult scores) {

        // Para cada recomendación, revisar si contiene elementos excluidos
        for (TitleRecommendationDTO recommendation : recommendations) {
            String tconst = recommendation.getTconst();

            // Verificar directores excluidos
            for (Pair<String, BigDecimal> excludedDirector : scores.excludeDirectors) {
                if (hasDirector(tconst, excludedDirector.getLeft())) {
                    // Reducir coincidencia y puntuación
                    recommendation.setMatchCount(recommendation.getMatchCount() - 1);
                    recommendation.setMatchScore(recommendation.getMatchScore()
                            .subtract(excludedDirector.getRight()));
                }
            }

            // Verificar actores excluidos
            for (Pair<String, BigDecimal> excludedActor : scores.excludeActors) {
                if (hasActor(tconst, excludedActor.getLeft())) {
                    // Reducir coincidencia y puntuación
                    recommendation.setMatchCount(recommendation.getMatchCount() - 1);
                    recommendation.setMatchScore(recommendation.getMatchScore()
                            .subtract(excludedActor.getRight()));
                }
            }

            // Verificar géneros excluidos
            for (Pair<String, BigDecimal> excludedGenre : scores.excludeGenres) {
                if (hasGenre(tconst, excludedGenre.getLeft())) {
                    // Reducir coincidencia y puntuación
                    recommendation.setMatchCount(recommendation.getMatchCount() - 1);
                    recommendation.setMatchScore(recommendation.getMatchScore()
                            .subtract(excludedGenre.getRight()));
                }
            }

            // Asegurar que el conteo de coincidencias no sea negativo
            if (recommendation.getMatchCount() < 0) {
                recommendation.setMatchCount(0);
            }

            // Asegurar que la puntuación no sea negativa
            if (recommendation.getMatchScore().compareTo(BigDecimal.ZERO) < 0) {
                recommendation.setMatchScore(BigDecimal.ZERO);
            }
        }
    }

    /**
     * Verifica si un título tiene un director específico
     */
    private boolean hasDirector(String tconst, String nconst) {
        Optional<Crew> crew = crewRepository.findById(tconst);
        return crew.isPresent() && crew.get().getDirectors().contains(nconst);
    }

    /**
     * Verifica si un título tiene un actor específico
     */
    private boolean hasActor(String tconst, String nconst) {
        return principalRepository.existsById_TconstAndNconst(tconst, nconst);
    }

    /**
     * Verifica si un título pertenece a un género específico
     */
    private boolean hasGenre(String tconst, String genre) {
        Optional<Title> title = titleRepository.findById(tconst);
        return title.isPresent() && title.get().getGenres().contains(genre);
    }

    /**
     * Crea una recomendación básica con la información del título
     */
    private TitleRecommendationDTO createBasicRecommendation(String tconst) {
        Optional<Title> titleOpt = titleRepository.findById(tconst);
        if (!titleOpt.isPresent()) {
            return null;
        }

        Title title = titleOpt.get();
        BigDecimal rating = getRatingForTitle(tconst);

        return TitleRecommendationDTO.builder()
                .tconst(tconst)
                .title(title.getPrimaryTitle())
                .rating(rating)
                .matchCount(0)
                .matchScore(BigDecimal.ZERO)
                .build();
    }

    /**
     * Obtiene la calificación para un título
     */
    private BigDecimal getRatingForTitle(String tconst) {
        return titleRatingRepository.findRatingByTconst(tconst)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Combina dos mapas de recomendaciones
     */
    private void mergeRecommendations(Map<String, TitleRecommendationDTO> allRecommendations,
            Map<String, TitleRecommendationDTO> newRecommendations) {

        for (Map.Entry<String, TitleRecommendationDTO> entry : newRecommendations.entrySet()) {
            String tconst = entry.getKey();
            TitleRecommendationDTO newRecommendation = entry.getValue();

            if (allRecommendations.containsKey(tconst)) {
                // Actualizar recomendación existente
                TitleRecommendationDTO existingRecommendation = allRecommendations.get(tconst);
                existingRecommendation.setMatchCount(
                        existingRecommendation.getMatchCount() + newRecommendation.getMatchCount());
                existingRecommendation.setMatchScore(
                        existingRecommendation.getMatchScore().add(newRecommendation.getMatchScore()));
            } else {
                // Agregar nueva recomendación
                allRecommendations.put(tconst, newRecommendation);
            }
        }
    }

    /**
     * Ordena las recomendaciones por: coincidencia > score > calificación
     */
    private void sortRecommendations(List<TitleRecommendationDTO> recommendations) {
        Collections.sort(recommendations, (a, b) -> {
            // Primero comparar por número de coincidencias (mayor primero)
            int compareMatch = Integer.compare(b.getMatchCount(), a.getMatchCount());
            if (compareMatch != 0) {
                return compareMatch;
            }

            // Luego por puntaje de coincidencia (mayor primero)
            int compareScore = b.getMatchScore().compareTo(a.getMatchScore());
            if (compareScore != 0) {
                return compareScore;
            }

            // Finalmente por calificación del título (mayor primero)
            return b.getRating().compareTo(a.getRating());
        });
    }

    /**
     * Obtiene los títulos mejor calificados
     */
    private List<TitleRecommendationDTO> getTopRatedTitles(List<String> viewedTitles, int limit) {
        List<String> topTitles = titleRatingRepository.findTopRatedTitles(viewedTitles, limit);

        List<TitleRecommendationDTO> recommendations = new ArrayList<>();
        for (String tconst : topTitles) {
            if (isTvEpisode(tconst)) {
                continue;
            }

            TitleRecommendationDTO recommendation = createBasicRecommendation(tconst);
            if (recommendation != null) {
                recommendations.add(recommendation);
            }
        }

        return recommendations;
    }

    /**
     * Verifica si un título es un episodio de TV
     */
    private boolean isTvEpisode(String tconst) {
        Optional<Title> title = titleRepository.findById(tconst);
        return title.isPresent() && "tvEpisode".equals(title.get().getTitleType());
    }

    private static class CategoryPriority implements Comparable<CategoryPriority> {
        RecommendationCategory category;
        BigDecimal topScore; // El score más alto encontrado en esta categoría
        List<Pair<String, BigDecimal>> allScoresForCategory; // La lista completa de scores para esta categoría

        CategoryPriority(RecommendationCategory category, BigDecimal topScore,
                List<Pair<String, BigDecimal>> allScores) {
            this.category = category;
            this.topScore = topScore;
            this.allScoresForCategory = allScores;
        }

        @Override
        public int compareTo(CategoryPriority other) {
            // Ordenar en forma descendente por topScore para que la categoría con el mayor
            // puntaje quede primera
            return other.topScore.compareTo(this.topScore);
        }
    }
}
