package id.ac.ui.cs.advprog.kilimanjaro.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class GenericResponse<T> {
    private final BaseResponse meta;
    private final T data;

    public GenericResponse(boolean success, String message, T data) {
        this.meta = new BaseResponse(success, message);
        this.data = data;
    }

    public String getMessage() {
        return meta.getMessage();
    }

    public boolean isSuccess() {
        return meta.isSuccess();
    }

    public Instant getTimestamp() {
        return meta.getTimestamp();
    }
}

@Getter
class BaseResponse {
    private final boolean success;
    private final String message;
    private final Instant timestamp;

    public BaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }
}
