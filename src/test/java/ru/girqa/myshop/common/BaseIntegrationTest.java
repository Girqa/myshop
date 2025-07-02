package ru.girqa.myshop.common;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
public class BaseIntegrationTest {

}
