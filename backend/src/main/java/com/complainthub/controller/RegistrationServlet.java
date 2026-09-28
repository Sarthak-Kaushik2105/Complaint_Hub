package com.complainthub.controller;

import com.complainthub.entity.User;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.service.UserService;
import com.complainthub.service.UserServiceImpl;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/register")
public class RegistrationServlet extends HttpServlet {

    private final UserService userService =
            new UserServiceImpl();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        String name =
                req.getParameter("name");

        String email =
                req.getParameter("email");

        String password =
                req.getParameter("password");

        try {
            User user = new User();

            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);

            /*
             * Public registration can only
             * create normal USER accounts.
             */
            user.setRole(UserRole.USER);

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
                    "Registration successful.",
                    userData
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