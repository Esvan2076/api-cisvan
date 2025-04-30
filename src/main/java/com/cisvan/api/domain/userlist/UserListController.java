package com.cisvan.api.domain.userlist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/user-list")
@RequiredArgsConstructor
public class UserListController {

    private final UserListOrchestrator userListOrchestrator;

    @GetMapping
    public ResponseEntity<List<UserList>> getUserList(HttpServletRequest request) {
        return ResponseEntity.ok(userListOrchestrator.getUserListByID(request));
    }

    @PostMapping("/{titleId}")
    public ResponseEntity<?> addToUserList(
        HttpServletRequest request,
        @PathVariable("titleId") String tconst
    ){
        boolean added = userListOrchestrator.addToUserList(request, tconst);

        return added 
            ? ResponseEntity.status(HttpStatus.CREATED).build()
            : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromUserList(
        HttpServletRequest request,
        @PathVariable("id") String tconst
    ) {
        boolean removed = userListOrchestrator.removeFromUserList(request, tconst);
    
        return removed
            ? ResponseEntity.status(HttpStatus.OK).build()
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}