package app.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageServiceTest {

    private final ImageService imageService;
    private final VideoService videoService;

    @Autowired
    public ImageServiceTest(ImageService imageService, VideoService videoService) {
        this.imageService = imageService;
        this.videoService = videoService;
    }

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @AfterEach
    public void cleanUpEach() {
        imageService.deleteAll();
    }

    @Test
    public void getImageById_properImage_correctImage() {
        String data = "imageString";
        String imageId = imageService.store(data);

        assertEquals(new String(imageService.getImage(imageId), StandardCharsets.UTF_8), data);
    }

    @Test
    public void deleteImageById_properImage_imageDeleted() {
        String data = "imageString";
        String imageId = imageService.store(data);
        imageService.deleteImageById(imageId);

        assertNull(imageService.getImage(imageId));
    }

    @Test
    public void deleteAllImages_onlyImages_allImagesDeleted() {
        String data1 = "imageString1";
        String data2 = "imageString2";

        String imageId1 = imageService.store(data1);
        String imageId2 = imageService.store(data2);

        assertEquals(new String(imageService.getImage(imageId1), StandardCharsets.UTF_8), data1);
        assertEquals(new String(imageService.getImage(imageId2), StandardCharsets.UTF_8), data2);

        imageService.deleteAll();

        assertNull(imageService.getImage(imageId1));
        assertNull(imageService.getImage(imageId2));
    }

    @Test
    public void deleteAllImages_imagesAndVideos_onlyImagesDeleted() {
        String data1 = "imageString1";
        String data2 = "imageString2";

        String imageId1 = imageService.store(data1);
        String imageId2 = imageService.store(data2);

        byte[] data = "data".getBytes();

        String videoId = videoService.store(new MockMultipartFile(
                "name",
                "originalFilename",
                "contentType",
                data
        ), "", "");

        imageService.deleteAll();

        assertNull(imageService.getImage(imageId1));
        assertNull(imageService.getImage(imageId2));
        assertArrayEquals(videoService.getVideo(videoId).getData(), data);
    }
}
