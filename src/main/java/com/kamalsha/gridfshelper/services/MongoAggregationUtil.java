package com.kamalsha.gridfshelper.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class provides methods for performing aggregation operations on MongoDB
 * collections.
 */
@Service
@Log4j2
public class MongoAggregationUtil {

  @Autowired
  private MongoTemplate mongoTemplate;

  /**
   * Executes an aggregation operation on a MongoDB collection.
   *
   * @param collectionName the name of the collection to perform the aggregation
   *                       on
   * @param pipeline       the list of pipeline stages to apply to the collection
   * @return a list of documents resulting from the aggregation operation
   */
  public List<Document> executeAggregation(String collectionName, List<Document> pipeline) {
    try {
      log.info("Executing aggregation on collection: " + collectionName);
      return mongoTemplate.getCollection(collectionName).aggregate(pipeline).allowDiskUse(true)
          .into(new ArrayList<>());
    } catch (Exception e) {
      log.error("Error executing aggregation on collection: " + collectionName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Executes an aggregation operation on a MongoDB collection using the specified
   * pipeline.
   *
   * @param collectionName The name of the collection to perform the aggregation
   *                       on.
   * @param pipelineJSON   The JSON string representing the aggregation pipeline.
   * @param args           Optional arguments to be used in the pipeline.
   * @return A list of documents resulting from the aggregation operation.
   * @throws Exception If an error occurs during the aggregation operation.
   */
  public List<Document> executeAggregation(String collectionName, String pipelineJSON, String... args) {
    try {
      log.info("Executing aggregation on collection: " + collectionName);
      return executeAggregation(collectionName, getAggregationPipeline(pipelineJSON, args));
    } catch (Exception e) {
      log.error("Error executing aggregation on collection: " + collectionName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Executes an aggregation operation on the specified collection using the
   * provided pipeline and arguments.
   *
   * @param collectionName The name of the collection to aggregate on.
   * @param pipelineJSON   The JSON string representing the aggregation pipeline.
   * @param args           Additional arguments to be used in the pipeline.
   * @return A list of documents resulting from the aggregation operation.
   * @throws Exception If an error occurs during the aggregation operation.
   */
  public List<Document> executeAggregation(String collectionName, String pipelineJSON, Map<String, Object> args) {
    try {
      log.info("Executing aggregation on collection: " + collectionName);
      return executeAggregation(collectionName, getAggregationPipeline(pipelineJSON, args));
    } catch (Exception e) {
      log.error("Error executing aggregation on collection: " + collectionName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Aggregates data from a MongoDB collection using a pipeline defined in a file.
   *
   * @param collectionName   the name of the MongoDB collection to aggregate data
   *                         from
   * @param pipelineFileName the name of the file containing the pipeline
   *                         definition
   * @param args             optional arguments to be passed to the pipeline
   * @return a list of documents representing the aggregated data
   * @throws Exception if an error occurs during the aggregation process
   */
  public List<Document> executeAggregationFromFile(String collectionName, String pipelineFileName, String... args) {
    try {
      log.info("Executing aggregation from file on collection: " + collectionName);
      return executeAggregation(collectionName, getAggregationPipelineFromFile(pipelineFileName, args));
    } catch (Exception e) {
      log.error("Error executing aggregation from file on collection: " + collectionName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Aggregates data from a MongoDB collection using a pipeline defined in a file.
   *
   * @param collectionName   the name of the MongoDB collection to aggregate data
   *                         from
   * @param pipelineFileName the name of the file containing the pipeline
   *                         definition
   * @param args             additional arguments to be passed to the pipeline
   * @return a list of documents representing the aggregated data
   * @throws Exception if an error occurs during the aggregation process
   */
  public List<Document> executeAggregationFromFile(String collectionName, String pipelineFileName,
      Map<String, Object> args) {
    try {
      log.info("Executing aggregation from file on collection: " + collectionName);
      return executeAggregation(collectionName, getAggregationPipelineFromFile(pipelineFileName, args));
    } catch (Exception e) {
      log.error("Error executing aggregation from file on collection: " + collectionName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Retrieves the pipeline for MongoDB aggregation with the provided JSON
   * template and arguments.
   *
   * @param pipelineJSON The JSON template for the pipeline.
   * @param args         The arguments to replace in the JSON template.
   * @return The list of documents resulting from the aggregation pipeline.
   * @throws Exception if an error occurs during the aggregation process.
   */
  public List<Document> getAggregationPipeline(String pipelineJSON, String... args) {
    try {
      String[] pipeline = { pipelineJSON };
      Pattern pattern = Pattern.compile("##(.*?)##");
      for (int i = 0; i < args.length; i++) {
        pipeline[0] = pipeline[0].replaceFirst(pattern.pattern(), args[i]);
      }

      return parseJsonToDocuments(pipeline[0]);
    } catch (Exception e) {
      log.error("Error getting aggregation pipeline", e);
      return new ArrayList<>();
    }
  }

  /**
   * Retrieves the pipeline for MongoDB aggregation based on the provided JSON
   * string and arguments.
   *
   * @param pipelineJSON The JSON string representing the pipeline.
   * @param args         The map of arguments to be replaced in the pipeline JSON.
   * @return The list of documents resulting from the aggregation pipeline.
   * @throws Exception if an error occurs during the parsing of the pipeline JSON.
   */
  public List<Document> getAggregationPipeline(String pipelineJSON, Map<String, Object> args) {
    try {
      for (Map.Entry<String, Object> entry : args.entrySet()) {
        pipelineJSON = pipelineJSON.replace("##" + entry.getKey() + "##", entry.getValue().toString());
      }

      return parseJsonToDocuments(pipelineJSON);
    } catch (Exception e) {
      log.error("Error getting aggregation pipeline", e);
      return new ArrayList<>();
    }
  }

  /**
   * Reads a pipeline JSON file and returns the result of executing the pipeline.
   *
   * @param pipelineFileName The name of the pipeline JSON file.
   * @param args             Optional arguments to be passed to the pipeline.
   * @return A list of documents representing the result of executing the
   *         pipeline.
   * @throws Exception if an error occurs while reading the pipeline file or
   *                   executing the pipeline.
   */
  public List<Document> getAggregationPipelineFromFile(String pipelineFileName, String... args) {
    try {
      String pipelineJSON = readPipelineFile(pipelineFileName);
      return getAggregationPipeline(pipelineJSON, args);
    } catch (Exception e) {
      log.error("Error getting aggregation pipeline from file", e);
      return new ArrayList<>();
    }
  }

  /**
   * Reads a pipeline JSON file and returns the pipeline as a list of documents.
   *
   * @param pipelineFileName the name of the pipeline JSON file
   * @param args             a map of arguments to be used in the pipeline
   * @return the pipeline as a list of documents
   * @throws Exception if an error occurs while reading the pipeline file or
   *                   parsing the JSON
   */
  public List<Document> getAggregationPipelineFromFile(String pipelineFileName, Map<String, Object> args) {
    try {
      String pipelineJSON = readPipelineFile(pipelineFileName);
      return getAggregationPipeline(pipelineJSON, args);
    } catch (Exception e) {
      log.error("Error getting aggregation pipeline from file", e);
      return new ArrayList<>();
    }
  }

  /**
   * Reads the content of a file and returns it as a String.
   *
   * @param pipelineFileName the name of the file containing the pipeline JSON
   * @return the content of the file as a String
   * @throws Exception if the file is not found or an error occurs while reading
   *                   the file
   */
  public String readPipelineFile(String pipelineFileName) {
    try {
      String pipelineJSON = "";
      try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pipelineFileName)) {
        if (inputStream != null) {
          pipelineJSON = new String(inputStream.readAllBytes());
        } else {
          throw new RuntimeException("Pipeline file not found: " + pipelineFileName);
        }
      }
      return pipelineJSON;
    } catch (Exception e) {
      log.error("Error reading pipeline file", e);
      return "";
    }
  }

  /**
   * Parses a JSON string into a list of Document objects.
   *
   * @param json the JSON string to parse
   * @return a list of Document objects parsed from the JSON string
   * @throws Exception if an error occurs during parsing
   */
  public List<Document> parseJsonToDocuments(String json) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Document.class));
    } catch (Exception e) {
      log.error("Error parsing JSON to documents", e);
      return new ArrayList<>();
    }
  }
}