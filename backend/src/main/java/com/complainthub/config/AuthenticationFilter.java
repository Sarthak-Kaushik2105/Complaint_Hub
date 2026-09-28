package com.complainthub.config;

import com.complainthub.util.AuthenticationConstants;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/api/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;

        String requestUri = httpRequest.getRequestURI();

        if (isPublicEndpoint(requestUri)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            sendUnauthorizedResponse(httpResponse);
            return;
        }

        Object userId =
                session.getAttribute(
                        AuthenticationConstants.USER_ID
                );

        Object userRole =
                session.getAttribute(
                        AuthenticationConstants.USER_ROLE
                );

        if (userId == null || userRole == null) {
            sendUnauthorizedResponse(
                    httpResponse
            );
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isPublicEndpoint(
            String requestUri
    ) {
        return requestUri.endsWith("/api/auth/login") || requestUri.endsWith("/api/auth/logout"
        ) || requestUri.endsWith("/api/auth/register");
    }

    private void sendUnauthorizedResponse(
            HttpServletResponse res
    ) throws IOException {
        ResponseUtil.sendError(
                res,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required."
        );
    }
}