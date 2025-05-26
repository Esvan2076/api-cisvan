package com.cisvan.api.domain.searchhistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchSchedulerService {
    
    private final SearchHistoryService searchHistoryService;
    
    @Scheduled(cron = "0 0 14 * * *") // Todos los días a las 14:00
    public void updateTrendingSearches() {
        log.info("Iniciando actualización de búsquedas trending...");
        try {
            searchHistoryService.updateTrendingSearches();
            log.info("Actualización de búsquedas trending completada exitosamente");
        } catch (Exception e) {
            log.error("Error al actualizar búsquedas trending", e);
        }
    }
}