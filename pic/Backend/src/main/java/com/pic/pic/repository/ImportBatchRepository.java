package com.pic.pic.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pic.pic.domain.ImportBatch;

@Profile("db")
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {}
