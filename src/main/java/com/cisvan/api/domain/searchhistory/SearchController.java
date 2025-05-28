package com.cisvan.api.domain.searchhistory;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.searchhistory.dtos.SearchSuggestionDTO;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import java.util.Optional;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchHistoryService searchHistoryService;
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
        
        // Solo registrar clicks si hay usuario autenticado
        try {
            Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
            if (userOpt.isEmpty()) {
                // No hay usuario, pero no es un error - simplemente no registramos
                return ResponseEntity.ok().build();
            }
            
            Users user = userOpt.get();
            searchHistoryService.recordSearch(
                user.getId(),
                clickData.getSearchTerm(),
                clickData.getResultType(),
                clickData.getResultId(),
                clickData.getResultTitle()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // En caso de error, devolver OK para no interrumpir la experiencia del usuario
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