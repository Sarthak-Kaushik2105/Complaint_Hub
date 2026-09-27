package com.complainthub.service;

import com.complainthub.entity.Complaint;
import com.complainthub.entity.enums.ComplaintPriority;
import com.complainthub.entity.enums.ComplaintStatus;

import java.util.List;

public interface ComplaintService {
    Complaint createComplaint(Complaint complaint);
    Complaint getComplaintById(long id);
    List<Complaint> getAllComplaints();
    List<Complaint> getComplaintsByUser(long userId);
    List<Complaint> getComplaintsByCategory(long categoryId);
    List<Complaint> getComplaintsByStatus(ComplaintStatus status);
    List<Complaint> getComplaintsByPriority(ComplaintPriority priority);
    Complaint updateComplaint(Complaint complaint);
    Complaint updateStatus(long complaintId, ComplaintStatus status);
    Complaint updatePriority(long complaintId, ComplaintPriority priority);
    Complaint reopenComplaint(long complaintId, long userId);
    boolean closeComplaint(long complaintId, long userId);
}
