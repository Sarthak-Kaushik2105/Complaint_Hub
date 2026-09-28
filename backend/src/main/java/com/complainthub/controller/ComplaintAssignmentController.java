package com.complainthub.controller;

import com.complainthub.entity.Complaint;
import com.complainthub.entity.ComplaintAssignment;
import com.complainthub.entity.User;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.service.ComplaintAssignmentService;
import com.complainthub.service.ComplaintAssignmentServiceImpl;
import com.complainthub.util.AuthenticationConstants;
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

@WebServlet("/api/admin/assignments/*")
public class ComplaintAssignmentController extends HttpServlet {

    private ComplaintAssignmentService complaintAssignmentService;

    @Override
    public void init() throws ServletException {
        complaintAssignmentService =
                new ComplaintAssignmentServiceImpl();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    request,
                    UserRole.ADMIN
            );

            String complaintIdParameter =
                    request.getParameter("complaintId");

            String agentIdParameter =
                    request.getParameter("agentId");

            if (complaintIdParameter == null
                    || complaintIdParameter.isBlank()) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Complaint ID is required."
                );
                return;
            }

            if (agentIdParameter == null
                    || agentIdParameter.isBlank()) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Agent ID is required."
                );
                return;
            }

            long complaintId =
                    parseId(
                            complaintIdParameter,
                            "Complaint ID"
                    );

            long agentId =
                    parseId(
                            agentIdParameter,
                            "Agent ID"
                    );

            long adminId =
                    getLoggedInUserId(request);

            Complaint complaint =
                    new Complaint();

            complaint.setId(
                    complaintId
            );

            User agent =
                    new User();

            agent.setId(
                    agentId
            );

            User assignedBy =
                    new User();

            assignedBy.setId(
                    adminId
            );

            ComplaintAssignment assignment =
                    new ComplaintAssignment();

            assignment.setComplaint(
                    complaint
            );

            assignment.setAgent(
                    agent
            );

            assignment.setAssignedBy(
                    assignedBy
            );

            assignment.setActive(
                    true
            );

            ComplaintAssignment createdAssignment =
                    complaintAssignmentService
                            .createAssignment(
                                    assignment
                            );

            ResponseUtil.sendSuccess(
                    response,
                    HttpServletResponse.SC_CREATED,
                    "Complaint assigned successfully.",
                    toAssignmentResponse(
                            createdAssignment
                    )
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Failed to create complaint assignment.",
                    e
            );

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create complaint assignment."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    request,
                    UserRole.ADMIN
            );

            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.equals("/")) {

                getAllAssignments(
                        response
                );
                return;
            }

            if (pathInfo.startsWith("/complaint/")) {

                long complaintId =
                        extractId(
                                pathInfo,
                                "/complaint/"
                        );

                getAssignmentsByComplaint(
                        complaintId,
                        response
                );
                return;
            }

            if (pathInfo.startsWith("/agent/")) {

                long agentId =
                        extractId(
                                pathInfo,
                                "/agent/"
                        );

                getAssignmentsByAgent(
                        agentId,
                        response
                );
                return;
            }

            long assignmentId =
                    extractId(
                            pathInfo,
                            "/"
                    );

            getAssignmentById(
                    assignmentId,
                    response
            );

        } catch (NumberFormatException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID must be a valid number."
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Failed to retrieve complaint assignments.",
                    e
            );

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve complaint assignments."
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    request,
                    UserRole.ADMIN
            );

            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.equals("/")) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Assignment ID is required."
                );
                return;
            }

            long assignmentId =
                    extractId(
                            pathInfo,
                            "/"
                    );

            ComplaintAssignment assignment =
                    complaintAssignmentService
                            .getAssignmentById(
                                    assignmentId
                            );

            if (assignment == null) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Assignment not found."
                );
                return;
            }

            String activeParameter =
                    request.getParameter("active");

            if (activeParameter != null) {

                if (!"true".equalsIgnoreCase(
                        activeParameter
                )
                        && !"false".equalsIgnoreCase(
                        activeParameter
                )) {

                    ResponseUtil.sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Active must be true or false."
                    );
                    return;
                }

                assignment.setActive(
                        Boolean.parseBoolean(
                                activeParameter
                        )
                );
            }

            String agentIdParameter =
                    request.getParameter("agentId");

            if (agentIdParameter != null
                    && !agentIdParameter.isBlank()) {

                long agentId =
                        parseId(
                                agentIdParameter,
                                "Agent ID"
                        );

                User agent =
                        new User();

                agent.setId(
                        agentId
                );

                assignment.setAgent(
                        agent
                );
            }

            String complaintIdParameter =
                    request.getParameter("complaintId");

            if (complaintIdParameter != null
                    && !complaintIdParameter.isBlank()) {

                long complaintId =
                        parseId(
                                complaintIdParameter,
                                "Complaint ID"
                        );

                Complaint complaint =
                        new Complaint();

                complaint.setId(
                        complaintId
                );

                assignment.setComplaint(
                        complaint
                );
            }

            ComplaintAssignment updatedAssignment =
                    complaintAssignmentService
                            .updateAssignment(
                                    assignment
                            );

            ResponseUtil.sendSuccess(
                    response,
                    HttpServletResponse.SC_OK,
                    "Assignment updated successfully.",
                    toAssignmentResponse(
                            updatedAssignment
                    )
            );

        } catch (NumberFormatException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID must be a valid number."
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Failed to update complaint assignment.",
                    e
            );

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to update complaint assignment."
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    request,
                    UserRole.ADMIN
            );

            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.equals("/")) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Assignment ID is required."
                );
                return;
            }

            long assignmentId =
                    extractId(
                            pathInfo,
                            "/"
                    );

            boolean deactivated =
                    complaintAssignmentService
                            .deactivateAssignment(
                                    assignmentId
                            );

            if (!deactivated) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Assignment not found."
                );
                return;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "assignmentId",
                    assignmentId
            );

            data.put(
                    "active",
                    false
            );

            ResponseUtil.sendSuccess(
                    response,
                    HttpServletResponse.SC_OK,
                    "Assignment deactivated successfully.",
                    data
            );

        } catch (NumberFormatException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Assignment ID must be a valid number."
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Failed to deactivate complaint assignment.",
                    e
            );

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to deactivate complaint assignment."
            );
        }
    }

    private void getAllAssignments(
            HttpServletResponse response
    ) throws IOException {

        List<ComplaintAssignment> assignments =
                complaintAssignmentService
                        .getAllAssignments();

        writeAssignmentList(
                response,
                assignments,
                "No assignments found.",
                "Assignments retrieved successfully."
        );
    }

    private void getAssignmentById(
            long assignmentId,
            HttpServletResponse response
    ) throws IOException {

        ComplaintAssignment assignment =
                complaintAssignmentService
                        .getAssignmentById(
                                assignmentId
                        );

        if (assignment == null) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Assignment not found."
            );
            return;
        }

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                "Assignment retrieved successfully.",
                toAssignmentResponse(
                        assignment
                )
        );
    }

    private void getAssignmentsByComplaint(
            long complaintId,
            HttpServletResponse response
    ) throws IOException {

        List<ComplaintAssignment> assignments =
                complaintAssignmentService
                        .getAssignmentsByComplaint(
                                complaintId
                        );

        writeAssignmentList(
                response,
                assignments,
                "No assignments found for this complaint.",
                "Complaint assignments retrieved successfully."
        );
    }

    private void getAssignmentsByAgent(
            long agentId,
            HttpServletResponse response
    ) throws IOException {

        List<ComplaintAssignment> assignments =
                complaintAssignmentService
                        .getAssignmentsByAgent(
                                agentId
                        );

        writeAssignmentList(
                response,
                assignments,
                "No assignments found for this agent.",
                "Agent assignments retrieved successfully."
        );
    }

    private void writeAssignmentList(
            HttpServletResponse response,
            List<ComplaintAssignment> assignments,
            String emptyMessage,
            String successMessage
    ) throws IOException {

        List<Map<String, Object>> assignmentResponses =
                new ArrayList<>();

        if (assignments != null) {

            for (ComplaintAssignment assignment :
                    assignments) {

                assignmentResponses.add(
                        toAssignmentResponse(
                                assignment
                        )
                );
            }
        }

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "total",
                assignmentResponses.size()
        );

        data.put(
                "assignments",
                assignmentResponses
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                assignmentResponses.isEmpty()
                        ? emptyMessage
                        : successMessage,
                data
        );
    }

    private Map<String, Object> toAssignmentResponse(
            ComplaintAssignment assignment
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                assignment.getId()
        );

        if (assignment.getComplaint() != null) {

            data.put(
                    "complaintId",
                    assignment
                            .getComplaint()
                            .getId()
            );

        } else {

            data.put(
                    "complaintId",
                    null
            );
        }

        if (assignment.getAgent() != null) {

            Map<String, Object> agent =
                    new LinkedHashMap<>();

            agent.put(
                    "id",
                    assignment
                            .getAgent()
                            .getId()
            );

            agent.put(
                    "name",
                    assignment
                            .getAgent()
                            .getName()
            );

            agent.put(
                    "email",
                    assignment
                            .getAgent()
                            .getEmail()
            );

            agent.put(
                    "role",
                    assignment
                            .getAgent()
                            .getRole()
            );

            data.put(
                    "agent",
                    agent
            );

        } else {

            data.put(
                    "agent",
                    null
            );
        }

        if (assignment.getAssignedBy() != null) {

            Map<String, Object> assignedBy =
                    new LinkedHashMap<>();

            assignedBy.put(
                    "id",
                    assignment
                            .getAssignedBy()
                            .getId()
            );

            assignedBy.put(
                    "name",
                    assignment
                            .getAssignedBy()
                            .getName()
            );

            assignedBy.put(
                    "email",
                    assignment
                            .getAssignedBy()
                            .getEmail()
            );

            assignedBy.put(
                    "role",
                    assignment
                            .getAssignedBy()
                            .getRole()
            );

            data.put(
                    "assignedBy",
                    assignedBy
            );

        } else {

            data.put(
                    "assignedBy",
                    null
            );
        }

        data.put(
                "active",
                assignment.isActive()
        );

        return data;
    }

    private long getLoggedInUserId(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            throw new IllegalArgumentException(
                    "User is not authenticated."
            );
        }

        Object userId =
                session.getAttribute(
                        AuthenticationConstants.USER_ID
                );

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID is missing from session."
            );
        }

        if (userId instanceof Long) {
            return (Long) userId;
        }

        try {

            return Long.parseLong(
                    userId.toString()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "User ID is invalid."
            );
        }
    }

    private long extractId(
            String pathInfo,
            String prefix
    ) {

        if (pathInfo == null
                || pathInfo.isBlank()) {

            throw new IllegalArgumentException(
                    "ID is required."
            );
        }

        if (!pathInfo.startsWith(prefix)) {

            throw new IllegalArgumentException(
                    "Invalid assignment path."
            );
        }

        String idValue =
                pathInfo.substring(
                        prefix.length()
                );

        return parseId(
                idValue,
                "ID"
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

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must be a valid number."
            );
        }
    }
}