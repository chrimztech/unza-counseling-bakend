package zm.unza.counseling.service;

import zm.unza.counseling.dto.response.CaseDocumentResponse;
import zm.unza.counseling.entity.CaseDocument;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.repository.CaseDocumentRepository;
import zm.unza.counseling.repository.CaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaseDocumentService {

    private final CaseDocumentRepository caseDocumentRepository;
    private final CaseRepository caseRepository;
    private final String uploadDirectory = "uploads/case-documents/";

    public CaseDocumentService(CaseDocumentRepository caseDocumentRepository, CaseRepository caseRepository) {
        this.caseDocumentRepository = caseDocumentRepository;
        this.caseRepository = caseRepository;
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        Path path = Paths.get(uploadDirectory);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload directory", e);
            }
        }
    }

    @Transactional
    public CaseDocumentResponse uploadDocument(Long caseId, MultipartFile file, Long uploadedBy, String description, Boolean isPublic) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        String fileName = file.getOriginalFilename();
        String filePath = uploadDirectory + caseId + "/" + fileName;
        String fileType = file.getContentType();
        Long fileSize = file.getSize();

        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + fileName, e);
        }

        CaseDocument document = new CaseDocument();
        document.setCaseEntity(caseEntity);
        document.setFileName(fileName);
        document.setFilePath(filePath);
        document.setFileType(fileType);
        document.setFileSize(fileSize);
        document.setDescription(description);
        document.setUploadedBy(uploadedBy);
        document.setUploadedAt(LocalDateTime.now());
        document.setIsPublic(isPublic);

        CaseDocument savedDocument = caseDocumentRepository.save(document);
        return convertToResponse(savedDocument);
    }

    public CaseDocumentResponse getDocumentById(Long id) {
        CaseDocument document = caseDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        return convertToResponse(document);
    }

    public List<CaseDocumentResponse> getDocumentsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));
        return caseDocumentRepository.findByCaseEntity(caseEntity).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CaseDocumentResponse> getPublicDocumentsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));
        return caseDocumentRepository.findByCaseEntityAndIsPublic(caseEntity, true).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CaseDocumentResponse> getDocumentsByUploadedBy(Long uploadedBy) {
        return caseDocumentRepository.findByUploadedBy(uploadedBy).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CaseDocumentResponse> searchDocumentsByCaseAndFileName(Long caseId, String fileName) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));
        return caseDocumentRepository.findByCaseEntityAndFileNameContaining(caseEntity, fileName).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public long countDocumentsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));
        return caseDocumentRepository.countByCaseEntity(caseEntity);
    }

    public long countDocumentsByUploadedBy(Long uploadedBy) {
        return caseDocumentRepository.countByUploadedBy(uploadedBy);
    }

    @Transactional
    public void deleteDocument(Long id) {
        CaseDocument document = caseDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));

        try {
            Path path = Paths.get(document.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + document.getFileName(), e);
        }

        caseDocumentRepository.delete(document);
    }

    @Transactional
    public void deleteDocumentsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        List<CaseDocument> documents = caseDocumentRepository.findByCaseEntity(caseEntity);
        for (CaseDocument document : documents) {
            try {
                Path path = Paths.get(document.getFilePath());
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file: " + document.getFileName(), e);
            }
        }

        caseDocumentRepository.deleteByCaseEntity(caseEntity);
    }

    private CaseDocumentResponse convertToResponse(CaseDocument document) {
        CaseDocumentResponse response = new CaseDocumentResponse();
        response.setId(document.getId());
        response.setCaseId(document.getCaseEntity().getId());
        response.setFileName(document.getFileName());
        response.setFilePath(document.getFilePath());
        response.setFileType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setDescription(document.getDescription());
        response.setUploadedBy(document.getUploadedBy());
        response.setUploadedAt(document.getUploadedAt());
        response.setIsPublic(document.getIsPublic());
        return response;
    }
}