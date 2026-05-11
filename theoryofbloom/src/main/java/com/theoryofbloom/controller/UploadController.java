package com.theoryofbloom.controller;

import com.theoryofbloom.service.GCSService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final GCSService gcsService;

    public UploadController(GCSService gcsService) {
        this.gcsService = gcsService;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            String url = gcsService.uploadFile(file);

            return ResponseEntity.ok(url);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(e.getMessage());
        }
    }
}
