package com.agrolink.productservice.controller;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 * <p>
 * 提供文件上传接口，支持上传产品图片等静态资源文件。
 * 上传后的文件存储在本地磁盘的上传目录中，并返回可访问的 URL 路径。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    /** 文件上传目录，默认值为 "uploads" */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 上传文件
     * <p>
     * 接受 multipart/form-data 格式的文件上传，生成唯一文件名并保存到上传目录。
     *
     * @param file 上传的文件
     * @return 上传后的文件访问路径（如 /uploads/uuid.jpg）
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }
        try {
            // 提取原始文件扩展名
            String ext = "";
            String name = file.getOriginalFilename();
            if (name != null && name.contains(".")) {
                ext = name.substring(name.lastIndexOf("."));
            }
            // 使用 UUID 生成唯一文件名，防止重名覆盖
            String filename = UUID.randomUUID().toString() + ext;
            Path dir = Paths.get(uploadDir);
            // 如果上传目录不存在则创建
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            // 保存文件到目标目录
            file.transferTo(dir.resolve(filename));
            // 返回可访问的 URL 路径
            return Result.success("/uploads/" + filename);
        } catch (IOException e) {
            throw new BusinessException(400, "上传失败: " + e.getMessage());
        }
    }
}
