package com.kdlt.platform.user.service;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${cloudinary.enabled:false}")
    private boolean cloudinaryEnabled;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/uploads}")
    private String uploadBaseUrl;

    private Cloudinary cloudinary;

    private Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
        }

        return cloudinary;
    }

    public String store(MultipartFile file, String subFolder) {
        validate(file);

        return cloudinaryEnabled
                ? storeOnCloudinary(file, subFolder)
                : storeLocally(file, subFolder);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "L'image ne doit pas dépasser 5 Mo."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Format d'image non supporté. Utilisez JPG, PNG ou WebP."
            );
        }
    }

    @SuppressWarnings("unchecked")
    private String storeOnCloudinary(
            MultipartFile file,
            String subFolder
    ) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", "tkardgo/" + subFolder,
                    "public_id", UUID.randomUUID().toString(),
                    "overwrite", true
            );

            Map<String, Object> result =
                    getCloudinary()
                            .uploader()
                            .upload(file.getBytes(), options);

            return (String) result.get("secure_url");

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Échec de l'upload vers Cloudinary.",
                    e
            );
        }
    }

    private String storeLocally(
            MultipartFile file,
            String subFolder
    ) {
        try {
            Path baseDir = Path.of(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Path dir = baseDir
                    .resolve(subFolder)
                    .normalize();

            if (!dir.startsWith(baseDir)) {
                throw new IllegalArgumentException(
                        "Dossier d'upload invalide."
                );
            }

            Files.createDirectories(dir);

            String extension = getExtension(file.getContentType());

            String filename = UUID.randomUUID() + extension;

            Path target = dir.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return uploadBaseUrl
                    + "/"
                    + subFolder
                    + "/"
                    + filename;

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Impossible d'enregistrer le fichier localement.",
                    e
            );
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Format d'image non supporté."
            );
        };
    }
}