package com.cisvan.api.domain.searchhistory;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.searchhistory.dtos.SearchSuggestionDTO;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import java.util.Optional;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchHistoryService searchHistoryService;
    private final TitleRepository titleRepository;
    private final UserLogicService userLogicService;

    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionDTO> getSuggestions(HttpServletRequest request) {
        // Hacer opcional la autenticación
        Long userId = null;
        try {
            Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
            userId = userOpt.map(Users::getId).orElse(null);
        } catch (Exception e) {
            // Si no hay usuario autenticado, continuar sin userId
            userId = null;
        }
        
        return ResponseEntity.ok(searchHistoryService.getSuggestions(userId));
    }

    @PostMapping("/click")
    public ResponseEntity<?> recordSearchClick(
            @RequestBody SearchClickDTO clickData,
            HttpServletRequest request) {

        try {
            Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok().build();
            }

            String resultType = clickData.getResultType();

            // Si es contenido (id que empieza con "tt"), verificar tipo de título
            if (clickData.getResultId() != null && clickData.getResultId().startsWith("tt")) {
                Optional<Title> titleOpt = titleRepository.findById(clickData.getResultId());

                if (titleOpt.isPresent()) {
                    String titleType = titleOpt.get().getTitleType();
                    if ("tvSeries".equalsIgnoreCase(titleType) || "tvMiniSeries".equalsIgnoreCase(titleType)) {
                        resultType = "tvSeries";
                    }
                }
            }

            Users user = userOpt.get();

            searchHistoryService.recordSearch(
                    user.getId(),
                    clickData.getSearchTerm(),
                    resultType,
                    clickData.getResultId(),
                    clickData.getResultTitle()
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error recording search click: " + e.getMessage());
            return ResponseEntity.ok().build();
        }
    }
    
    @Data
    static class SearchClickDTO {
        private String searchTerm;
        private String resultType;
        private String resultId;
        private String resultTitle;
    }
}