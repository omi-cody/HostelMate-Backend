package com.fyp.HostelMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import com.fyp.HostelMate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    // Repositories (full DB access)
    private final HostelRepository           hostelRepo;
    private final StudentRepository          studentRepo;
    private final StudentKycRepository       studentKycRepo;
    private final AdmissionRepository        admissionRepo;
    private final ApplicationRepository      applicationRepo;
    private final PaymentRepository          paymentRepo;
    private final ComplaintRequestRepository complaintRepo;
    private final EventRepository            eventRepo;
    private final NotificationRepository     notificationRepo;
    private final WebClient.Builder          webClientBuilder;

    @Value("${nvidia.api.key}")
    private String nvidiaApiKey;

    private static final String NVIDIA_API_URL = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String NVIDIA_MODEL   = "meta/llama-3.1-8b-instruct";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    //Request DTO
    public static class ChatRequest {
        public String message;
        public List<Map<String, String>> history;
    }

    //Chat endpoint
    @PostMapping("/chat")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> chat(
            @RequestBody ChatRequest req,
            Authentication auth) {

        if (req.message == null || req.message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Message cannot be empty"));
        }

        String systemPrompt = buildSystemPrompt(auth.getName());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (req.history != null) {
            for (Map<String, String> h : req.history) {
                String role    = h.getOrDefault("role", "user");
                String content = h.getOrDefault("content", "");
                if (!content.isBlank() && (role.equals("user") || role.equals("assistant"))) {
                    messages.add(Map.of("role", role, "content", content));
                }
            }
        }

        messages.add(Map.of("role", "user", "content", req.message));

        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model",       NVIDIA_MODEL);
            requestBody.put("temperature", 0.3);
            requestBody.put("top_p",       0.7);
            requestBody.put("max_tokens",  350);
            requestBody.put("stream",      false);
            requestBody.put("messages",    messages);

            String rawResponse = webClientBuilder.build()
                    .post()
                    .uri(NVIDIA_API_URL)
                    .header("Authorization", "Bearer " + nvidiaApiKey)
                    .header("Content-Type",  "application/json")
                    .header("Accept",        "application/json")
                    .bodyValue(mapper.writeValueAsString(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null || rawResponse.isBlank()) return fallback();

            Map<?, ?> responseMap = mapper.readValue(rawResponse, Map.class);
            List<?> choices = (List<?>) responseMap.get("choices");
            if (choices == null || choices.isEmpty()) return fallback();

            Map<?, ?> msgObj = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String reply = msgObj != null ? (String) msgObj.get("content") : null;

            if (reply == null || reply.isBlank()) return fallback();

            reply = reply.replaceAll("(?s)<think>.*?</think>", "").trim();

            return ResponseEntity.ok(ApiResponse.success("chat", Map.of("reply", reply)));

        } catch (Exception e) {
            log.warn("Chatbot failed: {}", e.getMessage());
            return fallback();
        }
    }

    //System prompt builder — full DB context
    private String buildSystemPrompt(String studentEmail) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are HostelMate Assistant. You help students with hostel-related questions in Nepal. ")
                .append("Be concise — reply in 2-4 sentences max. Be friendly.\n\n");

        sb.append("PLATFORM FLOW: Register → KYC → Browse hostels → Apply → ")
                .append("Hostel accepts → Pay admission fee (Khalti) → Room allocated → ")
                .append("Pay monthly fees (Khalti or cash) → Request leave when done.\n\n");

        // 1. Student identity + KYC
        try {
            var studentOpt = studentRepo.findByUser_Email(studentEmail);
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                sb.append("=== STUDENT DATA ===\n");
                sb.append("Name: ").append(student.getUser().getFullName()).append("\n");
                sb.append("Email: ").append(studentEmail).append("\n");

                studentKycRepo.findByStudent_StudentId(student.getStudentId()).ifPresent(kyc -> {
                    sb.append("KYC Status: ").append(kyc.getKycStatus()).append("\n");
                    if (kyc.getInstituteName()    != null) sb.append("Institute: ").append(kyc.getInstituteName()).append("\n");
                    if (kyc.getInstituteAddress() != null) sb.append("Institute Location: ").append(kyc.getInstituteAddress()).append("\n");
                    if (kyc.getLevelOfStudy()     != null) sb.append("Study Level: ").append(kyc.getLevelOfStudy()).append("\n");
                    if (kyc.getDietType()         != null) sb.append("Diet: ").append(kyc.getDietType()).append("\n");
                    if (kyc.getGuardianName()     != null) sb.append("Guardian: ").append(kyc.getGuardianName()).append("\n");
                });

                // 2. Current Admission
                List<Admission> admissions = admissionRepo.findByStudent_StudentIdOrderByAdmittedDateDesc(student.getStudentId());
                Optional<Admission> activeAdm = admissions.stream()
                        .filter(a -> a.getStatus() == AdmissionStatus.ACTIVE
                                || a.getStatus() == AdmissionStatus.PENDING_PAYMENT
                                || a.getStatus() == AdmissionStatus.LEAVE_REQUESTED)
                        .findFirst();

                if (activeAdm.isPresent()) {
                    Admission adm = activeAdm.get();
                    sb.append("\n=== CURRENT ADMISSION ===\n");
                    sb.append("Status: ").append(adm.getStatus()).append("\n");
                    sb.append("Hostel: ").append(adm.getHostel().getHostelName()).append("\n");
                    if (adm.getRoom() != null) {
                        sb.append("Room: ").append(adm.getRoom().getRoomNumber())
                                .append(" (Floor ").append(adm.getRoom().getFloor()).append(")\n");
                        sb.append("Room Type: ").append(adm.getRoom().getRoomType()).append("\n");
                    } else {
                        sb.append("Room: Not yet allocated\n");
                    }
                    if (adm.getMonthlyFeeAmount() != null) {
                        sb.append("Monthly Fee: Rs ").append(adm.getMonthlyFeeAmount()).append("\n");
                    }
                    if (adm.getAdmittedDate() != null) {
                        sb.append("Admitted On: ").append(adm.getAdmittedDate()).append("\n");
                    }
                } else {
                    sb.append("\n=== CURRENT ADMISSION ===\nNo active admission.\n");
                }

                // 3. Applications
                try {
                    List<Application> apps = applicationRepo.findByStudent_StudentIdOrderByAppliedAtDesc(student.getStudentId());
                    if (!apps.isEmpty()) {
                        sb.append("\n=== MY APPLICATIONS ===\n");
                        apps.stream().limit(5).forEach(app ->
                                sb.append("- ").append(app.getHostel().getHostelName())
                                        .append(" | Type: ").append(app.getApplicationType())
                                        .append(" | Status: ").append(app.getStatus())
                                        .append("\n")
                        );
                    }
                } catch (Exception e) {
                    log.debug("Applications load error: {}", e.getMessage());
                }

                //4. Payment history
                try {
                    List<Payment> payments = paymentRepo.findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId());
                    if (!payments.isEmpty()) {
                        sb.append("\n=== PAYMENT HISTORY ===\n");
                        payments.stream().limit(6).forEach(p -> {
                            String month = p.getFeeMonth() != null
                                    ? p.getFeeMonth().getMonth().name() + " " + p.getFeeMonth().getYear()
                                    : "Admission Fee";
                            sb.append("- ").append(month)
                                    .append(" | Rs ").append(p.getAmount())
                                    .append(" | ").append(p.getPaymentMethod())
                                    .append(" | ").append(p.getStatus())
                                    .append("\n");
                        });

                        long unpaidCount = payments.stream()
                                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                                .count();
                        if (unpaidCount > 0) {
                            sb.append("⚠ Unpaid fees: ").append(unpaidCount).append("\n");
                        }
                    }
                } catch (Exception e) {
                    log.debug("Payments load error: {}", e.getMessage());
                }

                // 5. Complaints / Maintenance requests
                try {
                    List<ComplaintRequest> complaints = complaintRepo.findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId());
                    if (!complaints.isEmpty()) {
                        sb.append("\n=== MY REQUESTS ===\n");
                        complaints.stream().limit(5).forEach(c ->
                                sb.append("- [").append(c.getRequestType()).append("] ")
                                        .append(c.getTitle())
                                        .append(" | Status: ").append(c.getStatus())
                                        .append("\n")
                        );
                    }
                } catch (Exception e) {
                    log.debug("Complaints load error: {}", e.getMessage());
                }

                //6. Hostel events
                try {
                    activeAdm.ifPresent(adm -> {
                        List<Event> events = eventRepo
                                .findByHostel_HostelIdOrderByEventDateDesc(adm.getHostel().getHostelId())
                                .stream()
                                .filter(ev -> ev.getEventDate() != null && !ev.getEventDate().isBefore(LocalDateTime.now()))
                                .collect(Collectors.toList());
                        if (!events.isEmpty()) {
                            sb.append("\n=== UPCOMING HOSTEL EVENTS ===\n");
                            events.stream().limit(3).forEach(e ->
                                    sb.append("- ").append(e.getEventName())
                                            .append(" | ").append(e.getEventDate())
                                            .append(" ").append(e.getLocation())
                                            .append(" @ ").append(e.getLocation() != null ? e.getLocation() : "TBD")
                                            .append("\n")
                            );
                        }
                    });
                } catch (Exception e) {
                    log.debug("Events load error: {}", e.getMessage());
                }

                //7. Unread notifications count
                try {
                    long unread = notificationRepo.countByStudent_StudentIdAndIsReadFalseAndIsDeletedFalse(student.getStudentId());
                    if (unread > 0) {
                        sb.append("\n=== NOTIFICATIONS ===\n");
                        sb.append("Unread notifications: ").append(unread).append("\n");
                    }
                } catch (Exception e) {
                    log.debug("Notifications load error: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Student data load error: {}", e.getMessage());
        }

        // 8. Available verified hostels
        try {
            List<?> hostels = hostelRepo.findAllVerifiedWithRooms();
            if (!hostels.isEmpty()) {
                sb.append("\n=== AVAILABLE VERIFIED HOSTELS ===\n");
                hostels.stream().limit(8).forEach(h -> {
                    var hostel = (Hostel) h;
                    var kyc    = hostel.getHostelKyc();
                    String loc = kyc != null
                            ? safe(kyc.getMunicipality()) + ", " + safe(kyc.getDistrict())
                            : "Nepal";
                    String price = kyc != null && kyc.getRoomPricings() != null
                            ? kyc.getRoomPricings().stream()
                            .mapToLong(r -> r.getMonthlyPrice().longValue())
                            .min().stream()
                            .mapToObj(v -> "Rs " + v + "/mo")
                            .findFirst().orElse("on request")
                            : "on request";

                    sb.append("- ").append(hostel.getHostelName())
                            .append(" (").append(hostel.getHostelType()).append(")")
                            .append(" | ").append(loc)
                            .append(" | from ").append(price);
                    sb.append("\n");
                });
            }
        } catch (Exception e) {
            log.debug("Hostels load error: {}", e.getMessage());
        }

        sb.append("\n=== INSTRUCTIONS ===\n");
        sb.append("- Use the student data above to answer personal questions accurately.\n");
        sb.append("- If asked about admission status, fees, payments, complaints or events — use the real data above.\n");
        sb.append("- Recommend hostels from the verified list when asked.\n");
        sb.append("- Be concise (2-4 sentences). Be friendly.\n");
        sb.append("- Match the student's language (Nepali or English).\n");

        return sb.toString();
    }

    private String safe(String s) {
        return s != null && !s.isBlank() ? s : "";
    }

    private ResponseEntity<ApiResponse<Object>> fallback() {
        return ResponseEntity.ok(ApiResponse.success("chat",
                Map.of("reply",
                        "Sorry, I'm having trouble connecting right now. Please try again in a moment.")));
    }
}