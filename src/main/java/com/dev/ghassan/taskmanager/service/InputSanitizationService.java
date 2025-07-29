package com.dev.ghassan.taskmanager.service;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InputSanitizationService {
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE);

    public String sanitizeString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        // Remove script tags
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        
        // Remove HTML tags
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        
        // HTML encode to prevent XSS
        sanitized = Encode.forHtml(sanitized);
        
        // Remove potential SQL injection characters
        sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");
        
        return sanitized.trim();
    }

    public List<String> sanitizeStringList(List<String> inputs) {
        if (inputs == null) {
            return null;
        }
        
        return inputs.stream()
                .map(this::sanitizeString)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());
    }
}