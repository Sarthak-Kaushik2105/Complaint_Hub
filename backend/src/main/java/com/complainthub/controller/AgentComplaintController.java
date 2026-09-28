package com.complainthub.controller;

import com.complainthub.entity.Complaint;
import com.complainthub.entity.ComplaintAssignment;
import com.complainthub.entity.ComplaintUpdate;
import com.complainthub.entity.enums.ComplaintStatus;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.service.ComplaintAssignmentService;
import com.complainthub.service.ComplaintAssignmentServiceImpl;
import com.complainthub.service.ComplaintService;
import com.complainthub.service.ComplaintServiceImpl;
import com.complainthub.service.ComplaintUpdateService;
import com.complainthub.service.ComplaintUpdateServiceImpl;
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

@WebServlet("/api/agent/complaints/*")
public class AgentComplaintController extends HttpServlet {

    private ComplaintService complaintService;
    private ComplaintAssignmentService complaintAssignmentService;
    private ComplaintUpdateService complaintUpdateService;

    @Override
    public void init() throws ServletException {
        complaintService = new ComplaintServiceImpl();
        complaintAssignmentService =
                new ComplaintAssignmentServiceImpl();
        complaintUpdateService =
                new ComplaintUpdateServiceImpl();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    request,
                    UserRole.AGENT
            );

            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.equals("/")) {

                getAssignedComplaints(
                        request,
                        response
                );
                return;
            }

            String[] pathParts =
                    pathInfo.split("/");

            if (pathParts.length == 2) {

                long complaintId =
                        parseId(
                                pathParts[1],
                                "Complaint Id"
                        );

                getAssignedComplaintById(
                        request,
                        response,
                        complaintId
                );
                return;
            }

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Endpoint not found."
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Failed to process agent GET request.",
                    e
            );

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    request,
                    UserRole.AGENT
            );

            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.equals("/")) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Complaint Id is required."
                );
                return;
            }

            String[] pathParts =
                    pathInfo.split("/");

            /*
             * Expected endpoint:
             *
             * POST /api/agent/complaints/{complaintId}/updates
             */
            if (pathParts.length == 3
                    && pathParts[2].equals("updates")) {

                long complaintId =
                        parseId(
                                pathParts[1],
                                "Complaint Id"
                        );

                createComplaintUpdate(
                        request,
                        response,
                        complaintId
                );
                return;
            }

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Endpoint not found."
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Failed to process agent POST request.",
                    e
            );

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred."
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
                    UserRole.AGENT
            );

            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.equals("/")) {

                ResponseUtil.sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Complaint Id is required."
                );
                return;
            }

            String[] pathParts =
                    pathInfo.split("/");

            /*
             * Expected endpoint:
             *
             * PUT /api/agent/complaints/{complaintId}/status
             */
            if (pathParts.length == 3
                    && pathParts[2].equals("status")) {

                long complaintId =
                        parseId(
                                pathParts[1],
                                "Complaint Id"
                        );

                updateComplaintStatus(
                        request,
                        response,
                        complaintId
                );
                return;
            }

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Endpoint not found."
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Failed to process agent PUT request.",
                    e
            );

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred."
            );
        }
    }

    /*
     * GET /api/agent/complaints
     *
     * Returns complaints assigned to the logged-in agent.
     */
    private void getAssignedComplaints(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        long agentId =
                getLoggedInUserId(request);

        List<ComplaintAssignment> assignments =
                complaintAssignmentService
                        .getAssignmentsByAgent(agentId);

        List<Map<String, Object>> complaints =
                new ArrayList<>();

        for (ComplaintAssignment assignment : assignments) {

            if (!assignment.isActive()) {
                continue;
            }

            Complaint complaint =
                    assignment.getComplaint();

            if (complaint == null) {
                continue;
            }

            complaints.add(
                    toComplaintSummaryResponse(
                            complaint
                    )
            );
        }

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "total",
                complaints.size()
        );

        data.put(
                "complaints",
                complaints
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                complaints.isEmpty()
                        ? "No active complaints assigned to you."
                        : "Assigned complaints retrieved successfully.",
                data
        );
    }

    /*
     * GET /api/agent/complaints/{complaintId}
     *
     * Returns one complaint only if it is actively assigned
     * to the logged-in agent.
     */
    private void getAssignedComplaintById(
            HttpServletRequest request,
            HttpServletResponse response,
            long complaintId
    ) throws IOException {

        long agentId =
                getLoggedInUserId(request);

        ComplaintAssignment assignment =
                getActiveAssignmentForAgent(
                        complaintId,
                        agentId
                );

        if (assignment == null) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "This complaint is not actively assigned to you."
            );
            return;
        }

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

        List<ComplaintUpdate> updates =
                complaintUpdateService
                        .getUpdatesByComplaint(
                                complaintId
                        );

        Map<String, Object> data =
                toComplaintDetailResponse(
                        complaint,
                        updates
                );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                "Complaint details retrieved successfully.",
                data
        );
    }

    /*
     * POST /api/agent/complaints/{complaintId}/updates
     *
     * Expected form parameter:
     *
     * message
     */
    private void createComplaintUpdate(
            HttpServletRequest request,
            HttpServletResponse response,
            long complaintId
    ) throws IOException {

        long agentId =
                getLoggedInUserId(request);

        ComplaintAssignment assignment =
                getActiveAssignmentForAgent(
                        complaintId,
                        agentId
                );

        if (assignment == null) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "You can add updates only to complaints assigned to you."
            );
            return;
        }

        String message =
                request.getParameter("message");

        if (message == null
                || message.isBlank()) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Update message is required."
            );
            return;
        }

        ComplaintUpdate complaintUpdate =
                new ComplaintUpdate();

        Complaint complaint =
                new Complaint();

        complaint.setId(complaintId);

        com.complainthub.entity.User agent =
                new com.complainthub.entity.User();

        agent.setId(agentId);

        complaintUpdate.setComplaint(
                complaint
        );

        complaintUpdate.setUpdatedBy(
                agent
        );

        complaintUpdate.setMessage(
                message.trim()
        );

        /*
         * Agent-created updates are visible to the user.
         */
        complaintUpdate.setVisibleToUser(
                true
        );

        ComplaintUpdate savedUpdate =
                complaintUpdateService.createUpdate(
                        complaintUpdate
                );

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                savedUpdate.getId()
        );

        data.put(
                "complaintId",
                complaintId
        );

        data.put(
                "message",
                savedUpdate.getMessage()
        );

        data.put(
                "visibleToUser",
                savedUpdate.getVisibleToUser()
        );

        data.put(
                "createdAt",
                savedUpdate.getCreatedAt()
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_CREATED,
                "Complaint update created successfully.",
                data
        );
    }

    /*
     * PUT /api/agent/complaints/{complaintId}/status
     *
     * Expected form parameter:
     *
     * status
     */
    private void updateComplaintStatus(
            HttpServletRequest request,
            HttpServletResponse response,
            long complaintId
    ) throws IOException {

        long agentId =
                getLoggedInUserId(request);

        ComplaintAssignment assignment =
                getActiveAssignmentForAgent(
                        complaintId,
                        agentId
                );

        if (assignment == null) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "You can update only complaints assigned to you."
            );
            return;
        }

        String statusValue =
                request.getParameter("status");

        if (statusValue == null
                || statusValue.isBlank()) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Status is required."
            );
            return;
        }

        ComplaintStatus newStatus;

        try {

            newStatus =
                    ComplaintStatus.valueOf(
                            statusValue
                                    .trim()
                                    .toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid complaint status."
            );
            return;
        }

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

        ComplaintStatus currentStatus =
                complaint.getStatus();

        if (!isAllowedStatusTransition(
                currentStatus,
                newStatus
        )) {

            ResponseUtil.sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "This status transition is not allowed."
            );
            return;
        }

        Complaint updatedComplaint =
                complaintService.updateStatus(
                        complaintId,
                        newStatus
                );

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                updatedComplaint.getId()
        );

        data.put(
                "status",
                updatedComplaint.getStatus()
        );

        data.put(
                "wasResolved",
                updatedComplaint.isWasResolved()
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                "Complaint status updated successfully.",
                data
        );
    }

    /*
     * Checks whether the complaint is actively assigned
     * to the current agent.
     */
    private ComplaintAssignment getActiveAssignmentForAgent(
            long complaintId,
            long agentId
    ) {

        ComplaintAssignment activeAssignment =
                complaintAssignmentService
                        .getActiveAssignmentByComplaint(
                                complaintId
                        );

        if (activeAssignment == null) {
            return null;
        }

        if (activeAssignment.getAgent() == null) {
            return null;
        }

        if (activeAssignment.getAgent().getId()
                != agentId) {
            return null;
        }

        return activeAssignment;
    }

    /*
     * Current workflow:
     *
     * ASSIGNED -> IN_PROGRESS
     * IN_PROGRESS -> RESOLVED
     *
     * The agent cannot directly close a complaint.
     */
    private boolean isAllowedStatusTransition(
            ComplaintStatus currentStatus,
            ComplaintStatus newStatus
    ) {

        if (currentStatus == null
                || newStatus == null) {

            return false;
        }

        if (currentStatus
                == ComplaintStatus.ASSIGNED) {

            return newStatus
                    == ComplaintStatus.IN_PROGRESS;
        }

        if (currentStatus
                == ComplaintStatus.IN_PROGRESS) {

            return newStatus
                    == ComplaintStatus.RESOLVED;
        }

        return false;
    }

    private long getLoggedInUserId(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            throw new SecurityException(
                    "You must be logged in."
            );
        }

        Object userIdAttribute =
                session.getAttribute(
                        AuthenticationConstants.USER_ID
                );

        if (userIdAttribute == null) {

            throw new SecurityException(
                    "You must be logged in."
            );
        }

        if (userIdAttribute instanceof Long) {
            return (Long) userIdAttribute;
        }

        if (userIdAttribute instanceof Integer) {
            return ((Integer) userIdAttribute).longValue();
        }

        if (userIdAttribute instanceof String) {

            try {
                return Long.parseLong(
                        (String) userIdAttribute
                );

            } catch (NumberFormatException e) {

                throw new SecurityException(
                        "Invalid logged-in user id."
                );
            }
        }

        throw new SecurityException(
                "Invalid logged-in user id."
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

    /*
     * Safe complaint summary for the agent's complaint list.
     */
    private Map<String, Object> toComplaintSummaryResponse(
            Complaint complaint
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                complaint.getId()
        );

        data.put(
                "title",
                complaint.getTitle()
        );

        data.put(
                "status",
                complaint.getStatus()
        );

        data.put(
                "priority",
                complaint.getPriority()
        );

        data.put(
                "createdAt",
                complaint.getCreatedAt()
        );

        return data;
    }

    /*
     * Full complaint response including updates.
     */
    private Map<String, Object> toComplaintDetailResponse(
            Complaint complaint,
            List<ComplaintUpdate> updates
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                complaint.getId()
        );

        data.put(
                "title",
                complaint.getTitle()
        );

        data.put(
                "description",
                complaint.getDescription()
        );

        data.put(
                "status",
                complaint.getStatus()
        );

        data.put(
                "priority",
                complaint.getPriority()
        );

        data.put(
                "wasResolved",
                complaint.isWasResolved()
        );

        data.put(
                "createdAt",
                complaint.getCreatedAt()
        );

        data.put(
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

            data.put(
                    "user",
                    userData
            );

        } else {

            data.put(
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

            data.put(
                    "category",
                    categoryData
            );

        } else {

            data.put(
                    "category",
                    null
            );
        }

        List<Map<String, Object>> updateResponses =
                new ArrayList<>();

        if (updates != null) {

            for (ComplaintUpdate update : updates) {

                updateResponses.add(
                        toUpdateResponse(update)
                );
            }
        }

        data.put(
                "updates",
                updateResponses
        );

        return data;
    }

    private Map<String, Object> toUpdateResponse(
            ComplaintUpdate update
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                update.getId()
        );

        data.put(
                "message",
                update.getMessage()
        );

        data.put(
                "visibleToUser",
                update.getVisibleToUser()
        );

        data.put(
                "createdAt",
                update.getCreatedAt()
        );

        if (update.getUpdatedBy() != null) {

            Map<String, Object> updatedBy =
                    new LinkedHashMap<>();

            updatedBy.put(
                    "id",
                    update.getUpdatedBy().getId()
            );

            updatedBy.put(
                    "name",
                    update.getUpdatedBy().getName()
            );

            updatedBy.put(
                    "role",
                    update.getUpdatedBy().getRole()
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

        return data;
    }
}