package com.cisvan.api.domain.users;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.profileImage.ProfileImage;
import com.cisvan.api.domain.profileImage.ProfileImageService;
import com.cisvan.api.domain.userfollow.UserFollowService;
import com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO;
import com.cisvan.api.domain.userprestige.UserPrestige;
import com.cisvan.api.domain.userprestige.UserPrestigeRepository;
import com.cisvan.api.domain.userprestige.UserPrestigeService;
import com.cisvan.api.domain.users.dto.request.AuthRequest;
import com.cisvan.api.domain.users.dto.request.LoginRequest;
import com.cisvan.api.domain.users.dto.request.ProfileUrl;
import com.cisvan.api.domain.users.dto.request.ResetPasswordRequest;
import com.cisvan.api.domain.users.dto.request.VerificationCodeRequest;
import com.cisvan.api.domain.users.dto.response.EmailVerificationResponse;
import com.cisvan.api.domain.users.dto.response.NotificationPromptStatusDTO;
import com.cisvan.api.domain.users.dto.response.UserDTO;
import com.cisvan.api.domain.users.dto.response.UserProfileDTO;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeExtendedDTO;
import com.cisvan.api.domain.users.mapper.UserMapper;
import com.cisvan.api.domain.users.services.UserLogicService;
import com.cisvan.api.domain.users.services.UserService;
import com.cisvan.api.domain.users.services.UserValidationService;
import com.cisvan.api.services.JwtService;
import com.cisvan.api.services.S3Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserOrchestrator {

    private final UserValidationService userValidationService;
    private final UserService userService;
    private final ProfileImageService profileImageService;
    private final UserLogicService userLogicService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final UserFollowService followService;
    private final BCryptPasswordEncoder encoder;
    private final UserPrestigeService userPrestigeService;
    private final UserPrestigeRepository userPrestigeRepository;
    private final Random random = new Random();
    
    @Transactional
    public Optional<EmailVerificationResponse> register(AuthRequest request, OperationResult operationResult) {
        // Validar si el username ya existe
        if (!userValidationService.validateUsernameAvailable(request.getUsername(), operationResult)) {
            return Optional.empty();
        }

        // Normalizar email
        String email = request.getEmail().trim().toLowerCase();

        // Validar si el email ya está registrado
        if (!userValidationService.validateEmailAvailable(request.getEmail(), operationResult)) {
            return Optional.empty();
        }

        // Generar código PIN de 4 dígitos
        String code = generate4DigitCode();

        // Crear usuario nuevo
        Users user = Users.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .email(email)
                .admin(false)
                .emailVerified(false)
                .emailVerificationCode(code)
                .passwordResetExpiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        // Guardar en base de datos
        userService.create(user);

        // Devolver respuesta con el código de verificación
        return Optional.of(new EmailVerificationResponse(
                user.getUsername(),
                user.getEmail(),
                code
        ));
    }

    public Optional<UserProfileDTO> getProfile(HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);

        return userOpt.map(user -> {
            FollowStatsDTO stats = followService.getFollowStats(user.getId());
            UserProfileDTO dto = userMapper.toDto(user, stats);

            // Obtener la preferencia de notificación por correo
            dto.setEmailNotifications(user.getEmailNotifications());

            userPrestigeService.getPrestigeDTOByUserId(user.getId())
                .ifPresent(dto::setUserPrestigeDTO);

            return dto;
        });
    }

    public List<UserSummaryPrestigeDTO> getFollowers(HttpServletRequest request) {
        return userLogicService.getUserFromRequest(request)
                .map(user -> followService.getFollowersOfUser(user.getId()))
                .orElse(Collections.emptyList());
    }   

    public List<UserSummaryPrestigeDTO> getFollowing(HttpServletRequest request) {
        return userLogicService.getUserFromRequest(request)
                .map(user -> followService.getFollowingOfUser(user.getId()))
                .orElse(Collections.emptyList());
    }   

    public String generate4DigitCode() {
        return String.format("%04d", random.nextInt(10000));
    }

    @Transactional(readOnly = true)
    public Optional<String> login(LoginRequest request, OperationResult result) {
        String email = request.getEmail().trim().toLowerCase();

        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return Optional.empty();
    
        Users user = userOpt.get();

        if (!userValidationService.validatePasswordMatches(user, request.getPassword(), result)) {
            return Optional.empty();
        }

        if (!userValidationService.validateEmailVerified(user, result)) {
            return Optional.empty();
        }

        if (!userValidationService.validateBanned(user, result)) {
            return Optional.empty();
        }

        // Todo OK → Generar y devolver JWT
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getAdmin());
        return Optional.of(token);
    }

    public Optional<UserDTO> getMe(HttpServletRequest request) {
        return userLogicService.getUserFromRequest(request)
            .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getAdmin()));
    }    

    @Transactional
    public Optional<String> verifyEmail(VerificationCodeRequest request, OperationResult result) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode();

        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return Optional.empty();
    
        Users user = userOpt.get();

        if (!userValidationService.validateVerificationCode(user, code, result)) {
            return Optional.empty();
        }

        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        user.setPasswordResetExpiresAt(null);
        userService.create(user);

        String jwt = jwtService.generateToken(user.getId(), user.getUsername(), user.getAdmin());
        return Optional.of(jwt);
    }

    @Transactional
    public Optional<EmailVerificationResponse> resendEmailVerificationCode(String email, OperationResult result) {
        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return Optional.empty();
    
        Users user = userOpt.get();
    
        String code = generate4DigitCode();
        user.setEmailVerificationCode(code);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(10));
        userService.create(user);
    
        return Optional.of(new EmailVerificationResponse(user.getUsername(), user.getEmail(), code));
    }    

    @Transactional
    public Optional<EmailVerificationResponse> forgotPassword(String email, OperationResult result) {
        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return Optional.empty();
    
        Users user = userOpt.get();
    
        if (!userValidationService.validateEmailVerified(user, result)) {
            return Optional.empty(); // Nada de null, Optional.empty() es tu amigo
        }
    
        String code = generate4DigitCode();
        user.setPasswordResetCode(code);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(10));
        userService.create(user);
    
        return Optional.of(new EmailVerificationResponse(
            user.getUsername(),
            user.getEmail(),
            code
        ));
    }    

    @Transactional
    public boolean resetPassword(ResetPasswordRequest request, OperationResult result) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return false;
    
        Users user = userOpt.get();

        if (!userValidationService.validateResetCode(user, code, result)) {
            return false;
        }

        user.setPassword(encoder.encode(newPassword));
        user.setPasswordResetCode(null);
        user.setPasswordResetExpiresAt(null);
        userService.create(user);

        return true;
    }

    public Optional<String> updateProfileImage(HttpServletRequest request, MultipartFile file, OperationResult result) {
        // 1. Validar tamaño de imagen
        if (!userValidationService.validateImageSize(file, result)) {
            return Optional.empty();
        }

        // 2. Obtener el usuario autenticado
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        Users user = userOpt.get();

        // 3. Subir el archivo a S3
        Optional<String> uploadedUrlOpt = s3Service.uploadFile(file, result);
        if (uploadedUrlOpt.isEmpty()) {
            return Optional.empty();
        }
        String imageUrl = uploadedUrlOpt.get();

        // 4. Actualizar URL de la imagen en el usuario
        user.setProfileImageUrl(imageUrl);
        userService.create(user);

        // 5. Registrar el histórico en profile_image
        ProfileImage profileImage = ProfileImage.builder()
                .userId(user.getId())
                .imageUrl(imageUrl)
                .build();
            profileImageService.create(profileImage);

        // 6. Retornar URL de la nueva imagen
        return Optional.of(imageUrl);
    }

    public Optional<ProfileUrl> updateProfileImageFromUrl(HttpServletRequest request, String imageUrl, OperationResult result) {
        // 1. Obtener el usuario autenticado
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        Users user = userOpt.get();
    
        // 2. Actualizar URL de la imagen
        user.setProfileImageUrl(imageUrl);
        userService.create(user);
    
        // 4. Retornar la nueva URL dentro de ProfileUrl
        return Optional.of(new ProfileUrl(imageUrl));
    }

    public void deleteProfileImage(HttpServletRequest request, OperationResult result) {
        // 1. Obtener el usuario autenticado
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return;
        }
        Users user = userOpt.get();

        // 2. Borrar la URL de la imagen (poner en null)
        user.setProfileImageUrl(null);
        userService.create(user);
    }

    public List<UserSummaryPrestigeDTO> getFollowersSummary(Long userId) {
        return followService.getFollowersOfUser(userId);
    }

    public boolean toggleFollow(HttpServletRequest request, Long targetUserId) {
        // 1. Obtener el usuario autenticado
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return false;
        }
        Users user = userOpt.get();

        if (user.getId()==targetUserId) {
            return false;
        }

        // 3. Consultar si ya lo sigue
        boolean alreadyFollowing = followService.isFollowing(user.getId(), targetUserId);

        if (alreadyFollowing) {
            followService.unfollow(user.getId(), targetUserId);
            return true; // ahora lo dejó de seguir
        } else {
            followService.follow(user.getId(), targetUserId);
            return true; // ahora lo sigue
        }
    }
    

    public boolean activateEmailNotification(HttpServletRequest request) {
        // Obtener el usuario autenticado desde el token
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return false;
        }

        Users user = userOpt.get();
        user.setEmailNotifications(true);
        user.setHasAnsweredNotificationPrompt(true);
        userService.create(user);
        return true;
    }

    public boolean deactivateEmailNotification(HttpServletRequest request) {
        // Obtener el usuario autenticado desde el token
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return false;
        }

        Users user = userOpt.get();
        user.setEmailNotifications(false);
        user.setHasAnsweredNotificationPrompt(true);
        userService.create(user);
        return false;
    }

    public NotificationPromptStatusDTO getNotificationPromptStatus(HttpServletRequest request) {
        // Obtener el usuario autenticado desde el token
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return NotificationPromptStatusDTO.builder()
                    .hasAnsweredNotificationPrompt(false)
                    .build();
        }

        Users user = userOpt.get();
        return NotificationPromptStatusDTO.builder()
                .hasAnsweredNotificationPrompt(user.getHasAnsweredNotificationPrompt())
                .build();
    }

    public Optional<UserSummaryPrestigeExtendedDTO> getMainOfUser(HttpServletRequest request, Long userId) {
        
        Optional<Users> targetUserOpt = userService.getById(userId);
        if (targetUserOpt.isEmpty()) return Optional.empty();

        Users targetUser = targetUserOpt.get();

        // Obtener userId autenticado (si existe)
        Optional<Users> currentUserOpt = userLogicService.getUserFromRequest(request);
        Long currentUserId = currentUserOpt.map(Users::getId).orElse(null);

        // Obtener prestigio si existe
        Optional<UserPrestige> prestigeOpt = userPrestigeService.getPrestigeByUserId(userId);

        // Verificar si el usuario autenticado sigue a este usuario
        boolean isFollowing = false;
        boolean isMySelf = false;

        if (currentUserId != null) {
            isFollowing = followService.isFollowing(currentUserId, userId);
            isMySelf = currentUserId.equals(userId);
        }

        UserSummaryPrestigeExtendedDTO dto = UserSummaryPrestigeExtendedDTO.extendedBuilder()
                .id(targetUser.getId())
                .username(targetUser.getUsername())
                .profileImageUrl(
                        targetUser.getProfileImageUrl() != null
                                ? targetUser.getProfileImageUrl()
                                : "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg"
                )
                .currentRank(prestigeOpt.map(UserPrestige::getCurrentRank).orElse((short) 0))
                .trendDirection(prestigeOpt.map(UserPrestige::getTrendDirection).orElse(null))
                .following(isFollowing)
                .mySelf(isMySelf)
                .build();

        return Optional.of(dto);
    }

    public Page<UserSummaryPrestigeDTO> searchUsersByUsername(String username, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<Users> usersPage = userService.searchByUsername(username, pageable);

        List<UserSummaryPrestigeDTO> dtos = usersPage.getContent().stream()
            .map(user -> {
                Optional<UserPrestige> prestigeOpt = userPrestigeRepository.findById(user.getId());
                return UserSummaryPrestigeDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .profileImageUrl(user.getProfileImageUrl())
                        .currentRank(prestigeOpt.map(UserPrestige::getCurrentRank).orElse((short) 0))
                        .trendDirection(prestigeOpt.map(UserPrestige::getTrendDirection).orElse(null))
                        .build();
            }).toList();

        return new PageImpl<>(dtos, pageable, usersPage.getTotalElements());
    }

}
