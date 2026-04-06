package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.ReviewRequest;
import com.fyp.HostelMate.dto.response.ReviewResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.ReviewerType;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl {

    private final ReviewRepository reviewRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;

    // ── STUDENT REVIEWS HOSTEL ────────────────────────────────────────────────
    @Transactional
    public ReviewResponse studentReviewHostel(User currentUser, ReviewRequest req) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        Admission admission = admissionRepository.findById(req.getAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found."));

        if (!admission.getStudent().getStudentId().equals(student.getStudentId())) {
            throw new BadRequestException("This admission does not belong to you.");
        }
        // Must have left the hostel before reviewing
        if (admission.getIsActive()) {
            throw new BadRequestException("You can only review a hostel after you have left.");
        }
        if (reviewRepository.existsByAdmission_AdmissionIdAndReviewerType(
                req.getAdmissionId(), ReviewerType.STUDENT)) {
            throw new BadRequestException("You have already reviewed this hostel for this admission.");
        }

        Review review = new Review();
        review.setReviewerType(ReviewerType.STUDENT);
        review.setStudent(student);
        review.setHostel(admission.getHostel());
        review.setAdmission(admission);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        reviewRepository.save(review);
        log.info("Student reviewed hostel: admissionId={}", req.getAdmissionId());
        return ReviewResponse.from(review);
    }

    // ── HOSTEL REVIEWS STUDENT ────────────────────────────────────────────────
    @Transactional
    public ReviewResponse hostelReviewStudent(User currentUser, ReviewRequest req) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        Admission admission = admissionRepository.findById(req.getAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found."));

        if (!admission.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("This admission does not belong to your hostel.");
        }
        if (admission.getIsActive()) {
            throw new BadRequestException("You can only review a student after they have left.");
        }
        if (reviewRepository.existsByAdmission_AdmissionIdAndReviewerType(
                req.getAdmissionId(), ReviewerType.HOSTEL)) {
            throw new BadRequestException("You have already reviewed this student for this admission.");
        }

        Review review = new Review();
        review.setReviewerType(ReviewerType.HOSTEL);
        review.setStudent(admission.getStudent());
        review.setHostel(hostel);
        review.setAdmission(admission);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        reviewRepository.save(review);
        log.info("Hostel reviewed student: admissionId={}", req.getAdmissionId());
        return ReviewResponse.from(review);
    }

    // ── GET HOSTEL REVIEWS (public — shown on hostel listing) ─────────────────
    public List<ReviewResponse> getHostelReviews(UUID hostelId) {
        return reviewRepository.findByHostel_HostelIdAndReviewerType(hostelId, ReviewerType.STUDENT)
                .stream().map(ReviewResponse::from).toList();
    }

    // ── GET STUDENT REVIEWS (visible to hostels when an application comes in) ─
    public List<ReviewResponse> getStudentReviews(UUID studentId) {
        return reviewRepository.findByStudent_StudentIdAndReviewerType(studentId, ReviewerType.HOSTEL)
                .stream().map(ReviewResponse::from).toList();
    }

    // ── ADMIN: ALL REVIEWS ────────────────────────────────────────────────────
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream().map(ReviewResponse::from).toList();
    }
}
