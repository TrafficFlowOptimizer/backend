package app.backend.controller.crossroad;

import app.backend.controller.Controller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CrossroadsUtilsTest {
    @Autowired
    CrossroadsUtils crossroadsUtils;
    @Autowired
    Controller controller;


    @Test
    void parseOutput() throws Exception {
        String crossroadId = controller.populateDb();
        crossroadId = crossroadId.split(";")[0];
        String result = Files.readString(Paths.get("templateOTResponse.json"));
        result = crossroadsUtils.parseOutput(result, crossroadId);
        System.out.println(result);
    }
}