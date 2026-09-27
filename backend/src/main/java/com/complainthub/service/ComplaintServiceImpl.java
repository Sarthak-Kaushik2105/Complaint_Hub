package com.complainthub.service;

import com.complainthub.dao.*;
import com.complainthub.entity.Category;
import com.complainthub.entity.Complaint;
import com.complainthub.entity.User;
import com.complainthub.entity.enums.ComplaintPriority;
import com.complainthub.entity.enums.ComplaintStatus;
import com.complainthub.util.ValidationUtil;

import java.util.List;

public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintDaoImpl complaintDao;
    private final UserDaoImpl userDao;
    private final CategoryDao categoryDao;

    public ComplaintServiceImpl() {
        this.complaintDao = new ComplaintDaoImpl();
        this.userDao = new UserDaoImpl();
        this.categoryDao = new CategoryDao();
    }

    @Override
    public Complaint createComplaint(Complaint complaint) {

        validateComplaint(complaint);

        long userId = complaint.getUser().getId();
        long categoryId = complaint.getCategory().getId();

        ValidationUtil.validateId(userId, "User Id");
        ValidationUtil.validateId(categoryId, "Category Id");

        User user = userDao.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        Category category = categoryDao.findById(categoryId);

        if (category == null) {
            throw new IllegalArgumentException("Category not found.");
        }

        if (!category.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot create complaint under an inactive category."
            );
        }

        complaint.setTitle(complaint.getTitle().trim());
        complaint.setDescription(complaint.getDescription().trim());

        /*
         * Set the initial complaint state if the caller
         * has not provided one.
         */
        if (complaint.getStatus() == null) {
            complaint.setStatus(ComplaintStatus.OPEN);
        }

        if (complaint.getPriority() == null) {
            complaint.setPriority(ComplaintPriority.NEW);
        }

        complaint.setUser(user);
        complaint.setCategory(category);

        complaintDao.save(complaint);

        return complaint;
    }

    @Override
    public Complaint getComplaintById(long id) {

        ValidationUtil.validateId(id, "Complaint Id");

        return complaintDao.findById(id);
    }

    @Override
    public List<Complaint> getAllComplaints() {

        return complaintDao.findAll();
    }

    @Override
    public List<Complaint> getComplaintsByUser(long userId) {

        ValidationUtil.validateId(userId, "User Id");

        return complaintDao.findByUserId(userId);
    }

    @Override
    public List<Complaint> getComplaintsByCategory(long categoryId) {

        ValidationUtil.validateId(categoryId, "Category Id");

        return complaintDao.findByCategoryId(categoryId);
    }

    @Override
    public List<Complaint> getComplaintsByStatus(
            ComplaintStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Complaint Status is required."
            );
        }

        return complaintDao.findByStatus(status);
    }

    @Override
    public List<Complaint> getComplaintsByPriority(
            ComplaintPriority priority) {

        if (priority == null) {
            throw new IllegalArgumentException(
                    "Complaint Priority is required."
            );
        }

        return complaintDao.findByPriority(priority);
    }

    @Override
    public Complaint updateComplaint(Complaint complaint) {

        validateComplaint(complaint);

        ValidationUtil.validateId(
                complaint.getId(),
                "Complaint Id"
        );

        Complaint existingComplaint =
                complaintDao.findById(complaint.getId());

        if (existingComplaint == null) {
            throw new IllegalArgumentException(
                    "Complaint not found."
            );
        }

        complaint.setTitle(complaint.getTitle().trim());
        complaint.setDescription(complaint.getDescription().trim());

        /*
         * Preserve fields that should not be changed
         * through the general complaint update.
         */
        complaint.setStatus(existingComplaint.getStatus());
        complaint.setPriority(existingComplaint.getPriority());
        complaint.setUser(existingComplaint.getUser());
        complaint.setCategory(existingComplaint.getCategory());
        complaint.setCreatedAt(existingComplaint.getCreatedAt());

        return complaintDao.update(complaint);
    }

    @Override
    public Complaint updateStatus(
            long complaintId,
            ComplaintStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        Complaint complaint = complaintDao.findById(complaintId);
        if (complaint == null) {
            throw new IllegalArgumentException("Complaint not found.");
        }

        complaint.setStatus(status);
        if (status == ComplaintStatus.RESOLVED) {
            complaint.setWasResolved(true);
        }

        return complaintDao.update(complaint);
    }

    @Override
    public Complaint updatePriority(
            long complaintId,
            ComplaintPriority priority) {

        ValidationUtil.validateId(
                complaintId,
                "Complaint Id"
        );

        if (priority == null) {
            throw new IllegalArgumentException(
                    "Complaint Priority is required."
            );
        }

        Complaint complaint =
                complaintDao.updatePriority(
                        complaintId,
                        priority
                );

        if (complaint == null) {
            return null;
        }

        return complaint;
    }

    @Override
    public Complaint reopenComplaint(
            long complaintId,
            long userId
    ) {
        Complaint complaint = complaintDao.findById(complaintId);

        if (complaint == null) {
            throw new IllegalArgumentException("Complaint not found.");
        }

        if (complaint.getUser() == null) {
            throw new IllegalArgumentException("Complaint owner is missing.");
        }

        if (complaint.getUser().getId() != userId) {
            throw new IllegalArgumentException("Only the complaint owner can reopen the complaint.");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new IllegalArgumentException("Only a resolved complaint can be reopened.");
        }

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setWasResolved(true);
        return complaintDao.update(complaint);
    }

    @Override
    public boolean closeComplaint(
            long complaintId,
            long userId
    ) {
        Complaint complaint = complaintDao.findById(complaintId);

        if (complaint == null) {
            throw new IllegalArgumentException("Complaint not found.");
        }

        if (complaint.getUser() == null) {
            throw new IllegalArgumentException("Complaint owner is missing.");
        }

        if (complaint.getUser().getId() != userId) {
            throw new IllegalArgumentException("Only the complaint owner can close the complaint.");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new IllegalArgumentException("Only a resolved complaint can be closed.");
        }

        complaint.setStatus(ComplaintStatus.CLOSED);
        complaintDao.update(complaint);
        return true;
    }

    // -------- Validate Method --------
    private void validateComplaint(Complaint complaint) {

        if (complaint == null) {
            throw new IllegalArgumentException(
                    "Complaint cannot be null."
            );
        }

        ValidationUtil.validateRequired(
                complaint.getTitle(),
                "Complaint Title"
        );

        ValidationUtil.validateMaxLength(
                complaint.getTitle().trim(),
                100,
                "Complaint Title"
        );

        ValidationUtil.validateRequired(
                complaint.getDescription(),
                "Complaint Description"
        );

        ValidationUtil.validateMaxLength(
                complaint.getDescription().trim(),
                2000,
                "Complaint Description"
        );

        if (complaint.getUser() == null) {
            throw new IllegalArgumentException(
                    "Complaint User is required."
            );
        }

        if (complaint.getCategory() == null) {
            throw new IllegalArgumentException(
                    "Complaint Category is required."
            );
        }
    }
}