package com.complainthub.controller;

import com.complainthub.entity.Complaint;
import com.complainthub.entity.ComplaintAttachment;
import com.complainthub.service.ComplaintAttachmentService;
import com.complainthub.service.ComplaintAttachmentServiceImpl;
import com.complainthub.service.ComplaintService;
import com.complainthub.service.ComplaintServiceImpl;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        try {
            String pathInfo =
                    req.getPathInfo();

            long complaintId =
                    parseComplaintIdForUpload(
                            pathInfo
                    );

            Complaint complaint =
                    complaintService.getComplaintById(
                            complaintId
                    );

            if (complaint == null) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Complaint not found."
                );

                return;
            }

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    complaint.getUser().getId()
            );

            Part filePart =
                    req.getPart("file");

            if (filePart == null
                    || filePart.getSize() <= 0) {

                throw new IllegalArgumentException(
                        "File is required."
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

            ComplaintAttachment attachment =
                    attachmentService.uploadAttachment(
                            complaintId,
                            fileData,
                            fileName,
                            contentType
                    );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_CREATED,
                    "Attachment uploaded successfully.",
                    toAttachmentResponse(
                            attachment
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
                    "Attachment upload failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to upload attachment.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to upload attachment."
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

            /*
             * GET /api/attachments/complaint/{complaintId}
             *
             * Returns all attachments belonging to a complaint.
             */
            if (pathInfo != null
                    && pathInfo.startsWith("/complaint/")) {

                long complaintId =
                        parseComplaintIdForList(
                                pathInfo
                        );

                Complaint complaint =
                        complaintService.getComplaintById(
                                complaintId
                        );

                if (complaint == null) {

                    ResponseUtil.sendError(
                            res,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Complaint not found."
                    );

                    return;
                }

                AuthorizationUtil.requireOwnerOrAdmin(
                        req,
                        complaint.getUser().getId()
                );

                List<ComplaintAttachment> attachments =
                        attachmentService
                                .getAttachmentsByComplaint(
                                        complaintId
                                );

                writeAttachmentList(
                        attachments,
                        res
                );

                return;
            }

            /*
             * GET /api/attachments/{attachmentId}
             *
             * Returns metadata for one attachment.
             */
            long attachmentId =
                    parseAttachmentId(
                            pathInfo
                    );

            ComplaintAttachment attachment =
                    attachmentService.getAttachmentById(
                            attachmentId
                    );

            if (attachment == null) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Attachment not found."
                );

                return;
            }

            Complaint complaint =
                    attachment.getComplaint();

            if (complaint == null
                    || complaint.getUser() == null) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Attachment complaint information not found."
                );

                return;
            }

            AuthorizationUtil.requireOwnerOrAdmin(
                    req,
                    complaint.getUser().getId()
            );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Attachment retrieved successfully.",
                    toAttachmentResponse(
                            attachment
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
                    "Unable to retrieve attachment.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve attachment."
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

            long attachmentId =
                    parseAttachmentId(
                            pathInfo
                    );

            ComplaintAttachment attachment =
                    attachmentService.getAttachmentById(
                            attachmentId
                    );

            if (attachment == null) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Attachment not found."
                );

                return;
            }

            Complaint complaint =
                    attachment.getComplaint();

            if (complaint == null
                    || complaint.getUser() == null) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
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

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Attachment not found."
                );

                return;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "attachmentId",
                    attachmentId
            );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Attachment deleted successfully.",
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
                    "Attachment deletion failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to delete attachment.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to delete attachment."
            );
        }
    }

    private void writeAttachmentList(
            List<ComplaintAttachment> attachments,
            HttpServletResponse response
    ) throws IOException {

        List<Map<String, Object>> attachmentResponses =
                new ArrayList<>();

        if (attachments != null) {

            for (ComplaintAttachment attachment :
                    attachments) {

                attachmentResponses.add(
                        toAttachmentResponse(
                                attachment
                        )
                );
            }
        }

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "total",
                attachmentResponses.size()
        );

        data.put(
                "attachments",
                attachmentResponses
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                attachmentResponses.isEmpty()
                        ? "No attachments found for this complaint."
                        : "Attachments retrieved successfully.",
                data
        );
    }

    private Map<String, Object> toAttachmentResponse(
            ComplaintAttachment attachment
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                attachment.getId()
        );

        Complaint complaint =
                attachment.getComplaint();

        if (complaint != null) {

            data.put(
                    "complaintId",
                    complaint.getId()
            );

        } else {

            data.put(
                    "complaintId",
                    null
            );
        }

        data.put(
                "fileName",
                attachment.getFileName()
        );

        data.put(
                "storedFileName",
                attachment.getStoredFileName()
        );

        data.put(
                "filePath",
                attachment.getFilePath()
        );

        data.put(
                "contentType",
                attachment.getContentType()
        );

        data.put(
                "fileSize",
                attachment.getFileSize()
        );

        data.put(
                "createdAt",
                attachment.getCreatedAt()
        );

        return data;
    }

    private long parseComplaintIdForUpload(
            String pathInfo
    ) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || !pathInfo.startsWith(
                "/complaint/"
        )) {

            throw new IllegalArgumentException(
                    "Complaint ID is required."
            );
        }

        String idValue =
                pathInfo.substring(
                        "/complaint/".length()
                );

        if (idValue.isBlank()
                || idValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid complaint ID."
            );
        }

        return parsePositiveId(
                idValue,
                "Complaint ID"
        );
    }

    private long parseComplaintIdForList(
            String pathInfo
    ) {

        String idValue =
                pathInfo.substring(
                        "/complaint/".length()
                );

        if (idValue.isBlank()
                || idValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid complaint ID."
            );
        }

        return parsePositiveId(
                idValue,
                "Complaint ID"
        );
    }

    private long parseAttachmentId(
            String pathInfo
    ) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || pathInfo.startsWith(
                "/complaint/"
        )) {

            throw new IllegalArgumentException(
                    "Attachment ID is required."
            );
        }

        String idValue =
                pathInfo.substring(1);

        if (idValue.isBlank()
                || idValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid attachment ID."
            );
        }

        return parsePositiveId(
                idValue,
                "Attachment ID"
        );
    }

    private long parsePositiveId(
            String value,
            String fieldName
    ) {

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