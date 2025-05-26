package com.cisvan.api.domain.searchhistory;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
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
    
    @PostMapping("/click")
    public ResponseEntity<?> recordSearchClick(
            @RequestBody SearchClickDTO clickData,
            HttpServletRequest request) {
        
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            System.out.println("No user found in request for search click");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Users user = userOpt.get();
        System.out.println("Recording search click for user " + user.getId() + ": " + 
                        clickData.getResultTitle() + " (" + clickData.getResultId() + ")");
        
        try {
            searchHistoryService.recordSearch(
                user.getId(),
                clickData.getSearchTerm(),
                clickData.getResultType(),
                clickData.getResultId(),
                clickData.getResultTitle()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error recording search click: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionDTO> getSuggestions(HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        Long userId = userOpt.map(Users::getId).orElse(null);
        
        return ResponseEntity.ok(searchHistoryService.getSuggestions(userId));
    }
    
    @Data
    static class SearchClickDTO {
        private String searchTerm;
        private String resultType;
        private String resultId;
        private String resultTitle;
    }
}