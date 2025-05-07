package com.cisvan.api.domain.recommendation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.RecommendedTitleDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.titlerating.TitleRating;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final TitleRepository titleRepository;
    private final TitleService titleService;

    public List<RecommendedTitleDTO> getGenreBasedRecommendations(String tconst) {
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty()) return List.of();

        Title baseTitle = titleOpt.get();
        String genresArrayLiteral = toPostgresArray(baseTitle.getGenres());

        // 1. Exact genre match
        List<Object[]> exactRows = titleRepository.findTop10ByGenresContainingAll(genresArrayLiteral, tconst);

        Set<String> seenTconsts = new HashSet<>();
        List<RecommendedTitleDTO> results = mapToDtoList(exactRows, seenTconsts);

        // 2. Partial genre match (if needed)
        int remaining = 10 - results.size();
        if (remaining > 0) {
            seenTconsts.add(tconst);
            List<Object[]> partialRows = titleRepository.findTopByAnyMatchingGenreExcluding(
                genresArrayLiteral,
                new ArrayList<>(seenTconsts),
                remaining
            );
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
                    : (rawPosterUrl.startsWith("http") ? rawPosterUrl : 
                       "https://m.media-amazon.com/images/M/" + rawPosterUrl + "._V1_SX300.jpg");
    
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
}
