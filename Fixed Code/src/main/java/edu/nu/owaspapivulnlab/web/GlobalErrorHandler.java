package edu.nu.owaspapivulnlab.web;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

// Task 8: Error Handling & Logging - Reduce error detail in production, custom exception mapping, secure logging
@ControllerAdvice
public class GlobalErrorHandler {
    // Task 8: Secure logger for server-side error tracking
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    // Task 8: Use property to determine if running in production
    @Value("${app.production:false}")
    private boolean productionMode;

    // Task 8: Custom exception mapping and secure error response
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> all(Exception e, HttpServletRequest request, HttpServletResponse response) {
        // Task 8: Log full error server-side with request/response/token info
        String token = request.getHeader("Authorization");
        log.error("[Task8] Error occurred: {}\nRequest: {} {}\nToken: {}\nResponseStatus: {}\nException: ",
                e.getMessage(), request.getMethod(), request.getRequestURI(), token, response.getStatus(), e);
        Map<String, String> errorMap = new HashMap<>();
        if (productionMode) {
            errorMap.put("error", "Internal server error");
        } else {
            errorMap.put("error", e.getClass().getName());
            errorMap.put("message", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMap);
    }

    // Task 8: Custom mapping for DB errors
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> db(DataAccessException e, HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("Authorization");
        log.error("[Task8] Database error: {}\nRequest: {} {}\nToken: {}\nResponseStatus: {}\nException: ",
                e.getMessage(), request.getMethod(), request.getRequestURI(), token, response.getStatus(), e);
        Map<String, String> errorMap = new HashMap<>();
        if (productionMode) {
            errorMap.put("error", "Database error");
        } else {
            errorMap.put("dbError", e.getMessage());
        }
        return ResponseEntity.status(500).body(errorMap);
    }
}
