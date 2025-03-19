package com.cisvan.api.component.title;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisvan.api.component.crew.TitleCrew;
import com.cisvan.api.component.crew.TitleCrewRepository;
import com.cisvan.api.component.name.NameBasicsService;
import com.cisvan.api.component.name.dto.NameBasicsDTO;
import com.cisvan.api.component.ratings.TitleRatings;
import com.cisvan.api.component.ratings.TitleRatingsRepository;
import com.cisvan.api.component.title.dto.TitleBasicsDTO;
import com.cisvan.api.component.title.mapper.TitleBasicsMapper;

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
    private TitleBasicsMapper titleBasicsMapper;

    public Optional<TitleBasics> findById(String tconst) {
        return titleBasicsRepository.findById(tconst);
    }

    public List<TitleBasics> findByName(String name) {
        return titleBasicsRepository.findByPrimaryTitleContainingIgnoreCase(name);
    }

    public Optional<TitleBasicsDTO> basicById(String tconst) {
        // Buscar si el título existe
        Optional<TitleBasics> titleBasicsOpt = titleBasicsRepository.findById(tconst);
        if (titleBasicsOpt.isEmpty()) {
            return Optional.empty();
        }

        // Convertir `TitleBasics` en `TitleBasicsDTO` usando el Mapper
        TitleBasicsDTO dto = titleBasicsMapper.toDTO(titleBasicsOpt.get());

        // 3️⃣ Buscar directores y escritores en `title_crew`
        Optional<TitleCrew> titleCrewOpt = titleCrewRepository.findById(tconst);
        if (titleCrewOpt.isPresent()) {
            TitleCrew titleCrew = titleCrewOpt.get();

            // Conseguir los IDs de los directores (máximo 3)
            List<NameBasicsDTO> directos = nameBasicsService.findNameBasicsByIds(
                titleCrew.getDirectors().stream().limit(3).toList()
            );

            // Conseguir los IDs de los escritores (máximo 3)
            List<NameBasicsDTO> writers = nameBasicsService.findNameBasicsByIds(
                titleCrew.getWriters().stream().limit(3).toList()
            );

            // 🔹 Asignar a DTO
            dto.setDirectos(directos);
            dto.setWriters(writers);
        }

        // 4️⃣ Buscar ratings en `title_ratings`
        Optional<TitleRatings> titleRatingsOpt = titleRatingsRepository.findById(tconst);
        titleRatingsOpt.ifPresent(dto::setRatings);

        return Optional.of(dto);
    }
}
