package com.zestia.datn.zestia.config;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.QueryTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static final String CONCURRENT_ORDER_MESSAGE =
            "Hệ thống đang xử lý đơn hàng hoặc voucher khác cùng lúc. Vui lòng thử lại sau vài giây.";

    @ExceptionHandler({
            CannotAcquireLockException.class,
            PessimisticLockingFailureException.class,
            LockTimeoutException.class,
            PessimisticLockException.class,
            QueryTimeoutException.class
    })
    public ResponseEntity<Map<String, Object>> handleOrderConcurrency(Exception ex) {
        log.warn("Concurrent order lock/deadlock conflict: {}", rootMessage(ex));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("code", "ORDER_CONCURRENCY_CONFLICT");
        body.put("retryable", true);
        body.put("error", CONCURRENT_ORDER_MESSAGE);
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataConflict(DataIntegrityViolationException ex) {
        log.warn("Database uniqueness/integrity conflict: {}", rootMessage(ex));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("code", "DATA_CONFLICT");
        body.put("retryable", false);
        body.put("error", "Dữ liệu vừa được xử lý bởi một yêu cầu khác. Vui lòng tải lại trang để xem kết quả mới nhất.");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessValidation(IllegalArgumentException ex) {
        return businessError(HttpStatus.BAD_REQUEST, "BUSINESS_VALIDATION", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessStateConflict(IllegalStateException ex) {
        return businessError(HttpStatus.CONFLICT, "BUSINESS_STATE_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason() != null ? ex.getReason() : "Yêu cầu không hợp lệ");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(Exception ex) {
        log.debug("Invalid API request: {}", rootMessage(ex));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("code", "INVALID_REQUEST");
        body.put("error", "Dữ liệu gửi lên không đúng định dạng hoặc còn thiếu thông tin bắt buộc.");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
        body.put("code", "UPLOAD_TOO_LARGE");
        body.put("error", "Tệp tải lên vượt quá dung lượng cho phép.");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }

    private ResponseEntity<Map<String, Object>> businessError(HttpStatus status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("code", code);
        body.put("retryable", false);
        body.put("error", message != null && !message.isBlank() ? message : "Yêu cầu không hợp lệ");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }

    private String rootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : throwable.getClass().getSimpleName();
    }
}
