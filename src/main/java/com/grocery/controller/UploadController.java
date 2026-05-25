package com.grocery.controller;

import com.grocery.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/product-image")
    @PreAuthorize("hasRole('ADMIN')")
    public Map upload(@RequestParam MultipartFile file) throws Exception {
        return cloudinaryService.upload(file);
    }
}
