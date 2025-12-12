package com.pic.pic.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pic.pic.domain.ImportBatch;
import com.pic.pic.repository.ImportBatchRepository;

@Profile("db")
@RestController
@RequestMapping("/api/imports")
public class ImportController {
    private final ImportBatchRepository repo;

    public ImportController(ImportBatchRepository repo) {
        this.repo = repo;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportBatch upload(@RequestPart("file") MultipartFile file) {
        ImportBatch b = new ImportBatch();
        b.setSourceFilename(file.getOriginalFilename());
        return repo.save(b);
    }
}
