package com.trabalho.verificadocs.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.trabalho.verificadocs.config.UploadProperties;

@Service
public class StorageService {

    private final Path uploadDir;

    public StorageService(UploadProperties uploadProperties) {
        this.uploadDir = Paths.get(uploadProperties.getDir()).toAbsolutePath().normalize();
    }

    public String salvar(MultipartFile arquivo) throws IOException {
        Files.createDirectories(uploadDir);

        String original = StringUtils.cleanPath(Objects.toString(arquivo.getOriginalFilename(), "documento"));
        String seguro = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String nomeFinal = UUID.randomUUID() + "_" + seguro;
        Path destino = uploadDir.resolve(nomeFinal).normalize();

        if (!destino.startsWith(uploadDir)) {
            throw new IOException("Caminho de arquivo invalido.");
        }

        Files.copy(arquivo.getInputStream(), destino);
        return destino.toString();
    }
}
