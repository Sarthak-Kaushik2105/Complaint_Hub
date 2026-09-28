package com.complainthub.service;

import com.complainthub.dao.*;
import com.complainthub.entity.Complaint;
import com.complainthub.entity.ComplaintAssignment;
import com.complainthub.entity.User;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.util.ValidationUtil;

import java.util.List;

public class ComplaintAssignmentServiceImpl
        implements ComplaintAssignmentService {

    private final ComplaintAssignmentDao assignmentDao;
    private final ComplaintDao complaintDao;
    private final UserDao userDao;

    public ComplaintAssignmentServiceImpl() {
        this.assignmentDao = new ComplaintAssignmentDaoImpl();
        this.complaintDao = new ComplaintDaoImpl();
        this.userDao = new UserDaoImpl();
    }

    @Override
    public ComplaintAssignment createAssignment(
            ComplaintAssignment assignment) {

        validateAssignment(assignment);

        long complaintId = assignment.getComplaint().getId();
        long agentId = assignment.getAgent().getId();
        long assignedById = assignment.getAssignedBy().getId();

        ValidationUtil.validateId(complaintId, "Complaint Id");
        ValidationUtil.validateId(agentId, "Agent Id");
        ValidationUtil.validateId(assignedById, "Assigned By Id");

        Complaint complaint = complaintDao.findById(complaintId);

        if (complaint == null) {
            throw new IllegalArgumentException("Complaint not found.");
        }

        User agent = userDao.findById(agentId);

        if (agent == null) {
            throw new IllegalArgumentException("Agent not found.");
        }

        if (agent.getRole() != UserRole.AGENT) {
            throw new IllegalArgumentException("Selected user is not an agent.");
        }

        User assignedBy = userDao.findById(assignedById);

        if (assignedBy == null) {
            throw new IllegalArgumentException("Assigned By user not found.");
        }

        if (assignedBy.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only an admin can assign a complaint.");
        }

        ComplaintAssignment activeAssignment =
                assignmentDao.findActiveAssignmentByComplaintId(
                        complaintId
                );

        if (activeAssignment != null) {
            throw new IllegalArgumentException("Complaint already has an active assignment.");
        }

        if (complaint.getStatus() !=
                com.complainthub.entity.enums.ComplaintStatus.OPEN) {
            throw new IllegalArgumentException("Only an OPEN complaint can be assigned.");
        }

        assignment.setComplaint(complaint);
        assignment.setAgent(agent);
        assignment.setAssignedBy(assignedBy);
        assignment.setActive(true);

        assignmentDao.save(assignment);
        complaint.setStatus(com.complainthub.entity.enums.ComplaintStatus.ASSIGNED);
        complaintDao.update(complaint);

        return assignment;
    }

    @Override
    public ComplaintAssignment getAssignmentById(long id) {

        ValidationUtil.validateId(
                id,
                "Assignment Id"
        );

        return assignmentDao.findById(id);
    }

    @Override
    public List<ComplaintAssignment> getAllAssignments() {

        return assignmentDao.findAll();
    }

    @Override
    public List<ComplaintAssignment> getAssignmentsByComplaint(
            long complaintId) {

        ValidationUtil.validateId(
                complaintId,
                "Complaint Id"
        );

        return assignmentDao.findByComplaintId(
                complaintId
        );
    }

    @Override
    public List<ComplaintAssignment> getAssignmentsByAgent(
            long agentId) {

        ValidationUtil.validateId(
                agentId,
                "Agent Id"
        );

        return assignmentDao.findByAgentId(
                agentId
        );
    }

    @Override
    public ComplaintAssignment getActiveAssignmentByComplaint(
            long complaintId) {

        ValidationUtil.validateId(
                complaintId,
                "Complaint Id"
        );

        return assignmentDao.findActiveAssignmentByComplaintId(
                complaintId
        );
    }

    @Override
    public ComplaintAssignment updateAssignment(
            ComplaintAssignment assignment) {

        validateAssignment(assignment);

        ValidationUtil.validateId(assignment.getId(), "Assignment Id");

        ComplaintAssignment existingAssignment = assignmentDao.findById(assignment.getId());

        if (existingAssignment == null)
            throw new IllegalArgumentException("Assignment not found.");

        long complaintId = assignment.getComplaint().getId();
        long agentId = assignment.getAgent().getId();
        long assignedById = assignment.getAssignedBy().getId();

        ValidationUtil.validateId(complaintId, "Complaint Id");
        ValidationUtil.validateId(agentId, "Agent Id");
        ValidationUtil.validateId(assignedById, "Assigned By Id");

        Complaint complaint = complaintDao.findById(complaintId);

        if (complaint == null) {
            throw new IllegalArgumentException(
                    "Complaint not found."
            );
        }

        User agent = userDao.findById(agentId);

        if (agent == null) {
            throw new IllegalArgumentException(
                    "Agent not found."
            );
        }

        if (agent.getRole() != UserRole.AGENT) {
            throw new IllegalArgumentException(
                    "Selected user is not an agent."
            );
        }

        User assignedBy = userDao.findById(assignedById);

        if (assignedBy == null) {
            throw new IllegalArgumentException(
                    "Assigned By user not found."
            );
        }

        if (assignedBy.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException(
                    "Only an admin can assign a complaint."
            );
        }

        /*
         * Do not allow this assignment to create
         * another active assignment for the same complaint.
         */
        if (assignment.isActive()) {

            ComplaintAssignment activeAssignment =
                    assignmentDao.findActiveAssignmentByComplaintId(
                            complaintId
                    );

            if (activeAssignment != null
                    && activeAssignment.getId()
                    != assignment.getId()) {

                throw new IllegalArgumentException(
                        "Complaint already has another active assignment."
                );
            }
        }

        assignment.setComplaint(complaint);
        assignment.setAgent(agent);
        assignment.setAssignedBy(assignedBy);

        return assignmentDao.update(assignment);
    }

    @Override
    public boolean deactivateAssignment(long assignmentId) {

        ValidationUtil.validateId(
                assignmentId,
                "Assignment Id"
        );

        ComplaintAssignment assignment =
                assignmentDao.findById(assignmentId);

        if (assignment == null) {
            return false;
        }

        if (!assignment.isActive()) {
            return true;
        }

        assignment.setActive(false);

        assignmentDao.update(assignment);

        return true;
    }

    // -------- Validate Method --------
    private void validateAssignment(
            ComplaintAssignment assignment) {

        if (assignment == null) {
            throw new IllegalArgumentException(
                    "Assignment cannot be null."
            );
        }

        if (assignment.getComplaint() == null) {
            throw new IllegalArgumentException(
                    "Complaint is required."
            );
        }

        if (assignment.getAgent() == null) {
            throw new IllegalArgumentException(
                    "Agent is required."
            );
        }

        if (assignment.getAssignedBy() == null) {
            throw new IllegalArgumentException(
                    "Assigned By user is required."
            );
        }
    }
}