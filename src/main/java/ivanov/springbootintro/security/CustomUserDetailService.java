package ivanov.springbootintro.security;

import ivanov.springbootintro.exception.InvalidLoginOrPasswordException;
import ivanov.springbootintro.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new InvalidLoginOrPasswordException("User login or password incorrect"));
    }
}
