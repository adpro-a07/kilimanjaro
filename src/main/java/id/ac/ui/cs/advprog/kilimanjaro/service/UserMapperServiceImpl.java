package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserNotFoundException;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapper;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapperFactory;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserMapperServiceImpl implements UserMapperService {
    private static final Logger logger = LoggerFactory.getLogger(UserMapperServiceImpl.class);

    private final UserRepository userRepository;
    private final UserDataMapperFactory mapperFactory;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public UserMapperServiceImpl(UserRepository userRepository,
                                 UserDataMapperFactory mapperFactory,
                                 JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.mapperFactory = mapperFactory;
        this.jwtTokenService = jwtTokenService;
    }


    @Override
    public UserData getUserById(UUID userId) throws UserNotFoundException {
        logger.info("Fetching user with ID: {}", userId);
        BaseUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user);
    }

    @Override
    public UserData getUserById(UUID userId, boolean includeProfile) throws UserNotFoundException {
        logger.info("Fetching user with ID: {} and includeProfile: {}", userId, includeProfile);
        BaseUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user, includeProfile);
    }

    @Override
    public UserData getUserByEmail(String email) throws UserNotFoundException {
        logger.info("Fetching user with email: {}", email);
        BaseUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user);
    }

    @Override
    public UserData getUserByEmail(String email, boolean includeProfile) throws UserNotFoundException {
        logger.info("Fetching user with email: {} and includeProfile: {}", email, includeProfile);
        BaseUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user, includeProfile);
    }

    @Override
    public UserData getUserDataFromToken(String token) throws AuthenticationException, UserNotFoundException {
        logger.info("Fetching user data from token: {}", token);
        BaseUser user = jwtTokenService.getUserFromToken(token);
        if (user == null) {
            throw new UserNotFoundException("User not found in token");
        }

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user);
    }
}
