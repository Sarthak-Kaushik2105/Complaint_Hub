package com.complainthub.controller;

import com.complainthub.entity.Category;
import com.complainthub.entity.Complaint;
import com.complainthub.entity.User;
import com.complainthub.service.ComplaintService;
import com.complainthub.service.ComplaintServiceImpl;
import com.complainthub.util.AuthenticationConstants;
import com.complainthub.util.AuthorizationException;
import com.complainthub.util.AuthorizationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/complaints/*")
public class ComplaintController extends HttpServlet {

    private final ComplaintService complaintService =
            new ComplaintServiceImpl();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            Long authenticatedUserId =
                    getAuthenticatedUserId(request);

            String title =
                    request.getParameter("title");

            String description =
                    request.getParameter("description");

            long categoryId =
                    parseId(
                            request.getParameter("categoryId"),
                            "Category ID"
                    );

            Complaint complaint = new Complaint();

            complaint.setTitle(title);
            complaint.setDescription(description);

            User user = new User();
            user.setId(authenticatedUserId);

            Category category = new Category();
            category.setId(categoryId);

            complaint.setUser(user);
            complaint.setCategory(category);

            Complaint createdComplaint =
                    complaintService.createComplaint(complaint);

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            response.getWriter().println(
                    "Complaint created successfully."
            );

            printComplaint(createdComplaint, response);

        } catch (IllegalArgumentException exception) {
            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().println(
                    "Validation Error: "
                            + exception.getMessage()
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create complaint."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo =
                    request.getPathInfo();

            if (pathInfo == null
                    || pathInfo.isBlank()
                    || pathInfo.equals("/")) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Complaint ID or /my path is required"
                );

                return;
            }

            if (pathInfo.equals("/my")) {
                getMyComplaints(request, response);
                return;
            }

            long complaintId =
                    extractComplaintId(request);

            Complaint complaint =
                    complaintService.getComplaintById(
                            complaintId
                    );

            if (complaint == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Complaint not found"
                );

                return;
            }

            AuthorizationUtil.requireOwnerOrAdmin(
                    request,
                    complaint.getUser().getId()
            );

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            printComplaint(complaint, response);

        } catch (AuthorizationException exception) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    exception.getMessage()
            );

        } catch (IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    exception.getMessage()
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            response.sendError(
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

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            long authenticatedUserId = getAuthenticatedUserId(request);

            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                throw new IllegalArgumentException("Complaint ID is required.");
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 3) {
                throw new IllegalArgumentException("Invalid complaint status URL.");
            }

            long complaintId = parseId(pathParts[1], "Complaint ID");

            String action = pathParts[2].toLowerCase();

            if (action.equals("reopen")) {
                Complaint reopenedComplaint =
                        complaintService.reopenComplaint(complaintId, authenticatedUserId);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Complaint reopened successfully.");
                response.getWriter().println("Complaint ID: " + reopenedComplaint.getId());
                response.getWriter().println("Status: " + reopenedComplaint.getStatus());
                response.getWriter().println("Was Resolved: " + reopenedComplaint.isWasResolved());

                return;
            }

            if (action.equals("close")) {

                boolean closed =
                        complaintService.closeComplaint(
                                complaintId,
                                authenticatedUserId
                        );

                if (!closed) {
                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    response.getWriter().println(
                            "Complaint could not be closed."
                    );

                    return;
                }

                response.setStatus(
                        HttpServletResponse.SC_OK
                );

                response.getWriter().println(
                        "Complaint closed successfully."
                );

                response.getWriter().println(
                        "Complaint ID: "
                                + complaintId
                );

                response.getWriter().println(
                        "Status: CLOSED"
                );

                return;
            }

            throw new IllegalArgumentException(
                    "Invalid complaint action. Use /reopen or /close."
            );

        } catch (IllegalArgumentException exception) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().println(
                    "Validation Error: "
                            + exception.getMessage()
            );

        } catch (Exception exception) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().println(
                    "Unable to update complaint status."
            );
        }
    }

    private void getMyComplaints(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        long authenticatedUserId =
                getAuthenticatedUserId(request);

        List<Complaint> complaints =
                complaintService.getComplaintsByUser(
                        authenticatedUserId
                );

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        if (complaints == null || complaints.isEmpty()) {
            response.getWriter().println(
                    "You have not created any complaints yet."
            );

            return;
        }

        response.getWriter().println(
                "Total complaints: "
                        + complaints.size()
        );

        for (Complaint complaint : complaints) {
            printComplaint(complaint, response);
        }
    }

    private Long getAuthenticatedUserId(
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
                    Long.parseLong(userId.toString());

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

    private long parseId(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        try {
            long id = Long.parseLong(value);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        fieldName
                                + " must be greater than zero."
                );
            }

            return id;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid number."
            );
        }
    }

    private long extractComplaintId(
            HttpServletRequest request
    ) {

        String pathInfo =
                request.getPathInfo();

        if (pathInfo == null
                || pathInfo.isBlank()
                || pathInfo.equals("/")) {

            throw new IllegalArgumentException(
                    "Complaint ID is required in the URL."
            );
        }

        String idValue =
                pathInfo.substring(1);

        if (idValue.contains("/")) {
            throw new IllegalArgumentException(
                    "Invalid complaint URL."
            );
        }

        return parseId(
                idValue,
                "Complaint ID"
        );
    }

    private void printComplaint(
            Complaint complaint,
            HttpServletResponse response
    ) throws IOException {

        response.getWriter().println(
                "--------------------------------------"
        );

        response.getWriter().println(
                "Complaint ID: "
                        + complaint.getId()
        );

        response.getWriter().println(
                "Title: "
                        + complaint.getTitle()
        );

        response.getWriter().println(
                "Status: "
                        + complaint.getStatus()
        );

        response.getWriter().println(
                "Priority: "
                        + complaint.getPriority()
        );

        response.getWriter().println(
                "User ID: "
                        + complaint.getUser().getId()
        );

        response.getWriter().println(
                "Category ID: "
                        + complaint.getCategory().getId()
        );
    }
}