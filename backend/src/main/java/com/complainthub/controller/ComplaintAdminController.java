package com.complainthub.controller;

import com.complainthub.entity.Complaint;
import com.complainthub.entity.enums.ComplaintPriority;
import com.complainthub.entity.enums.ComplaintStatus;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.service.ComplaintService;
import com.complainthub.service.ComplaintServiceImpl;
import com.complainthub.util.AuthorizationException;
import com.complainthub.util.AuthorizationUtil;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/complaints/*")
public class ComplaintAdminController extends HttpServlet {

    private final ComplaintService complaintService =
            new ComplaintServiceImpl();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.isBlank()
                    || pathInfo.equals("/")) {

                getComplaints(request, response);
                return;
            }

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Unknown admin complaint operation"
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
                    "An unexpected error occurred"
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.isBlank()) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid complaint path"
                );

                return;
            }

            if (pathInfo.matches("/\\d+/status")) {

                updateComplaintStatus(
                        request,
                        response
                );

                return;
            }

            if (pathInfo.matches("/\\d+/priority")) {

                updateComplaintPriority(
                        request,
                        response
                );

                return;
            }

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Unknown admin complaint operation"
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
                    "An unexpected error occurred"
            );
        }
    }

    private void getComplaints(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        AuthorizationUtil.requireRole(
                request,
                UserRole.ADMIN
        );

        String statusParameter =
                request.getParameter("status");

        String priorityParameter =
                request.getParameter("priority");

        String categoryIdParameter =
                request.getParameter("categoryId");

        ComplaintStatus status = null;
        ComplaintPriority priority = null;
        Long categoryId = null;

        if (statusParameter != null
                && !statusParameter.isBlank()) {

            try {
                status = ComplaintStatus.valueOf(
                        statusParameter
                                .trim()
                                .toUpperCase()
                );

            } catch (IllegalArgumentException exception) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid complaint status"
                );

                return;
            }
        }

        if (priorityParameter != null
                && !priorityParameter.isBlank()) {

            try {
                priority = ComplaintPriority.valueOf(
                        priorityParameter
                                .trim()
                                .toUpperCase()
                );

            } catch (IllegalArgumentException exception) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid complaint priority"
                );

                return;
            }
        }

        if (categoryIdParameter != null
                && !categoryIdParameter.isBlank()) {

            categoryId = parseId(
                    categoryIdParameter,
                    "Category ID"
            );
        }

        List<Complaint> complaints =
                complaintService.getAllComplaints();

        if (status != null) {

            ComplaintStatus selectedStatus =
                    status;

            complaints = complaints.stream()
                    .filter(complaint ->
                            complaint.getStatus()
                                    == selectedStatus
                    )
                    .toList();
        }

        if (priority != null) {

            ComplaintPriority selectedPriority =
                    priority;

            complaints = complaints.stream()
                    .filter(complaint ->
                            complaint.getPriority()
                                    == selectedPriority
                    )
                    .toList();
        }

        if (categoryId != null) {

            Long selectedCategoryId =
                    categoryId;

            complaints = complaints.stream()
                    .filter(complaint ->
                            complaint.getCategory() != null
                                    && complaint.getCategory().getId()
                                    == selectedCategoryId
                    )
                    .toList();
        }

        if (complaints == null) {
            complaints = new ArrayList<>();
        }

        List<Map<String, Object>> complaintResponses =
                complaints.stream()
                        .map(this::toComplaintResponse)
                        .toList();

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "total",
                complaintResponses.size()
        );

        data.put(
                "complaints",
                complaintResponses
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                complaintResponses.isEmpty()
                        ? "No complaints found."
                        : "Complaints retrieved successfully.",
                data
        );
    }

    private void updateComplaintStatus(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        AuthorizationUtil.requireRole(
                request,
                UserRole.ADMIN
        );

        long complaintId =
                extractIdFromPath(
                        request,
                        "status"
                );

        Map<String, String> formParameters =
                readFormParameters(request);

        String statusParameter =
                formParameters.get("status");

        if (statusParameter == null
                || statusParameter.isBlank()) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Status is required"
            );

            return;
        }

        ComplaintStatus status;

        try {

            status = ComplaintStatus.valueOf(
                    statusParameter
                            .trim()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid complaint status"
            );

            return;
        }

        Complaint updatedComplaint =
                complaintService.updateStatus(
                        complaintId,
                        status
                );

        if (updatedComplaint == null) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Complaint not found"
            );

            return;
        }

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                "Complaint status updated successfully.",
                toComplaintResponse(
                        updatedComplaint
                )
        );
    }

    private void updateComplaintPriority(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        AuthorizationUtil.requireRole(
                request,
                UserRole.ADMIN
        );

        long complaintId =
                extractIdFromPath(
                        request,
                        "priority"
                );

        Map<String, String> formParameters =
                readFormParameters(request);

        String priorityParameter =
                formParameters.get("priority");

        if (priorityParameter == null
                || priorityParameter.isBlank()) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Priority is required"
            );

            return;
        }

        ComplaintPriority priority;

        try {

            priority = ComplaintPriority.valueOf(
                    priorityParameter
                            .trim()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid complaint priority"
            );

            return;
        }

        Complaint updatedComplaint =
                complaintService.updatePriority(
                        complaintId,
                        priority
                );

        if (updatedComplaint == null) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Complaint not found"
            );

            return;
        }

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                "Complaint priority updated successfully.",
                toComplaintResponse(
                        updatedComplaint
                )
        );
    }

    private long extractIdFromPath(
            HttpServletRequest request,
            String operation
    ) {

        String pathInfo =
                request.getPathInfo();

        if (pathInfo == null
                || pathInfo.isBlank()) {

            throw new IllegalArgumentException(
                    "Complaint path is required"
            );
        }

        String suffix =
                "/" + operation;

        if (!pathInfo.startsWith("/")
                || !pathInfo.endsWith(suffix)) {

            throw new IllegalArgumentException(
                    "Invalid complaint path"
            );
        }

        String idPart =
                pathInfo.substring(
                        1,
                        pathInfo.length()
                                - suffix.length()
                );

        return parseId(
                idPart,
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
                    fieldName + " is required"
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

    private Map<String, String> readFormParameters(
            HttpServletRequest request
    ) throws IOException {

        Map<String, String> parameters =
                new HashMap<>();

        String body =
                request.getReader()
                        .lines()
                        .reduce(
                                "",
                                (current, next) ->
                                        current + next
                        );

        if (body.isBlank()) {
            return parameters;
        }

        String[] pairs =
                body.split("&");

        for (String pair : pairs) {

            String[] keyValue =
                    pair.split("=", 2);

            String key =
                    URLDecoder.decode(
                            keyValue[0],
                            StandardCharsets.UTF_8
                    );

            String value = "";

            if (keyValue.length > 1) {

                value =
                        URLDecoder.decode(
                                keyValue[1],
                                StandardCharsets.UTF_8
                        );
            }

            parameters.put(
                    key,
                    value
            );
        }

        return parameters;
    }

    private Map<String, Object> toComplaintResponse(
            Complaint complaint
    ) {

        Map<String, Object> complaintData =
                new LinkedHashMap<>();

        complaintData.put(
                "id",
                complaint.getId()
        );

        complaintData.put(
                "title",
                complaint.getTitle()
        );

        complaintData.put(
                "description",
                complaint.getDescription()
        );

        complaintData.put(
                "status",
                complaint.getStatus()
        );

        complaintData.put(
                "priority",
                complaint.getPriority()
        );

        complaintData.put(
                "wasResolved",
                complaint.isWasResolved()
        );

        complaintData.put(
                "createdAt",
                complaint.getCreatedAt()
        );

        complaintData.put(
                "updatedAt",
                complaint.getUpdatedAt()
        );

        if (complaint.getUser() != null) {

            Map<String, Object> userData =
                    new LinkedHashMap<>();

            userData.put(
                    "id",
                    complaint.getUser().getId()
            );

            userData.put(
                    "name",
                    complaint.getUser().getName()
            );

            userData.put(
                    "email",
                    complaint.getUser().getEmail()
            );

            userData.put(
                    "role",
                    complaint.getUser().getRole()
            );

            complaintData.put(
                    "user",
                    userData
            );

        } else {

            complaintData.put(
                    "user",
                    null
            );
        }

        if (complaint.getCategory() != null) {

            Map<String, Object> categoryData =
                    new LinkedHashMap<>();

            categoryData.put(
                    "id",
                    complaint.getCategory().getId()
            );

            categoryData.put(
                    "name",
                    complaint.getCategory().getName()
            );

            categoryData.put(
                    "description",
                    complaint.getCategory().getDescription()
            );

            complaintData.put(
                    "category",
                    categoryData
            );

        } else {

            complaintData.put(
                    "category",
                    null
            );
        }

        return complaintData;
    }
}