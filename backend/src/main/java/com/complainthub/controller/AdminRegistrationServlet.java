package com.complainthub.controller;

import com.complainthub.entity.User;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.service.UserService;
import com.complainthub.service.UserServiceImpl;
import com.complainthub.util.AuthorizationUtil;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/admin/users/register")
public class AdminRegistrationServlet extends HttpServlet {

    private final UserService userService =
            new UserServiceImpl();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    req,
                    UserRole.ADMIN
            );

            String name =
                    req.getParameter("name");

            String email =
                    req.getParameter("email");

            String password =
                    req.getParameter("password");

            String roleParameter =
                    req.getParameter("role");

            if (roleParameter == null
                    || roleParameter.isBlank()) {

                throw new IllegalArgumentException(
                        "Role is required."
                );
            }

            UserRole requestedRole;

            try {
                requestedRole =
                        UserRole.valueOf(
                                roleParameter
                                        .trim()
                                        .toUpperCase()
                        );

            } catch (IllegalArgumentException e) {

                throw new IllegalArgumentException(
                        "Invalid user role."
                );
            }

            /*
             * This endpoint can only create
             * ADMIN or AGENT accounts.
             */
            if (requestedRole != UserRole.ADMIN
                    && requestedRole != UserRole.AGENT) {

                throw new IllegalArgumentException(
                        "Only ADMIN or AGENT accounts "
                                + "can be created through "
                                + "this endpoint."
                );
            }

            User user = new User();

            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(requestedRole);

            User createdUser =
                    userService.createUser(user);

            Map<String, Object> userData =
                    new LinkedHashMap<>();

            userData.put(
                    "id",
                    createdUser.getId()
            );

            userData.put(
                    "name",
                    createdUser.getName()
            );

            userData.put(
                    "email",
                    createdUser.getEmail()
            );

            userData.put(
                    "role",
                    createdUser.getRole()
            );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_CREATED,
                    "User registered successfully.",
                    userData
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: "
                            + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Registration failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to register user.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to register user."
            );
        }
    }
}