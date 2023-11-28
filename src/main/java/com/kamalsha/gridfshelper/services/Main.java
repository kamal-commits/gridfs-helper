package com.kamalsha.gridfshelper.services;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoAuditing
@EnableMongoRepositories
public class Main {

  public static void main(String[] args) {

    // GridFsService gridFsService = new GridFsService();
    // try {

    // gridFsService.storeBase64("SEVZIE1OQQ==", "testFile.txt");
    // } catch (Exception e) {
    // e.printStackTrace();
    // }

    System.out.println("Hello, World!");
  }
}
