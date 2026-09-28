package com.complainthub.controller;

import com.complainthub.entity.Complaint;
import com.complainthub.entity.ComplaintUpdate;
import com.complainthub.entity.User;
import com.complainthub.service.ComplaintService;
import com.complainthub.service.ComplaintServiceImpl;
import com.complainthub.service.ComplaintUpdateService;
import com.complainthub.service.ComplaintUpdateServiceImpl;
import com.complainthub.util.AuthenticationConstants;
import com.complainthub.util.AuthorizationException;
import com.complainthub.util.AuthorizationUtil;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/complaint-updates/*")
public class ComplaintUpdateController extends HttpServlet {

    private final ComplaintUpdateService complaintUpdateService =
            new ComplaintUpdateServiceImpl();

    private final ComplaintService complaintService =
            new ComplaintServiceImpl();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            long authenticatedUserId =
                    getAuthenticatedUserId(request);

            long complaintId =
                    extractComplaintId(request);

            String message =
                    request.getParameter("message");

            String visibleToUserParameter =
                    request.getParameter("visibleToUser");

            boolean visibleToUser =
                    parseVisibleToUser(
                            visibleToUserParameter
                    );

            User updatedBy =
                    new User();

            updatedBy.setId(
                    authenticatedUserId
            );

            Complaint complaint =
                    new Complaint();

            complaint.setId(
                    complaintId
            );

            ComplaintUpdate complaintUpdate =
                    new ComplaintUpdate();

            complaintUpdate.setComplaint(
                    complaint
            );

            complaintUpdate.setUpdatedBy(
                    updatedBy
            );

            complaintUpdate.setMessage(
                    message
            );

            complaintUpdate.setVisibleToUser(
                    visibleToUser
            );

            ComplaintUpdate createdUpdate =
                    complaintUpdateService.createUpdate(
                            complaintUpdate
                    );

            ResponseUtil.sendSuccess(
                    response,
                    HttpServletResponse.SC_CREATED,
                    "Complaint update created successfully.",
                    toUpdateResponse(
                            createdUpdate
                    )
            );

        } catch (AuthorizationException exception) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    exception.getMessage()
            );

        } catch (IllegalArgumentException exception) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    exception.getMessage()
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create complaint update."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            long authenticatedUserId =
                    getAuthenticatedUserId(request);

            long complaintId =
                    extractComplaintId(request);

            Complaint complaint =
                    complaintService.getComplaintById(
                            complaintId
                    );

            if (complaint == null) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Complaint not found."
                );

                return;
            }

            if (AuthorizationUtil.isAdmin(request)) {

                List<ComplaintUpdate> updates =
                        complaintUpdateService
                                .getUpdatesByComplaint(
                                        complaintId
                                );

                writeUpdates(
                        updates,
                        response
                );

                return;
            }

            AuthorizationUtil.requireOwnerOrAdmin(
                    request,
                    complaint.getUser().getId()
            );

            List<ComplaintUpdate> updates =
                    complaintUpdateService
                            .getVisibleUpdatesByComplaint(
                                    complaintId
                            );

            writeUpdates(
                    updates,
                    response
            );

        } catch (AuthorizationException exception) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    exception.getMessage()
            );

        } catch (IllegalArgumentException exception) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    exception.getMessage()
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve complaint updates."
            );
        }
    }

    private void writeUpdates(
            List<ComplaintUpdate> updates,
            HttpServletResponse response
    ) throws IOException {

        List<Map<String, Object>> updateResponses =
                new ArrayList<>();

        if (updates != null) {

            for (ComplaintUpdate update : updates) {

                updateResponses.add(
                        toUpdateResponse(update)
                );
            }
        }

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "total",
                updateResponses.size()
        );

        data.put(
                "updates",
                updateResponses
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                updateResponses.isEmpty()
                        ? "No complaint updates found."
                        : "Complaint updates retrieved successfully.",
                data
        );
    }

    private Map<String, Object> toUpdateResponse(
            ComplaintUpdate complaintUpdate
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                complaintUpdate.getId()
        );

        if (complaintUpdate.getComplaint() != null) {

            data.put(
                    "complaintId",
                    complaintUpdate
                            .getComplaint()
                            .getId()
            );

        } else {

            data.put(
                    "complaintId",
                    null
            );
        }

        if (complaintUpdate.getUpdatedBy() != null) {

            Map<String, Object> updatedBy =
                    new LinkedHashMap<>();

            updatedBy.put(
                    "id",
                    complaintUpdate
                            .getUpdatedBy()
                            .getId()
            );

            updatedBy.put(
                    "name",
                    complaintUpdate
                            .getUpdatedBy()
                            .getName()
            );

            updatedBy.put(
                    "email",
                    complaintUpdate
                            .getUpdatedBy()
                            .getEmail()
            );

            updatedBy.put(
                    "role",
                    complaintUpdate
                            .getUpdatedBy()
                            .getRole()
            );

            data.put(
                    "updatedBy",
                    updatedBy
            );

        } else {

            data.put(
                    "updatedBy",
                    null
            );
        }

        data.put(
                "message",
                complaintUpdate.getMessage()
        );

        data.put(
                "visibleToUser",
                complaintUpdate.getVisibleToUser()
        );

        data.put(
                "createdAt",
                complaintUpdate.getCreatedAt()
        );

        return data;
    }

    private long getAuthenticatedUserId(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            throw new IllegalArgumentException(
                    "Authenticated session is required."
            );
        }

        Object userId =
                session.getAttribute(
                        AuthenticationConstants.USER_ID
                );

        if (userId == null) {

            throw new IllegalArgumentException(
                    "Authenticated User ID is missing."
            );
        }

        try {

            long parsedUserId =
                    Long.parseLong(
                            userId.toString()
                    );

            if (parsedUserId <= 0) {

                throw new IllegalArgumentException(
                        "Authenticated User ID is invalid."
                );
            }

            return parsedUserId;

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "Authenticated User ID is invalid."
            );
        }
    }

    private long extractComplaintId(
            HttpServletRequest request
    ) {

        String pathInfo =
                request.getPathInfo();

        /*
         * Expected path:
         *
         * /complaint/42
         */
        if (pathInfo == null
                || pathInfo.isBlank()
                || pathInfo.equals("/")) {

            throw new IllegalArgumentException(
                    "Complaint update path is required."
            );
        }

        String[] pathParts =
                pathInfo.split("/");

        /*
         * pathParts[0] = ""
         * pathParts[1] = "complaint"
         * pathParts[2] = "42"
         */
        if (pathParts.length != 3
                || !"complaint".equalsIgnoreCase(
                pathParts[1]
        )) {

            throw new IllegalArgumentException(
                    "Invalid complaint update URL."
            );
        }

        return parseId(
                pathParts[2],
                "Complaint ID"
        );
    }

    private long parseId(
            String value,
            String fieldName
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        try {

            long id =
                    Long.parseLong(value);

            if (id <= 0) {

                throw new IllegalArgumentException(
                        fieldName
                                + " must be greater than zero."
                );
            }

            return id;

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must be a valid number."
            );
        }
    }

    private boolean parseVisibleToUser(
            String value
    ) {

        /*
         * If the parameter is omitted, the update is
         * not visible to normal users by default.
         */
        if (value == null
                || value.isBlank()) {

            return false;
        }

        if ("true".equalsIgnoreCase(value)
                || "1".equals(value)) {

            return true;
        }

        if ("false".equalsIgnoreCase(value)
                || "0".equals(value)) {

            return false;
        }

        throw new IllegalArgumentException(
                "Visible To User must be true or false."
        );
    }
}