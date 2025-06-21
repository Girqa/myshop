package ru.girqa.myshop;

import org.springframework.boot.SpringApplication;

public class TestMyshopApplication {

  public static void main(String[] args) {
    SpringApplication.from(MyshopApplication::main).with(TestcontainersConfiguration.class)
        .run(args);
  }

}
