package com.cisvan.api.domain.streaming.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.episode.services.EpisodeLogicService;
import com.cisvan.api.domain.streaming.Streaming;
import com.cisvan.api.domain.streaming.StreamingRepository;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.titlestream.services.TitleStreamService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StreamingLogicService {

    private final StreamingRepository streamingRepository;
    private final TitleStreamService titleStreamService;
    private final EpisodeLogicService episodeLogicService;
    private final TitleService titleService;

    public List<Streaming> getStreamingServicesByTitle(String tconst) {
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty()) {
            return List.of();
        }

        Title title = titleOpt.get();

        // Resolver el tconst que realmente se usará para buscar servicios de streaming
        String searchTconst = episodeLogicService.resolveSearchTconst(tconst, title.getTitleType());

        // Obtener los IDs desde title_stream
        List<Integer> streamingIds = titleStreamService.getStreamingsByTitleId(searchTconst);

        // Traer máximo 4 servicios de streaming y filtrar los que existan
        return streamingIds.stream()
                .limit(4)
                .map(streamingRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
