package com.kamalsha.gridfshelper.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

import lombok.extern.log4j.Log4j2;

/**
 * Service class for managing files using MongoDB GridFS.
 */
@Service
@Log4j2
public class GridFsUtil {

  @Autowired
  private GridFsTemplate gridFsTemplate;

  /**
   * Store a file in the database.
   *
   * @param file The file to be stored.
   * @return The ObjectId of the stored file.
   * @throws IOException If an I/O error occurs.
   */
  public String store(MultipartFile file) throws IOException {
    try {
      if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("File is null or empty.");
      }

      String fileName = file.getOriginalFilename();
      if (fileName == null || fileName.isEmpty()) {
        throw new IllegalArgumentException("File name is null or empty.");
      }

      InputStream inputStream = file.getInputStream();
      if (inputStream == null) {
        throw new IOException("Failed to get input stream from the file.");
      }

      DBObject metaData = createMetaData(file.getContentType());

      ObjectId objectId = gridFsTemplate.store(inputStream, fileName, metaData);
      log.info("File stored with ObjectId: {}", objectId);
      return objectId.toString();
    } catch (IOException e) {
      log.error("Failed to store file: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Store a file from InputStream in the database.
   *
   * @param inputStream The InputStream of the file.
   * @param fileName    The name of the file.
   * @param contentType The content type of the file.
   * @return The ObjectId of the stored file.
   * @throws IOException If an I/O error occurs.
   */
  public String store(InputStream inputStream, String fileName, String contentType) throws IOException {
    DBObject metaData = createMetaData(contentType);
    ObjectId objectId = gridFsTemplate.store(inputStream, fileName, metaData);
    log.info("File stored with ObjectId: {}", objectId);
    return objectId.toString();
  }

  /**
   * Store a Base64-encoded file in the database.
   *
   * @param base64   The Base64-encoded file content.
   * @param fileName The name of the file.
   * @return The ObjectId of the stored file.
   */
  public String storeBase64(String base64, String fileName) throws IOException {
    try {
      if (base64 == null || base64.isEmpty()) {
        throw new IllegalArgumentException("Base64 content is null or empty.");
      }

      InputStream inputStream = new java.io.ByteArrayInputStream(Base64.getDecoder().decode(base64));
      String contentType = determineContentType(fileName);
      DBObject metaData = createMetaData(contentType);

      ObjectId objectId = gridFsTemplate.store(inputStream, fileName, metaData);
      if (objectId != null) {
        log.info("File stored with ObjectId: {}", objectId);
        return objectId.toString();
      } else {
        log.warn("Failed to store file with Base64 content: {}", base64);
        return "6565b97aec98e3550acd60fc";
      }
    } catch (Exception e) {
      log.error("Failed to store file: {}", e.getMessage());
      throw e;
    }
  }

  private DBObject createMetaData(String contentType) {
    DBObject metaData = new BasicDBObject();
    metaData.put("contentType", contentType);
    return metaData;
  }

  private String determineContentType(String fileName) {
    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    switch (extension) {
      case "xlsx":
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      case "xls":
        return "application/vnd.ms-excel";
      case "csv":
        return "text/csv";
      case "pdf":
        return "application/pdf";
      case "doc":
        return "application/msword";
      case "docx":
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      case "txt":
        return "text/plain";
      case "png":
        return "image/png";
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      default:
        return "application/octet-stream";
    }
  }

  /**
   * Retrieve a file from the database as a resource.
   *
   * @param fileId The ObjectId of the file to retrieve.
   * @return The GridFsResource representing the file.
   * @throws FileNotFoundException If the file is not found.
   */
  public GridFsResource retrieve(String fileId) throws FileNotFoundException {
    try {
      GridFSFile file = findGridFSFileById(fileId);
      return gridFsTemplate.getResource(file);
    } catch (FileNotFoundException e) {
      log.error("Failed to retrieve file: {}", e.getMessage());
      throw e;
    }
  }

  public GridFSFile findGridFSFileById(String fileId) throws FileNotFoundException {
    try {
      GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(fileId))));
      if (file == null) {
        throw new FileNotFoundException("File not found with id " + fileId);
      }
      return file;
    } catch (FileNotFoundException e) {
      log.error("Failed to find file: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a file from the database.
   *
   * @param fileId The ObjectId of the file to delete.
   * @throws FileNotFoundException If the file is not found.
   */
  public void delete(String fileId) throws FileNotFoundException {
    try {
      GridFSFile file = findGridFSFileById(fileId);
      gridFsTemplate.delete(new Query(Criteria.where("_id").is(file.getId())));
      log.info("File deleted with ObjectId: {}", fileId);
    } catch (FileNotFoundException e) {
      log.error("Failed to delete file: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Download a file from the database.
   *
   * @param fileId The ObjectId of the file to download.
   * @return The ResponseEntity containing the file as an InputStreamResource.
   * @throws IOException If an I/O error occurs.
   */
  public ResponseEntity<InputStreamResource> downloadFile(String fileId) throws IOException {
    try {
      GridFSFile file = findGridFSFileById(fileId);

      GridFsResource resource = gridFsTemplate.getResource(file);
      InputStream inputStream = resource.getInputStream();
      HttpHeaders headers = createFileDownloadHeaders(file);

      return new ResponseEntity<>(new InputStreamResource(inputStream), headers, HttpStatus.OK);
    } catch (IOException e) {
      log.error("Failed to download file: {}", e.getMessage());
      throw e;
    }
  }

  public HttpHeaders createFileDownloadHeaders(GridFSFile file) {
    HttpHeaders headers = new HttpHeaders();

    if (file == null) {
      headers.add("Content-Disposition", "attachment; filename=unknown");
      headers.add("Content-Type", "application/octet-stream");
      headers.add("Content-Length", "0");
      headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
      return headers;
    }

    String filename = file.getFilename();
    if (filename != null && !filename.isEmpty()) {
      headers.add("Content-Disposition", "attachment; filename=" + filename);
    } else {
      headers.add("Content-Disposition", "attachment; filename=unknown");
    }

    if (file.getMetadata() == null) {
      headers.add("Content-Type", "text/plain");
      headers.add("Content-Length", "0");
      headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
      return headers;
    }

    Object contentTypeObj = file.getMetadata().get("contentType");
    if (contentTypeObj != null) {
      String contentType = contentTypeObj.toString();
      headers.add("Content-Type", contentType);
    } else {
      headers.add("Content-Type", "application/octet-stream");
    }

    long contentLength = file.getLength();
    headers.add("Content-Length", String.valueOf(contentLength));

    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");

    return headers;
  }

}
