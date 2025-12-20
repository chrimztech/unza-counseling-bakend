package zm.unza.counseling.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.storage.type:local}") // local, s3, minio
    private String storageType;

    @Value("${app.storage.local.path:uploads/}")
    private String localStoragePath;

    @Value("${app.storage.s3.bucket:unza-counseling-files}")
    private String s3BucketName;

    @Value("${app.storage.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${app.storage.s3.region:us-east-1}")
    private String s3Region;

    @Value("${app.storage.s3.access-key:}")
    private String s3AccessKey;

    @Value("${app.storage.s3.secret-key:}")
    private String s3SecretKey;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private AmazonS3 createS3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Endpoint, s3Region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    public String storeFile(MultipartFile file, String subdirectory) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = generateUniqueFileName(originalFileName);
        String filePath = subdirectory + "/" + fileName;

        switch (storageType.toLowerCase()) {
            case "s3":
                return storeFileInS3(file, filePath);
            case "minio":
                return storeFileInS3(file, filePath); // MinIO is compatible with S3 API
            case "local":
            default:
                return storeFileLocally(file, filePath);
        }
    }

    private String storeFileLocally(MultipartFile file, String filePath) throws IOException {
        Path targetLocation = Paths.get(localStoragePath).resolve(filePath);
        Files.createDirectories(targetLocation.getParent());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File stored locally at: {}", targetLocation);
        return filePath;
    }

    private String storeFileInS3(MultipartFile file, String filePath) throws IOException {
        AmazonS3 s3Client = createS3Client();
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest putObjectRequest = new PutObjectRequest(s3BucketName, filePath, file.getInputStream(), metadata);
        s3Client.putObject(putObjectRequest);

        log.info("File stored in S3 at: {}/{}", s3BucketName, filePath);
        return filePath;
    }

    public byte[] downloadFile(String filePath) throws IOException {
        switch (storageType.toLowerCase()) {
            case "s3":
            case "minio":
                return downloadFileFromS3(filePath);
            case "local":
            default:
                return downloadFileLocally(filePath);
        }
    }

    private byte[] downloadFileLocally(String filePath) throws IOException {
        Path fileLocation = Paths.get(localStoragePath).resolve(filePath);
        return Files.readAllBytes(fileLocation);
    }

    private byte[] downloadFileFromS3(String filePath) throws IOException {
        AmazonS3 s3Client = createS3Client();
        S3Object s3Object = s3Client.getObject(s3BucketName, filePath);
        
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            return inputStream.readAllBytes();
        }
    }

    public void deleteFile(String filePath) throws IOException {
        switch (storageType.toLowerCase()) {
            case "s3":
            case "minio":
                deleteFileFromS3(filePath);
                break;
            case "local":
            default:
                deleteFileLocally(filePath);
                break;
        }
    }

    private void deleteFileLocally(String filePath) throws IOException {
        Path fileLocation = Paths.get(localStoragePath).resolve(filePath);
        Files.deleteIfExists(fileLocation);
        log.info("File deleted locally: {}", filePath);
    }

    private void deleteFileFromS3(String filePath) {
        AmazonS3 s3Client = createS3Client();
        s3Client.deleteObject(s3BucketName, filePath);
        log.info("File deleted from S3: {}", filePath);
    }

    public List<String> listFiles(String directory) throws IOException {
        switch (storageType.toLowerCase()) {
            case "s3":
            case "minio":
                return listFilesFromS3(directory);
            case "local":
            default:
                return listFilesLocally(directory);
        }
    }

    private List<String> listFilesLocally(String directory) throws IOException {
        Path directoryPath = Paths.get(localStoragePath).resolve(directory);
        if (!Files.exists(directoryPath)) {
            return List.of();
        }
        
        return Files.walk(directoryPath)
                .filter(Files::isRegularFile)
                .map(directoryPath::relativize)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    private List<String> listFilesFromS3(String directory) {
        AmazonS3 s3Client = createS3Client();
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(s3BucketName)
                .withPrefix(directory + "/");

        ListObjectsV2Result result = s3Client.listObjectsV2(listObjectsRequest);
        
        return result.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    public String generateSignedUrl(String filePath, int expirationMinutes) {
        AmazonS3 s3Client = createS3Client();
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * expirationMinutes;
        expiration.setTime(expTimeMillis);

        return s3Client.generatePresignedUrl(s3BucketName, filePath, expiration).toString();
    }

    private String generateUniqueFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(formatter);
        String extension = getFileExtension(originalFileName);
        String baseName = getFileNameWithoutExtension(originalFileName);
        
        return String.format("%s/%s_%d%s", timestamp, baseName, System.currentTimeMillis(), extension);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex != -1 ? fileName.substring(lastDotIndex) : "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex != -1 ? fileName.substring(0, lastDotIndex) : fileName;
    }
}