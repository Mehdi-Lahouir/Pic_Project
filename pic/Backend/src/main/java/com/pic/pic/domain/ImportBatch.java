package com.pic.pic.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_batch")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_filename")
    private String sourceFilename;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    @PrePersist
    void prePersist() {
        if (importedAt == null) importedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSourceFilename() { return sourceFilename; }
    public Instant getImportedAt() { return importedAt; }

    public void setSourceFilename(String sourceFilename) { this.sourceFilename = sourceFilename; }
}
