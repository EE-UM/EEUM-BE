package com.eeum.global.logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingFilter implements Filter {

  public static final String REGEX = "(\"(?:idToken|accessToken|refreshToken|token|password|deviceId)\"\\s*:\\s*\")[^\"]*(\")";
  private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
      REGEX,
      Pattern.CASE_INSENSITIVE
  );

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest httpServletRequest) {
      CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(
          httpServletRequest);

      String url = wrappedRequest.getRequestURI();
      String method = wrappedRequest.getMethod();
      String body = wrappedRequest.getReader().lines().reduce("", String::concat);
      log.info("Incoming Request: URL={}, Method={}, Body={}", url, method,
          maskSensitiveData(body));

      chain.doFilter(wrappedRequest, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  String maskSensitiveData(String body) {
    return SENSITIVE_PATTERN.matcher(body).replaceAll("$1***$2");
  }
}
