package com.complainthub.controller;

import com.complainthub.entity.UserProfilePhoto;
import com.complainthub.service.UserProfilePhotoService;
import com.complainthub.service.UserProfilePhotoServiceImpl;
import com.complainthub.util.AuthorizationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;

@WebServlet("/api/users/*")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 5 * 1024 * 1024
)
public class UserProfilePhotoController extends HttpServlet {

    private final UserProfilePhotoService profilePhotoService =
            new UserProfilePhotoServiceImpl();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();

            long userId =
                    parseUserId(pathInfo);

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    userId
            );

            Part filePart = req.getPart("file");

            if (filePart == null
                    || filePart.getSize() <= 0) {

                throw new IllegalArgumentException(
                        "Profile photo file is required."
                );
            }

            String fileName =
                    filePart.getSubmittedFileName();

            String contentType =
                    filePart.getContentType();

            if (fileName == null
                    || fileName.isBlank()) {

                throw new IllegalArgumentException(
                        "File name is required."
                );
            }

            byte[] fileData;

            try (InputStream inputStream =
                         filePart.getInputStream()) {

                fileData = inputStream.readAllBytes();
            }

            UserProfilePhoto profilePhoto =
                    profilePhotoService.uploadProfilePhoto(
                            userId,
                            fileData,
                            fileName,
                            contentType
                    );

            res.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            res.getWriter().println(
                    "Profile photo uploaded successfully."
            );

            res.getWriter().println(
                    "Profile Photo ID: "
                            + profilePhoto.getId()
            );

            res.getWriter().println(
                    "User ID: " + userId
            );

            res.getWriter().println(
                    "File Name: "
                            + profilePhoto.getFileName()
            );

            res.getWriter().println(
                    "Content Type: "
                            + profilePhoto.getContentType()
            );

            res.getWriter().println(
                    "File Size: "
                            + profilePhoto.getFileSize()
            );

        } catch (SecurityException e) {

            res.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            res.getWriter().println(
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            res.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            res.getWriter().println(
                    "Profile photo upload failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            res.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            res.getWriter().println(
                    "Unable to upload profile photo."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();

            long userId =
                    parseUserId(pathInfo);

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    userId
            );

            UserProfilePhoto profilePhoto =
                    profilePhotoService.getProfilePhotoByUser(
                            userId
                    );

            if (profilePhoto == null) {

                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Profile photo not found."
                );

                return;
            }

            res.setStatus(
                    HttpServletResponse.SC_OK
            );

            res.getWriter().println(
                    "Profile Photo ID: "
                            + profilePhoto.getId()
            );

            res.getWriter().println(
                    "User ID: " + userId
            );

            res.getWriter().println(
                    "File Name: "
                            + profilePhoto.getFileName()
            );

            res.getWriter().println(
                    "Stored File Name: "
                            + profilePhoto.getStoredFileName()
            );

            res.getWriter().println(
                    "File Path: "
                            + profilePhoto.getFilePath()
            );

            res.getWriter().println(
                    "Content Type: "
                            + profilePhoto.getContentType()
            );

            res.getWriter().println(
                    "File Size: "
                            + profilePhoto.getFileSize()
            );

            res.getWriter().println(
                    "Created At: "
                            + profilePhoto.getCreatedAt()
            );

            res.getWriter().println(
                    "Updated At: "
                            + profilePhoto.getUpdatedAt()
            );

        } catch (SecurityException e) {

            res.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            res.getWriter().println(
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            res.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            res.getWriter().println(
                    "Invalid request: " + e.getMessage()
            );

        } catch (Exception e) {

            res.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            res.getWriter().println(
                    "Unable to retrieve profile photo."
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();

            long userId =
                    parseUserId(pathInfo);

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    userId
            );

            UserProfilePhoto profilePhoto =
                    profilePhotoService.getProfilePhotoByUser(
                            userId
                    );

            if (profilePhoto == null) {

                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Profile photo not found."
                );

                return;
            }

            boolean deleted =
                    profilePhotoService.deleteProfilePhoto(
                            profilePhoto.getId()
                    );

            if (!deleted) {

                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Profile photo not found."
                );

                return;
            }

            res.setStatus(
                    HttpServletResponse.SC_OK
            );

            res.getWriter().println(
                    "Profile photo deleted successfully."
            );

            res.getWriter().println(
                    "User ID: " + userId
            );

        } catch (SecurityException e) {

            res.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            res.getWriter().println(
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            res.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            res.getWriter().println(
                    "Profile photo deletion failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            res.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            res.getWriter().println(
                    "Unable to delete profile photo."
            );
        }
    }

    private long parseUserId(String pathInfo) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || pathInfo.length() <= 1) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        String value = pathInfo.substring(1);

        /*
         * Expected:
         *
         * /{userId}/profile-photo
         */
        if (!value.endsWith("/profile-photo")) {

            throw new IllegalArgumentException(
                    "Invalid profile photo endpoint."
            );
        }

        String userIdValue =
                value.substring(
                        0,
                        value.length()
                                - "/profile-photo".length()
                );

        if (userIdValue.isBlank()
                || userIdValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        try {

            return Long.parseLong(userIdValue);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "User ID must be a valid number."
            );
        }
    }
}