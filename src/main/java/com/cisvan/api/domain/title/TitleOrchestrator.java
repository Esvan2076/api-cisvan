package com.cisvan.api.domain.title;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.crew.CrewRepository;
import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.name.services.NameLogicService;
import com.cisvan.api.domain.rating.RatingRepository;
import com.cisvan.api.domain.streaming.services.StreamingLogicService;
import com.cisvan.api.domain.title.dtos.TitleBasicDTO;
import com.cisvan.api.domain.title.mappers.TitleMapper;
import com.cisvan.api.domain.title.services.TitleLogicService;
import com.cisvan.api.domain.title.services.TitleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleOrchestrator {

    private final CrewRepository crewRepository;
    private final RatingRepository ratingRepository;
    private final TitleLogicService titleLogicService;
    private final TitleService titleService;
    private final NameLogicService nameLogicService;
    private final TitleMapper titleMapper;
    private final StreamingLogicService streamingLogicService;

    public Optional<TitleBasicDTO> getTitleBasicById(String tconst) {
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty()) {
            return Optional.empty();
        }

        Title title = titleOpt.get();

        // Ajustes si es episodio
        titleLogicService.adjustForEpisode(title, tconst);

        TitleBasicDTO detailDTO = titleMapper.toDTO(title);

        // Directores y escritores
        crewRepository.findById(tconst).ifPresent(titleCrew -> {
            List<NameBasicDTO> directos = nameLogicService.getNameBasicsDTOsByIds(
                titleCrew.getDirectors().stream().limit(3).toList()
            );
            List<NameBasicDTO> writers = nameLogicService.getNameBasicsDTOsByIds(
                titleCrew.getWriters().stream().limit(3).toList()
            );
            detailDTO.setDirectos(directos);
            detailDTO.setWriters(writers);
        });

        // Ratings
        ratingRepository.findById(tconst).ifPresent(detailDTO::setRatings);

        // Streaming
        detailDTO.setStreamingServices(streamingLogicService.getStreamingServicesByTitle(tconst));

        return Optional.of(detailDTO);
    }
}
