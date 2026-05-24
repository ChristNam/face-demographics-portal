package com.facedetect;

import ai.djl.modality.cv.Image;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Component
public class FaceStreamHandler extends TextWebSocketHandler {

    // 1. Declare both of our AI Services
    private final FaceDetectionService faceDetectionService;
    private final AttributeEstimationService attributeEstimationService;

    // 2. Spring Boot automatically injects BOTH services into this handler
    public FaceStreamHandler(FaceDetectionService faceDetectionService, 
                             AttributeEstimationService attributeEstimationService) {
        this.faceDetectionService = faceDetectionService;
        this.attributeEstimationService = attributeEstimationService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String base64ImagePayload = message.getPayload();

        try {
            // Convert the network string back into a real Image
            byte[] imageBytes = Base64.getDecoder().decode(base64ImagePayload);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bais);

            if (bufferedImage != null) {
                // ENGINE 1: Find the face and crop it
                Image croppedFace = faceDetectionService.detectAndCropFace(bufferedImage);

                String responseJson;

                if (croppedFace != null) {
                    // ENGINE 2: A face was found! Pass the cropped face to the ONNX Demographics Model
                    DemographicResult attributes = attributeEstimationService.estimateAttributes(croppedFace);

                    // Build the JSON response using the REAL AI data!
                    responseJson = String.format(
                            "{\"age\":\"%s\",\"ageConfidence\":%f,\"gender\":\"%s\",\"genderConfidence\":%f,\"race\":\"%s\",\"raceConfidence\":%f}",
                            attributes.age, attributes.ageConfidence,
                            attributes.gender, attributes.genderConfidence,
                            attributes.race, attributes.raceConfidence
                    );
                } else {
                    // No face found in this frame, reset the dashboard
                    responseJson = "{"
                            + "\"age\":\"No Face\","
                            + "\"ageConfidence\":0.0,"
                            + "\"gender\":\"Looking...\","
                            + "\"genderConfidence\":0.0,"
                            + "\"race\":\"Waiting...\","
                            + "\"raceConfidence\":0.0"
                            + "}";
                }
                
                // Send the final dynamic data back to the browser UI
                session.sendMessage(new TextMessage(responseJson));
            }
        } catch (Exception e) {

            System.err.println("Error processing frame: " + e.getMessage());
            e.printStackTrace();
        }
    }
}