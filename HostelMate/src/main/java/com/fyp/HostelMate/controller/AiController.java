package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.entity.Admission;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final HostelRepository hostelRepo;
    private final StudentRepository studentRepo;
    private final StudentKycRepository studentKycRepo;
    private final AdmissionRepository admissionRepo;
    private final WebClient.Builder webClientBuilder;

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    @PostMapping("/hostel-recommendation/{hostelId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getHostelRecommendation(
            Authentication auth,
            @PathVariable UUID hostelId) {

        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success("AI recommendation",
                    Map.of("recommendation",
                            "AI recommendations require an Anthropic API key configured in application.properties (anthropic.api.key). Contact admin to enable this feature.")));
        }

        Hostel hostel = hostelRepo.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));

        var student = studentRepo.findByUser_Email(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        var kycOpt = studentKycRepo.findByStudent_StudentId(student.getStudentId());

        // Build student context
        String studentInstitute = kycOpt.map(k -> safe(k.getInstituteName())).orElse("Unknown");
        String studentInstituteAddr = kycOpt.map(k -> safe(k.getInstituteAddress())).orElse("Unknown");
        String studentLevel = kycOpt.map(k -> safe(k.getLevelOfStudy())).orElse("Unknown");
        String studentDiet = kycOpt.map(k -> k.getDietType() != null ? k.getDietType().name() : "VEG").orElse("VEG");

        // Build hostel context
        var kyc = hostel.getHostelKyc();
        String hostelLocation = kyc != null
                ? safe(kyc.getTole()) + ", " + safe(kyc.getMunicipality()) + ", " + safe(kyc.getDistrict())
                : "Location unknown";
        String amenities = kyc != null ? safe(kyc.getAmenities()) : "Not specified";
        boolean hasMeals = kyc != null && kyc.getMealPlans() != null && !kyc.getMealPlans().isEmpty();
        String admissionFee = kyc != null && kyc.getAdmissionFee() != null ? "Rs " + kyc.getAdmissionFee() : "Unknown";

        // Compare with currently admitted students at this hostel
        List<Admission> currentAdmissions = admissionRepo
                .findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(hostel.getHostelId(), AdmissionStatus.ACTIVE);

        Map<String, Long> levelCounts = currentAdmissions.stream()
                .map(a -> a.getStudent().getStudentKyc())
                .filter(Objects::nonNull)
                .filter(k -> k.getLevelOfStudy() != null)
                .collect(Collectors.groupingBy(k -> k.getLevelOfStudy(), Collectors.counting()));

        String peerProfile = levelCounts.isEmpty()
                ? "No current students to compare with."
                : "Currently admitted students: " + currentAdmissions.size() + " total. " +
                  "Study levels: " + levelCounts.entrySet().stream()
                      .map(e -> e.getValue() + " " + e.getKey() + " students")
                      .collect(Collectors.joining(", ")) + ".";

        // Institute locations of current students (to gauge proximity fit)
        List<String> peerInstitutes = currentAdmissions.stream()
                .map(a -> a.getStudent().getStudentKyc())
                .filter(Objects::nonNull)
                .map(k -> safe(k.getInstituteAddress()))
                .filter(addr -> !addr.equals("Unknown") && !addr.isBlank())
                .distinct().limit(5)
                .collect(Collectors.toList());

        String peerInstituteInfo = peerInstitutes.isEmpty()
                ? ""
                : "Peer students' institute locations: " + String.join(", ", peerInstitutes) + ".";

        String prompt = "You are a friendly hostel recommendation advisor for students in Nepal.\n\n" +
                "STUDENT LOOKING FOR HOSTEL:\n" +
                "- Institute: " + studentInstitute + "\n" +
                "- Institute location: " + studentInstituteAddr + "\n" +
                "- Level of study: " + studentLevel + "\n" +
                "- Diet preference: " + studentDiet + "\n\n" +
                "HOSTEL BEING VIEWED:\n" +
                "- Name: " + hostel.getHostelName() + "\n" +
                "- Type: " + hostel.getHostelType() + " hostel\n" +
                "- Location: " + hostelLocation + "\n" +
                "- Amenities: " + amenities + "\n" +
                "- Meal plan: " + (hasMeals ? "Yes" : "No") + "\n" +
                "- Admission fee: " + admissionFee + "\n\n" +
                "PEER COMPARISON (other admitted students at this hostel):\n" +
                peerProfile + "\n" + peerInstituteInfo + "\n\n" +
                "Based on the student's institute address and the hostel's location, assess proximity. " +
                "Compare their study level with peer students. Check diet compatibility with meal plan. " +
                "Give a concise 2-3 sentence personalized recommendation. Be specific and honest — " +
                "mention if the hostel is far, if peers are at similar level, and if diet is compatible.";

        try {
            var body = Map.of(
                    "model", "claude-haiku-4-5-20251001",
                    "max_tokens", 350,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            var response = webClientBuilder.build()
                    .post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", anthropicApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("content")) {
                var content = (List<?>) response.get("content");
                if (!content.isEmpty()) {
                    String text = (String) ((Map<?, ?>) content.get(0)).get("text");
                    return ResponseEntity.ok(ApiResponse.success("AI recommendation", Map.of("recommendation", text)));
                }
            }
        } catch (Exception e) {
            log.warn("AI recommendation failed: {}", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("AI recommendation",
                Map.of("recommendation", "Could not generate recommendation at this time. Please try again.")));
    }

    private String safe(String s) { return s != null && !s.isBlank() ? s : "Unknown"; }
}
