package com.zestia.datn.zestia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class LuckyWheelIconService {
    static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    private final Path uploadDirectory;

    public LuckyWheelIconService() {
        this(resolveUploadDirectory());
    }

    LuckyWheelIconService(Path uploadDirectory) {
        this.uploadDirectory = uploadDirectory.toAbsolutePath().normalize();
    }

    public Map<String, String> store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Vui lòng chọn ảnh biểu tượng.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw badRequest("Ảnh biểu tượng không được vượt quá 2 MB.");
        }

        try {
            byte[] bytes = file.getBytes();
            String extension = detectExtension(bytes);
            if (extension == null) {
                throw badRequest("Ảnh biểu tượng phải là tệp JPG, PNG, WebP hoặc AVIF hợp lệ.");
            }
            Files.createDirectories(uploadDirectory);
            String filename = "prize_" + UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT) + extension;
            Path target = uploadDirectory.resolve(filename).normalize();
            if (!target.getParent().equals(uploadDirectory)) {
                throw badRequest("Tên tệp ảnh không hợp lệ.");
            }
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            return Map.of("url", "/images/lucky-wheel/" + filename);
        } catch (ResponseStatusException error) {
            throw error;
        } catch (IOException error) {
            log.error("Không thể lưu ảnh biểu tượng vòng quay", error);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể lưu ảnh biểu tượng lúc này.");
        }
    }

    static String detectExtension(byte[] bytes) {
        if (bytes == null || bytes.length < 12) return null;
        if ((bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        if ((bytes[0] & 0xff) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return ".png";
        }
        String header = new String(bytes, 0, 12, StandardCharsets.ISO_8859_1);
        if (header.startsWith("RIFF") && "WEBP".equals(header.substring(8, 12))) return ".webp";
        String box = header.substring(4, 12);
        if (box.startsWith("ftypavif") || box.startsWith("ftypavis")) return ".avif";
        return null;
    }

    private static Path resolveUploadDirectory() {
        Path[] candidates = {
                Paths.get("..", "frontend", "public", "images", "lucky-wheel"),
                Paths.get("frontend", "public", "images", "lucky-wheel")
        };
        for (Path candidate : candidates) {
            Path absolute = candidate.toAbsolutePath().normalize();
            Path frontendDirectory = absolute.getParent().getParent().getParent();
            if (Files.exists(frontendDirectory)) return absolute;
        }
        return candidates[0].toAbsolutePath().normalize();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
