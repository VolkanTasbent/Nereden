package com.nereden.api.presentation.controller;

import com.nereden.api.application.dto.ApiResponse;
import com.nereden.api.application.dto.storage.UploadImageResponse;
import com.nereden.api.application.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UploadImageResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.of(storageService.uploadImage(file));
    }
}
