package com.hungphu.crm.features.project.repository;

import com.hungphu.crm.features.project.entity.ProjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectDocumentRepository extends JpaRepository<ProjectDocument, UUID> {

    List<ProjectDocument> findByProjectIdOrderByUploadedAtDesc(UUID projectId);
}
