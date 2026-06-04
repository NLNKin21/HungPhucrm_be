package com.hungphu.crm.features.consultation;

import com.hungphu.crm.features.consultation.dto.ConsultationNoteDto;
import com.hungphu.crm.features.consultation.entity.Consultation;
import com.hungphu.crm.features.consultation.entity.ConsultationNote;
import com.hungphu.crm.features.consultation.repository.ConsultationNoteRepository;
import com.hungphu.crm.features.consultation.repository.ConsultationRepository;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationNoteService {

    private final ConsultationNoteRepository noteRepository;
    private final ConsultationRepository     consultationRepository;
    private final UserRepository             userRepository;

    // ── Lấy tất cả notes của 1 tư vấn ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConsultationNoteDto.Response> getNotes(UUID consultationId) {
        // Kiểm tra tư vấn tồn tại
        if (!consultationRepository.existsById(consultationId)) {
            throw new ResourceNotFoundException("Tư vấn", consultationId);
        }
        return noteRepository.findByConsultationId(consultationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Thêm note mới ─────────────────────────────────────────────────────────
    // Employee chỉ được add note vào tư vấn được giao cho mình.
    // Admin/Manager được add note vào bất kỳ tư vấn nào.

    @Transactional
    public ConsultationNoteDto.Response addNote(UUID consultationId,
                                                ConsultationNoteDto.CreateRequest request,
                                                UserDetailsImpl currentUser) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Tư vấn", consultationId));

        // Employee chỉ được ghi chú tư vấn của mình
        if (currentUser.getRole().name().equals("EMPLOYEE")) {
            boolean isAssignee = consultation.getAssignedTo() != null
                    && consultation.getAssignedTo().getId().equals(currentUser.getId());
            if (!isAssignee) {
                throw new AccessDeniedException("Bạn không có quyền ghi chú cho tư vấn này");
            }
        }

        ConsultationNote note = new ConsultationNote();
        note.setConsultation(consultation);
        note.setAuthor(userRepository.getReferenceById(currentUser.getId()));
        note.setContent(request.getContent().trim());

        return toResponse(noteRepository.save(note));
    }

    // ── Xóa note ──────────────────────────────────────────────────────────────
    // Chỉ tác giả hoặc Admin mới được xóa.

    @Transactional
    public void deleteNote(UUID noteId, UserDetailsImpl currentUser) {
        ConsultationNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Ghi chú", noteId));

        boolean isAuthor = note.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin  = currentUser.getRole().name().equals("ADMIN");

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền xóa ghi chú này");
        }

        noteRepository.delete(note);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ConsultationNoteDto.Response toResponse(ConsultationNote note) {
        return ConsultationNoteDto.Response.builder()
                .id(note.getId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .author(ConsultationNoteDto.Response.AuthorInfo.builder()
                        .id(note.getAuthor().getId())
                        .fullName(note.getAuthor().getFullName())
                        .build())
                .build();
    }
}