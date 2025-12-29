package com.sky.controller.admin;

import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/admin/upload")
public class AdminUploadController {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping(value = "/book-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadBookCover(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException("请选择图片文件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BaseException("图片过大，请选择 5MB 以内的文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BaseException("仅支持图片格式");
        }

        String original = file.getOriginalFilename();
        String ext = extFromFilename(original);
        String objectName = "book/covers/" + UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            String url = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(url);
        } catch (Exception e) {
            throw new BaseException("上传失败：" + e.getMessage());
        }
    }

    private static String extFromFilename(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        String ext = filename.substring(idx).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) return "";
        return ext;
    }
}

