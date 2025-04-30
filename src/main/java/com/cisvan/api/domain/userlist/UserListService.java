package com.cisvan.api.domain.userlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserListService {

    private final UserListRepository userListRepository;

    public List<UserList> getUserList(Long userId) {
        return userListRepository.findByUserId(userId);
    }

    public UserList create(UserList userList) {
        return userListRepository.save(userList);
    }

    public Optional<UserList> findByUserIdAndTitleId(Long userId, String titleId) {
        return userListRepository.findByUserIdAndTitleId(userId, titleId);
    }

    public boolean existsByUserIdAndTitleId(Long userId, String titleId) {
        return userListRepository.existsByUserIdAndTitleId(userId, titleId);
    }    

    public void delete(UserList userList) {
        userListRepository.delete(userList);
    }
    
    public List<String> getTitleIdsByUserId(Long userId) {
        return userListRepository.findTitleIdsByUserId(userId);
    }
}
