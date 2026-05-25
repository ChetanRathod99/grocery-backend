package com.grocery.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    @Value("${cloudinary.cloud-name}") private String cloudName;
    @Value("${cloudinary.api-key}") private String apiKey;
    @Value("${cloudinary.api-secret}") private String apiSecret;

    public Map upload(MultipartFile file) throws Exception {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret));
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "grocery/products"));
    }
}
