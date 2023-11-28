// package com.kamalsha.gridfshelper.services;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.util.ArrayList;
// import java.util.List;

// import org.bson.Document;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
// import org.springframework.data.mongodb.core.MongoTemplate;

// import com.mongodb.client.AggregateIterable;
// import com.mongodb.client.MongoCollection;

// @DataMongoTest
// public class MongoAggregationUtilTest {

// @Mock
// private MongoTemplate mongoTemplate;

// @InjectMocks
// private MongoAggregationUtil mongoAggregationUtil;

// public MongoAggregationUtilTest() {
// MockitoAnnotations.openMocks(this);
// }

// @Test
// @DisplayName("Given a collection name and pipeline, when executeAggregation
// is called, then it should return the result of the aggregation")
// public void
// givenCollectionNameAndPipeline_whenExecuteAggregationCalled_thenReturnAggregationResult()
// {
// // Given
// String collectionName = "testCollection";
// List<Document> pipeline = new ArrayList<>();
// Document resultDocument = new Document("result", "value");
// ArrayList<Document> expectedResult = new ArrayList<>();
// expectedResult.add(resultDocument);

// when(mongoTemplate.getCollection(collectionName)).thenReturn(mock(MongoCollection.class));
// when(mongoTemplate.getCollection(collectionName).aggregate(pipeline)).thenReturn(mock(AggregateIterable.class));
// when(mongoTemplate.getCollection(collectionName).aggregate(pipeline).allowDiskUse(true))
// .thenReturn(mock(AggregateIterable.class));
// when(mongoTemplate.getCollection(collectionName).aggregate(pipeline).allowDiskUse(true).into(new
// ArrayList<>()))
// .thenReturn(expectedResult);

// // When
// List<Document> result =
// mongoAggregationUtil.executeAggregation(collectionName, pipeline);

// // Then
// assertEquals(expectedResult, result);
// verify(mongoTemplate, times(1)).getCollection(collectionName);
// verify(mongoTemplate.getCollection(collectionName),
// times(1)).aggregate(pipeline);
// verify(mongoTemplate.getCollection(collectionName).aggregate(pipeline),
// times(1)).allowDiskUse(true);
// verify(mongoTemplate.getCollection(collectionName).aggregate(pipeline).allowDiskUse(true),
// times(1))
// .into(new ArrayList<>());
// }

// @Test
// @DisplayName("Given a collection name and pipeline, when an exception occurs
// during aggregation, then it should return an empty list")
// public void
// givenCollectionNameAndPipeline_whenExceptionOccursDuringAggregation_thenReturnEmptyList()
// {
// // Given
// String collectionName = "testCollection";
// List<Document> pipeline = new ArrayList<>();

// when(mongoTemplate.getCollection(collectionName)).thenReturn(mock(MongoCollection.class));
// when(mongoTemplate.getCollection(collectionName).aggregate(pipeline)).thenThrow(new
// RuntimeException());

// // When
// List<Document> result =
// mongoAggregationUtil.executeAggregation(collectionName, pipeline);

// // Then
// assertNotNull(result);
// assertTrue(result.isEmpty());
// verify(mongoTemplate, times(1)).getCollection(collectionName);
// verify(mongoTemplate.getCollection(collectionName),
// times(1)).aggregate(pipeline);
// }
// }