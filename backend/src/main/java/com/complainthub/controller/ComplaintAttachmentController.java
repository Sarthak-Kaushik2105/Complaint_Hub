package com.complainthub.controller;

import com.complainthub.entity.Complaint;
import com.complainthub.entity.ComplaintAttachment;
import com.complainthub.service.ComplaintAttachmentService;
import com.complainthub.service.ComplaintAttachmentServiceImpl;
import com.complainthub.service.ComplaintService;
import com.complainthub.service.ComplaintServiceImpl;
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
import java.util.List;

@WebServlet("/api/attachments/*")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 5 * 1024 * 1024
)
public class ComplaintAttachmentController extends HttpServlet {

    private final ComplaintAttachmentService attachmentService =
            new ComplaintAttachmentServiceImpl();

    private final ComplaintService complaintService =
            new ComplaintServiceImpl();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();

            long complaintId =
                    parseComplaintIdForUpload(pathInfo);

            Complaint complaint =
                    complaintService.getComplaintById(complaintId);

            if (complaint == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().println("Complaint not found.");
                return;
            }

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    complaint.getUser().getId()
            );

            Part filePart = req.getPart("file");

            if (filePart == null || filePart.getSize() <= 0) {
                throw new IllegalArgumentException(
                        "File is required."
                );
            }

            String fileName =
                    filePart.getSubmittedFileName();

            String contentType =
                    filePart.getContentType();

            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException(
                        "File name is required."
                );
            }

            byte[] fileData;

            try (InputStream inputStream =
                         filePart.getInputStream()) {

                fileData = inputStream.readAllBytes();
            }

            ComplaintAttachment attachment =
                    attachmentService.uploadAttachment(
                            complaintId,
                            fileData,
                            fileName,
                            contentType
                    );

            res.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            res.getWriter().println(
                    "Attachment uploaded successfully."
            );
            res.getWriter().println(
                    "Attachment ID: " + attachment.getId()
            );
            res.getWriter().println(
                    "Complaint ID: " + complaintId
            );
            res.getWriter().println(
                    "File Name: " + attachment.getFileName()
            );
            res.getWriter().println(
                    "Content Type: " + attachment.getContentType()
            );
            res.getWriter().println(
                    "File Size: " + attachment.getFileSize()
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
                    "Attachment upload failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            res.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            res.getWriter().println(
                    "Unable to upload attachment."
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

            /*
             * GET /api/attachments/complaint/{complaintId}
             *
             * Returns all attachments belonging to a complaint.
             */
            if (pathInfo != null
                    && pathInfo.startsWith("/complaint/")) {

                long complaintId =
                        parseComplaintIdForList(pathInfo);

                Complaint complaint =
                        complaintService.getComplaintById(
                                complaintId
                        );

                if (complaint == null) {
                    res.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    res.getWriter().println(
                            "Complaint not found."
                    );

                    return;
                }

                AuthorizationUtil.requireOwnerOrAdmin(
                        req,
                        complaint.getUser().getId()
                );

                List<ComplaintAttachment> attachments =
                        attachmentService.getAttachmentsByComplaint(
                                complaintId
                        );

                res.setStatus(
                        HttpServletResponse.SC_OK
                );

                if (attachments.isEmpty()) {
                    res.getWriter().println(
                            "No attachments found for this complaint."
                    );

                    return;
                }

                for (ComplaintAttachment attachment : attachments) {

                    res.getWriter().println(
                            "Attachment ID: "
                                    + attachment.getId()
                    );

                    res.getWriter().println(
                            "File Name: "
                                    + attachment.getFileName()
                    );

                    res.getWriter().println(
                            "Content Type: "
                                    + attachment.getContentType()
                    );

                    res.getWriter().println(
                            "File Size: "
                                    + attachment.getFileSize()
                    );

                    res.getWriter().println(
                            "Created At: "
                                    + attachment.getCreatedAt()
                    );

                    res.getWriter().println();
                }

                return;
            }

            /*
             * GET /api/attachments/{attachmentId}
             *
             * Returns metadata for one attachment.
             */
            long attachmentId =
                    parseAttachmentId(pathInfo);

            ComplaintAttachment attachment =
                    attachmentService.getAttachmentById(
                            attachmentId
                    );

            if (attachment == null) {
                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Attachment not found."
                );

                return;
            }

            Complaint complaint =
                    attachment.getComplaint();

            if (complaint == null
                    || complaint.getUser() == null) {

                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Attachment complaint information not found."
                );

                return;
            }

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    complaint.getUser().getId()
            );

            res.setStatus(
                    HttpServletResponse.SC_OK
            );

            res.getWriter().println(
                    "Attachment ID: " + attachment.getId()
            );

            res.getWriter().println(
                    "Complaint ID: " + complaint.getId()
            );

            res.getWriter().println(
                    "File Name: " + attachment.getFileName()
            );

            res.getWriter().println(
                    "Stored File Name: "
                            + attachment.getStoredFileName()
            );

            res.getWriter().println(
                    "File Path: " + attachment.getFilePath()
            );

            res.getWriter().println(
                    "Content Type: "
                            + attachment.getContentType()
            );

            res.getWriter().println(
                    "File Size: " + attachment.getFileSize()
            );

            res.getWriter().println(
                    "Created At: " + attachment.getCreatedAt()
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
                    "Unable to retrieve attachment."
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

            long attachmentId =
                    parseAttachmentId(pathInfo);

            ComplaintAttachment attachment =
                    attachmentService.getAttachmentById(
                            attachmentId
                    );

            if (attachment == null) {
                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Attachment not found."
                );

                return;
            }

            Complaint complaint =
                    attachment.getComplaint();

            if (complaint == null
                    || complaint.getUser() == null) {

                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Attachment complaint information not found."
                );

                return;
            }

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    complaint.getUser().getId()
            );

            boolean deleted =
                    attachmentService.deleteAttachment(
                            attachmentId
                    );

            if (!deleted) {
                res.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                res.getWriter().println(
                        "Attachment not found."
                );

                return;
            }

            res.setStatus(
                    HttpServletResponse.SC_OK
            );

            res.getWriter().println(
                    "Attachment deleted successfully."
            );

            res.getWriter().println(
                    "Attachment ID: " + attachmentId
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
                    "Attachment deletion failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            res.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            res.getWriter().println(
                    "Unable to delete attachment."
            );
        }
    }

    private long parseComplaintIdForUpload(String pathInfo) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || !pathInfo.startsWith("/complaint/")) {

            throw new IllegalArgumentException(
                    "Complaint ID is required."
            );
        }

        String idValue =
                pathInfo.substring("/complaint/".length());

        if (idValue.isBlank()
                || idValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid complaint ID."
            );
        }

        try {
            return Long.parseLong(idValue);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Complaint ID must be a valid number."
            );
        }
    }

    private long parseComplaintIdForList(String pathInfo) {

        String idValue =
                pathInfo.substring("/complaint/".length());

        if (idValue.isBlank()
                || idValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid complaint ID."
            );
        }

        try {
            return Long.parseLong(idValue);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Complaint ID must be a valid number."
            );
        }
    }

    private long parseAttachmentId(String pathInfo) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || pathInfo.startsWith("/complaint/")) {

            throw new IllegalArgumentException(
                    "Attachment ID is required."
            );
        }

        String idValue = pathInfo.substring(1);

        if (idValue.isBlank()
                || idValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid attachment ID."
            );
        }

        try {
            return Long.parseLong(idValue);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Attachment ID must be a valid number."
            );
        }
    }
}