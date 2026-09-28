package com.complainthub.controller;

import com.complainthub.entity.UserProfilePhoto;
import com.complainthub.service.UserProfilePhotoService;
import com.complainthub.service.UserProfilePhotoServiceImpl;
import com.complainthub.util.AuthorizationUtil;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

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

        try {
            String pathInfo =
                    req.getPathInfo();

            long userId =
                    parseUserId(pathInfo);

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    userId
            );

            Part filePart =
                    req.getPart("file");

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

                fileData =
                        inputStream.readAllBytes();
            }

            UserProfilePhoto profilePhoto =
                    profilePhotoService.uploadProfilePhoto(
                            userId,
                            fileData,
                            fileName,
                            contentType
                    );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_CREATED,
                    "Profile photo uploaded successfully.",
                    toProfilePhotoResponse(
                            profilePhoto
                    )
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Profile photo upload failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to upload profile photo.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to upload profile photo."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        try {
            String pathInfo =
                    req.getPathInfo();

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

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Profile photo not found."
                );

                return;
            }

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Profile photo retrieved successfully.",
                    toProfilePhotoResponse(
                            profilePhoto
                    )
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request: " + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to retrieve profile photo.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve profile photo."
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        try {
            String pathInfo =
                    req.getPathInfo();

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

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Profile photo not found."
                );

                return;
            }

            boolean deleted =
                    profilePhotoService.deleteProfilePhoto(
                            profilePhoto.getId()
                    );

            if (!deleted) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Profile photo not found."
                );

                return;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "userId",
                    userId
            );

            data.put(
                    "profilePhotoId",
                    profilePhoto.getId()
            );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Profile photo deleted successfully.",
                    data
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Profile photo deletion failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to delete profile photo.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to delete profile photo."
            );
        }
    }

    private Map<String, Object> toProfilePhotoResponse(
            UserProfilePhoto profilePhoto
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                profilePhoto.getId()
        );

        data.put(
                "userId",
                profilePhoto.getUser()
        );

        data.put(
                "fileName",
                profilePhoto.getFileName()
        );

        data.put(
                "storedFileName",
                profilePhoto.getStoredFileName()
        );

        data.put(
                "filePath",
                profilePhoto.getFilePath()
        );

        data.put(
                "contentType",
                profilePhoto.getContentType()
        );

        data.put(
                "fileSize",
                profilePhoto.getFileSize()
        );

        data.put(
                "createdAt",
                profilePhoto.getCreatedAt()
        );

        data.put(
                "updatedAt",
                profilePhoto.getUpdatedAt()
        );

        return data;
    }

    private long parseUserId(
            String pathInfo
    ) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || pathInfo.length() <= 1) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        String value =
                pathInfo.substring(1);

        /*
         * Expected:
         *
         * /{userId}/profile-photo
         */
        if (!value.endsWith(
                "/profile-photo"
        )) {

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

            long userId =
                    Long.parseLong(userIdValue);

            if (userId <= 0) {

                throw new IllegalArgumentException(
                        "User ID must be greater than zero."
                );
            }

            return userId;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "User ID must be a valid number."
            );
        }
    }
}