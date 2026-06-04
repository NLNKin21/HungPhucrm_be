package com.hungphu.crm.features.consultation;

import com.hungphu.crm.features.consultation.dto.ConsultationNoteDto;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consultations/{consultationId}/notes")
@RequiredArgsConstructor
public class ConsultationNoteController {

    private final ConsultationNoteService noteService;

    // GET /api/v1/consultations/{consultationId}/notes
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<ConsultationNoteDto.Response>>> getNotes(
            @PathVariable("consultationId") UUID consultationId) {
        return ResponseEntity.ok(
                ApiResponse.success(noteService.getNotes(consultationId)));
    }

    // POST /api/v1/consultations/{consultationId}/notes
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<ConsultationNoteDto.Response>> addNote(
            @PathVariable("consultationId") UUID consultationId,
            @Valid @RequestBody ConsultationNoteDto.CreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        noteService.addNote(consultationId, request, currentUser),
                        "Đã thêm ghi chú"));
    }

    // DELETE /api/v1/consultations/{consultationId}/notes/{noteId}
    @DeleteMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable("consultationId") UUID consultationId,
            @PathVariable("noteId") UUID noteId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        noteService.deleteNote(noteId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa ghi chú"));
    }
}