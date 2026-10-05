package com.agrolink.backend.service;

import com.agrolink.backend.dto.UpdateProfileRequest;
import com.agrolink.backend.model.User;
import com.agrolink.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void updateProfilePersistsLocationForOrderMap() {
        User user = new User();
        user.setId(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Nimal");
        request.setLatitude(6.9271);
        request.setLongitude(79.8612);

        User updated = userService.updateProfile(7, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(updated.getName()).isEqualTo("Nimal");
        assertThat(userCaptor.getValue().getLatitude()).isEqualTo(6.9271);
        assertThat(userCaptor.getValue().getLongitude()).isEqualTo(79.8612);
    }
}
