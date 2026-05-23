package com.api.RestAPI.infrastructure.persistence.message.repository;

import com.api.RestAPI.infrastructure.persistence.message.entity.ProviderMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProviderMessageJpaRepository extends JpaRepository<ProviderMessageEntity, UUID> {
}