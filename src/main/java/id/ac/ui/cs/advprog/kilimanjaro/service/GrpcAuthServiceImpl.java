package id.ac.ui.cs.advprog.kilimanjaro.service;

import com.google.protobuf.Timestamp;
import id.ac.ui.cs.advprog.kilimanjaro.auth.*;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.JwtTokenProvider;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.LocalDateTime;

@GrpcService
public class GrpcAuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private final JwtTokenProvider jwtTokenProvider;

    public GrpcAuthServiceImpl(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void verifyToken(VerifyTokenRequest request,
                            StreamObserver<VerifyTokenResponse> responseObserver) {
        try {
            boolean isValid = jwtTokenProvider.validateToken(request.getToken());

            if (isValid) {
                responseObserver.onNext(VerifyTokenResponse.newBuilder()
                        .setValid(true)
                        .build());
            } else {
                responseObserver.onNext(VerifyTokenResponse.newBuilder()
                        .setValid(false)
                        .build());
            }
        } catch (Exception e) {
            responseObserver.onNext(VerifyTokenResponse.newBuilder()
                    .setValid(false)
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserFromToken(GetUserFromTokenRequest request,
                                 StreamObserver<GetUserFromTokenResponse> responseObserver) {
        String token = request.getToken();
        BaseUser userData = jwtTokenProvider.getUserFromToken(token);

        if (userData != null) {
            UserData user = extractUserData(userData);

            GetUserFromTokenResponse response = GetUserFromTokenResponse.newBuilder()
                    .setUser(user)
                    .build();

            responseObserver.onNext(response);
        } else {
            GetUserFromTokenResponse response = GetUserFromTokenResponse.newBuilder()
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void validateAndExtract(ValidateAndExtractRequest request,
                                   StreamObserver<ValidateAndExtractResponse> responseObserver) {
        String token = request.getToken();
        BaseUser userData = jwtTokenProvider.getUserFromToken(token);

        if (userData != null) {
            UserData user = extractUserData(userData);

            ValidateAndExtractResponse response = ValidateAndExtractResponse.newBuilder()
                    .setValid(true)
                    .setUser(user)
                    .build();

            responseObserver.onNext(response);
        } else {
            ValidateAndExtractResponse response = ValidateAndExtractResponse.newBuilder()
                    .setValid(false)
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    private Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private UserData extractUserData(BaseUser baseUser) {
        UserIdentity userIdentity = UserIdentity.newBuilder()
                .setId(baseUser.getId().toString())
                .setEmail(baseUser.getEmail())
                .setFullName(baseUser.getFullName())
                .setPhoneNumber(baseUser.getPhoneNumber())
                .setRole(UserRole.valueOf(baseUser.getRole().name()))
                .setCreatedAt(convertLocalDateTimeToTimestamp(baseUser.getCreatedAt()))
                .setUpdatedAt(convertLocalDateTimeToTimestamp(baseUser.getUpdatedAt()))
                .build();

        return UserData.newBuilder()
                .setIdentity(userIdentity)
                .setProfile(baseUser.getProfile())
                .build();
    }
}
