package com.cisvan.api.component.title;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisvan.api.component.crew.TitleCrewRepository;
import com.cisvan.api.component.episode.TitleEpisode;
import com.cisvan.api.component.episode.TitleEpisodeRepository;
import com.cisvan.api.component.name.NameBasicsService;
import com.cisvan.api.component.name.dto.NameBasicsDTO;
import com.cisvan.api.component.ratings.TitleRatingsRepository;
import com.cisvan.api.component.streaming.Streaming;
import com.cisvan.api.component.streaming.StreamingRepository;
import com.cisvan.api.component.title.dto.TitleBasicsDTO;
import com.cisvan.api.component.title.mapper.TitleBasicsMapper;
import com.cisvan.api.component.titlestream.TitleStreamRepository;

@Service
public class TitleBasicsService {
    @Autowired
    private TitleBasicsRepository titleBasicsRepository;

    @Autowired
    private TitleRatingsRepository titleRatingsRepository;

    @Autowired
    private NameBasicsService nameBasicsService;

    @Autowired
    private TitleCrewRepository titleCrewRepository;

    @Autowired
    private TitleEpisodeRepository titleEpisodeRepository;

    @Autowired
    private TitleStreamRepository titleStreamRepository;

    @Autowired
    private StreamingRepository streamingRepository;
    
    @Autowired
    private TitleBasicsMapper titleBasicsMapper;

    public Optional<TitleBasics> findById(String tconst) {
        return titleBasicsRepository.findById(tconst);
    }

    public List<TitleBasics> findByName(String name) {
        return titleBasicsRepository.findByPrimaryTitleContainingIgnoreCase(name);
    }

    public Optional<TitleBasicsDTO> basicById(String tconst) {
        Optional<TitleBasics> titleBasicsOpt = titleBasicsRepository.findById(tconst);
        if (titleBasicsOpt.isEmpty()) {
            return Optional.empty();
        }
    
        TitleBasics titleBasics = titleBasicsOpt.get();
    
        // Modificar si es un episodio
        adjustForEpisode(titleBasics, tconst);
    
        TitleBasicsDTO dto = titleBasicsMapper.toDTO(titleBasics);
    
        // Directores y escritores
        titleCrewRepository.findById(tconst).ifPresent(titleCrew -> {
            List<NameBasicsDTO> directos = nameBasicsService.findNameBasicsByIds(
                titleCrew.getDirectors().stream().limit(3).toList()
            );
            List<NameBasicsDTO> writers = nameBasicsService.findNameBasicsByIds(
                titleCrew.getWriters().stream().limit(3).toList()
            );
            dto.setDirectos(directos);
            dto.setWriters(writers);
        });
    
        // Ratings
        titleRatingsRepository.findById(tconst).ifPresent(dto::setRatings);
    
        // Determinar el tconst a usar para streaming (si es episodio, usar el parent)
        String searchTconst = tconst;
        if ("tvEpisode".equalsIgnoreCase(titleBasics.getTitleType())) {
            Optional<String> parentTconstOpt = titleEpisodeRepository.findParentTconstByEpisodeTconst(tconst);
            if (parentTconstOpt.isPresent()) {
                searchTconst = parentTconstOpt.get();
            }
        }
    
        // Obtener IDs de los sistemas de streaming
        List<Integer> streamingIds = titleStreamRepository.findStreamingIdsByTconst(searchTconst);
    
        // Obtener cada Streaming por ID y construir la lista
        // Obtener máximo 2 servicios de streaming
        List<Streaming> streamingServices = streamingIds.stream()
            .limit(4)
            .map(streamingRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    
        // Asignar la lista al DTO
        dto.setStreamingServices(streamingServices);
    
        return Optional.of(dto);
    }    

    private void adjustForEpisode(TitleBasics titleBasics, String tconst) {
        if (!"tvEpisode".equalsIgnoreCase(titleBasics.getTitleType())) {
            return;
        }
    
        Optional<TitleEpisode> episodeOpt = titleEpisodeRepository.findById(tconst);
        if (episodeOpt.isEmpty()) {
            return;
        }
    
        TitleEpisode episode = episodeOpt.get();
    
        Optional<String> parentTconstOpt = titleEpisodeRepository.findParentTconstByEpisodeTconst(tconst);
        if (parentTconstOpt.isEmpty()) {
            return;
        }
    
        String parentTconst = parentTconstOpt.get();
    
        // Aplicar los ajustes por separado
        adjustPrimaryTitleWithSeasonPrefix(titleBasics, episode);
        replaceOriginalTitleWithSeriesTitle(titleBasics, parentTconst);
        replacePosterWithSeriesPoster(titleBasics, parentTconst);
    }

    private void adjustPrimaryTitleWithSeasonPrefix(TitleBasics titleBasics, TitleEpisode episode) {
        String seasonEpisodePrefix = episode.getSeasonNumber() + "." + episode.getEpisodeNumber();
        titleBasics.setPrimaryTitle(seasonEpisodePrefix + ": " + titleBasics.getPrimaryTitle());
    }

    private void replaceOriginalTitleWithSeriesTitle(TitleBasics titleBasics, String parentTconst) {
        titleBasicsRepository.findById(parentTconst).ifPresent(parent -> {
            titleBasics.setOriginalTitle(parent.getPrimaryTitle());
        });
    }

    private void replacePosterWithSeriesPoster(TitleBasics titleBasics, String parentTconst) {
        titleBasicsRepository.findById(parentTconst).ifPresent(parent -> {
            titleBasics.setPosterUrl(parent.getPosterUrl());
        });
    }    
}
