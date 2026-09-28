package com.complainthub.controller;

import com.complainthub.entity.User;
import com.complainthub.service.AuthenticationService;
import com.complainthub.service.AuthenticationServiceImpl;
import com.complainthub.util.AuthenticationConstants;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {

    private final AuthenticationService authenticationService =
            new AuthenticationServiceImpl();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        String email =
                req.getParameter("email");

        String password =
                req.getParameter("password");

        try {
            User authUser =
                    authenticationService.authenticate(
                            email,
                            password
                    );

            HttpSession session =
                    req.getSession();

            session.setAttribute(
                    AuthenticationConstants.USER_ID,
                    authUser.getId()
            );

            session.setAttribute(
                    AuthenticationConstants.USER_ROLE,
                    authUser.getRole()
            );

            Map<String, Object> userData =
                    new LinkedHashMap<>();

            userData.put(
                    "id",
                    authUser.getId()
            );

            userData.put(
                    "name",
                    authUser.getName()
            );

            userData.put(
                    "email",
                    authUser.getEmail()
            );

            userData.put(
                    "role",
                    authUser.getRole()
            );

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "user",
                    userData
            );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Login successful.",
                    data
            );

        } catch (Exception e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid email or password."
            );
        }
    }
}