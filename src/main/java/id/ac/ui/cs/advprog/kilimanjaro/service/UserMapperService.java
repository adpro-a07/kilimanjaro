package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserNotFoundException;

import java.util.UUID;

public interface UserMapperService {

    /**
     * Get user data by user ID
     *
     * @param userId The user ID
     * @return The user data
     * @throws UserNotFoundException if the user is not found
     */
    UserData getUserById(UUID userId) throws UserNotFoundException;

    /**
     * Get user data by user ID with profile
     *
     * @param userId The user ID
     * @param includeProfile Whether to include the profile or not
     * @return The user data
     * @throws UserNotFoundException if the user is not found
     */
    UserData getUserById(UUID userId, boolean includeProfile) throws UserNotFoundException;

    /**
     * Get user data by email
     *
     * @param email The user's email
     * @return The user data
     * @throws UserNotFoundException if the user is not found
     */
    UserData getUserByEmail(String email) throws UserNotFoundException;

    /**
     * Get user data by email with profile
     *
     * @param email The user's email
     * @param includeProfile Whether to include the profile or not
     * @return The user data
     * @throws UserNotFoundException if the user is not found
     */
    UserData getUserByEmail(String email, boolean includeProfile) throws UserNotFoundException;

    /**
     * Get user data from a JWT token
     *
     * @param token The JWT token
     * @return The user data
     * @throws AuthenticationException if the token is invalid
     * @throws UserNotFoundException if the user is not found
     */
    UserData getUserDataFromToken(String token) throws AuthenticationException, UserNotFoundException;
}
