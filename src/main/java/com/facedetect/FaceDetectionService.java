package com.facedetect;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;


@Service
public class FaceDetectionService {

    private ZooModel<Image, DetectedObjects> model;
    private Predictor<Image, DetectedObjects> predictor;

    // @PostConstruct tells Spring Boot to run this method exactly once when the server starts up.
    @PostConstruct
    public void init() throws ModelException, IOException {
        System.out.println("=> Starting PyTorch Face Detection Engine...");
        
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
                .optFilter("backbone", "resnet50")
                .optEngine("PyTorch")
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();
        
        System.out.println("=> Face Detection Engine Loaded Successfully!");
    }

    // @PreDestroy cleans up heavy C++ memory when you shut down the server.
    @PreDestroy
    public void cleanup() {
        if (predictor != null) predictor.close();
        if (model != null) model.close();
    }

    /**
     * Takes a live webcam frame, finds the face, and crops it out.
     */
    public Image detectAndCropFace(BufferedImage bufferedImage) throws Exception {
        // 1. Convert the standard Java image into a DJL AI Image
        Image img = ImageFactory.getInstance().fromImage(bufferedImage);

        // 2. Run the AI prediction!
        DetectedObjects results = predictor.predict(img);
        List<DetectedObjects.Classification> items = results.items();

        // If no face is found in the frame, return null
        if (items.isEmpty()) {
            return null;
        }

        // 3. Grab the strongest face match and its bounding box
        DetectedObjects.DetectedObject item = (DetectedObjects.DetectedObject) items.get(0);
        BoundingBox box = item.getBoundingBox();
        Rectangle rect = box.getBounds();

        int width = img.getWidth();
        int height = img.getHeight();

        // 4. Calculate exact pixel coordinates
        int x = (int) (rect.getX() * width);
        int y = (int) (rect.getY() * height);
        int w = (int) (rect.getWidth() * width);
        int h = (int) (rect.getHeight() * height);

        // Safety clipping so we don't accidentally try to crop outside the image borders
        x = Math.max(0, x);
        y = Math.max(0, y);
        w = Math.min(width - x, w);
        h = Math.min(height - y, h);

        // 5. Return just the isolated face!
        BufferedImage wrapped = (BufferedImage) img.getWrappedImage();
        BufferedImage cropped = wrapped.getSubimage(x, y, w, h);
        return ImageFactory.getInstance().fromImage(cropped);
    }
}
