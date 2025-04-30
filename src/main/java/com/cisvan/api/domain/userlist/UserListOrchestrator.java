package com.cisvan.api.domain.userlist;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserListOrchestrator {

    private final UserListRepository userListRepository;
    private final UserListService userListService;
    private final UserLogicService userLogicService;
    private final TitleService titleService;

    public List<UserList> getUserListByID(HttpServletRequest request) {
        // 1. Obtener el usuario autenticado
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return List.of(); 
        }
        Users user = userOpt.get();

        return userListRepository.findByUserId(user.getId());
    }

    public boolean addToUserList(HttpServletRequest request, String tconst) {
        // 1. Obtener el usuario autenticado
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return false; 
        }
        Users user = userOpt.get();

        if (!titleService.existsById(tconst)) {
            return false; // Ya existe
        }

        if (userListService.existsByUserIdAndTitleId(user.getId(), tconst)) {
            return false; // Ya existe
        }

        UserList entry = UserList.builder()
                .userId(user.getId())
                .titleId(tconst)
                .build();

        userListService.create(entry);
        return true;
    }

    public boolean removeFromUserList(HttpServletRequest request, String tconst) {
        // 1. Obtener el usuario autenticado
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return false; 
        }
        Users user = userOpt.get();
    
        Optional<UserList> entryOpt = userListService.findByUserIdAndTitleId(user.getId(), tconst);
        if (entryOpt.isEmpty()) {
            return false; // No existe la entrada a eliminar
        }
    
        userListService.delete(entryOpt.get());
        return true;
    }
}
