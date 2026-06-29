package com.nereden.api.domain.repository;

import com.nereden.api.domain.entity.AnalysisRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, UUID> {
}
