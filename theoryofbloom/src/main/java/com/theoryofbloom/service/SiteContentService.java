package com.theoryofbloom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.theoryofbloom.model.SiteContent;
import org.springframework.stereotype.Service;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads and saves SiteContent as a JSON file.
 * File location: {project-root}/content/site-content.json
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SiteContentService {
    private static final Logger logger = LoggerFactory.getLogger(SiteContentService.class);

    private static final String CONTENT_DIR  = "content";
    private static final String CONTENT_FILE = "site-content.json";

    private final ObjectMapper mapper;
    private final Validator validator;

    public SiteContentService(Validator validator) {
        this.validator = validator;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private Path getFilePath() {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        
        // 1. Check current dir / content
        Path p1 = userDir.resolve(CONTENT_DIR).resolve(CONTENT_FILE);
        if (Files.exists(p1)) return p1;
        
        // 2. Check theoryofbloom / content (if we are in root)
        Path p2 = userDir.resolve("theoryofbloom").resolve(CONTENT_DIR).resolve(CONTENT_FILE);
        if (Files.exists(p2)) return p2;

        // 3. Check parent / content (if we are in submodule)
        if (userDir.getParent() != null) {
            Path p3 = userDir.getParent().resolve(CONTENT_DIR).resolve(CONTENT_FILE);
            if (Files.exists(p3)) return p3;
        }

        return p1; // fallback to current dir
    }

    /** Load current site content. Returns default values if file doesn't exist yet. */
    public SiteContent load() {
        Path path = getFilePath();
        String finalPath = path.toAbsolutePath().toString();
        logger.info("SiteContentService: Loading from: {}", finalPath);
        
        if (Files.exists(path)) {
            try {
                byte[] data = Files.readAllBytes(path);
                if (data.length == 0) {
                    logger.warn("SiteContentService: File is empty: {}", finalPath);
                    return new SiteContent();
                }
                SiteContent sc = mapper.readValue(data, SiteContent.class);
                logger.info("SiteContentService: Successfully loaded content from {}", finalPath);
                return sc;
            } catch (IOException e) {
                logger.error("SiteContentService: Error reading/parsing {}: {}", finalPath, e.getMessage());
                return new SiteContent();
            }
        }
        logger.warn("SiteContentService: No file found at {}. Using defaults.", finalPath);
        return new SiteContent();
    }

    /** Persist site content to disk. */
    public void save(SiteContent content) {
        Set<ConstraintViolation<SiteContent>> violations = validator.validate(content);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<SiteContent> violation : violations) {
                sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
            }
            throw new ConstraintViolationException("Site content validation failed: " + sb.toString(), violations);
        }

        Path path = getFilePath();
        try {
            File dir = path.getParent().toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            mapper.writeValue(path.toFile(), content);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save site content: " + e.getMessage(), e);
        }
    }
}
