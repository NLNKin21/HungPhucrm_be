package com.hungphu.crm.features.consultation.repository;

import com.hungphu.crm.features.consultation.entity.ConsultationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, UUID> {

    @Query("""
        SELECT n FROM ConsultationNote n
        LEFT JOIN FETCH n.author
        WHERE n.consultation.id = :consultationId
        ORDER BY n.createdAt ASC
        """)
    List<ConsultationNote> findByConsultationId(@Param("consultationId") UUID consultationId);
}