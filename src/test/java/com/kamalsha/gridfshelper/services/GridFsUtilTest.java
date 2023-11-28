package com.kamalsha.gridfshelper.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

@DataMongoTest
public class GridFsUtilTest {

  @Mock
  private GridFsTemplate gridFsTemplate;

  @InjectMocks
  private GridFsUtil gridFsUtil;

  public GridFsUtilTest() {
    MockitoAnnotations.openMocks(this);
  }

  String base64 = "SGVsbG8gV29ybGQh";
  String fileName = "hello.txt";

  @Test
  @DisplayName("Given a file, when store is called, then the file is stored in GridFS")
  public void givenFile_whenStoreCalled_thenFileIsStoredInGridFS() throws IOException {
    // Given
    MockMultipartFile file = new MockMultipartFile("hello", fileName, "text/plain", "Hello World !".getBytes());
    String originalFileName = file.getOriginalFilename();
    InputStream inputStream = file.getInputStream();
    DBObject metaData = new BasicDBObject();
    metaData.put("contentType", file.getContentType());
    ObjectId objectId = new ObjectId();

    when(gridFsTemplate.store(inputStream, originalFileName, metaData)).thenReturn(objectId);

    // When
    String result = gridFsUtil.store(file);

    // Then
    assertEquals(objectId.toString(), result);

    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<DBObject> metaDataCaptor = ArgumentCaptor.forClass(DBObject.class);

    verify(gridFsTemplate, times(1)).store(inputStreamCaptor.capture(), fileNameCaptor.capture(),
        metaDataCaptor.capture());

    assertEquals(inputStream, inputStreamCaptor.getValue());
    assertEquals(fileName, fileNameCaptor.getValue());
    assertEquals(metaData, metaDataCaptor.getValue());
  }

  @Test
  @DisplayName("Given an InputStream, fileName, and contentType, when store is called, then the file is stored in GridFS")
  public void givenInputStreamFileNameAndContentType_whenStoreCalled_thenFileIsStoredInGridFS() throws IOException {
    // Given
    InputStream inputStream = new ByteArrayInputStream("Hello, World!".getBytes());
    String contentType = "text/plain";
    DBObject metaData = new BasicDBObject();
    metaData.put("contentType", contentType);
    ObjectId objectId = new ObjectId();

    when(gridFsTemplate.store(inputStream, fileName, metaData)).thenReturn(objectId);

    // When
    String result = gridFsUtil.store(inputStream, fileName, contentType);

    // Then
    assertEquals(objectId.toString(), result);

    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<DBObject> metaDataCaptor = ArgumentCaptor.forClass(DBObject.class);

    verify(gridFsTemplate, times(1)).store(inputStreamCaptor.capture(), fileNameCaptor.capture(),
        metaDataCaptor.capture());

    assertEquals(inputStream, inputStreamCaptor.getValue());
    assertEquals(fileName, fileNameCaptor.getValue());
    assertEquals(metaData, metaDataCaptor.getValue());
  }

  @Test
  @DisplayName("Given a Base64-encoded file and fileName, when storeBase64 is called, then the file is stored in GridFS")
  public void givenBase64AndFileName_whenStoreBase64Called_thenFileIsStoredInGridFS() {
    // Given
    String contentType = "text/plain";
    ObjectId objectId = new ObjectId();
    DBObject metaData = new BasicDBObject();
    metaData.put("contentType", contentType);
    when(gridFsTemplate.store(any(InputStream.class), eq(fileName), eq(metaData))).thenReturn(objectId);

    // When
    String result;
    try {
      result = gridFsUtil.storeBase64(base64, fileName);
    } catch (IOException e) {
      e.printStackTrace();
      result = "";
    }

    // Then
    assertEquals(objectId.toString(), result);

    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<DBObject> metaDataCaptor = ArgumentCaptor.forClass(DBObject.class);

    verify(gridFsTemplate, times(1)).store(inputStreamCaptor.capture(), fileNameCaptor.capture(),
        metaDataCaptor.capture());

    assertEquals(fileName, fileNameCaptor.getValue());
    assertEquals(metaData, metaDataCaptor.getValue());
  }

  @Test
  @DisplayName("Given a fileId, when retrieve is called, then the file is retrieved from GridFS")
  public void givenFileId_whenRetrieveCalled_thenFileIsRetrievedFromGridFS() throws FileNotFoundException {
    String fileId = saveFile();
    GridFSFile file = mock(GridFSFile.class);
    GridFsResource resource = mock(GridFsResource.class);

    when(gridFsTemplate.findOne(any(Query.class))).thenReturn(file);
    when(gridFsTemplate.getResource(file)).thenReturn(resource);

    // When
    GridFsResource result = gridFsUtil.retrieve(fileId);

    // Then
    assertEquals(resource, result);
  }

  @Test
  @DisplayName("Given a fileId, when delete is called, then the file is deleted from GridFS")
  public void givenFileId_whenDeleteCalled_thenFileIsDeletedFromGridFS() throws FileNotFoundException {
    String fileId = saveFile();

    GridFSFile file = mock(GridFSFile.class);

    when(gridFsTemplate.findOne(any(Query.class))).thenReturn(file);

    // When
    gridFsUtil.delete(fileId);

    // Then
    verify(gridFsTemplate, times(1)).delete(any(Query.class));
  }

  private String saveFile() {
    String contentType = "text/plain";
    ObjectId objectId = new ObjectId();
    DBObject metaData = new BasicDBObject();
    metaData.put("contentType", contentType);
    when(gridFsTemplate.store(any(InputStream.class), eq(fileName), eq(metaData))).thenReturn(objectId);

    // Given
    String fileId;
    try {
      fileId = gridFsUtil.storeBase64(base64, fileName);
    } catch (IOException e) {
      e.printStackTrace();
      fileId = "";
    }
    return fileId;
  }
}
