package com.facedetect;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AttributeEstimationService {

    private ZooModel<Image, DemographicResult> model;
    private Predictor<Image, DemographicResult> predictor;

    @PostConstruct
    public void init() throws ModelException, IOException {
        System.out.println("=> Starting ONNX Demographic Engine...");
        
        Path modelDir = Paths.get("models/fairface");

        Criteria<Image, DemographicResult> criteria = Criteria.builder()
                .setTypes(Image.class, DemographicResult.class)
                .optModelPath(modelDir)
                .optModelName("fairface_multi_task.onnx")
                .optEngine("OnnxRuntime")
                .optTranslator(new FairFaceTranslator())
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();
        
        System.out.println("=> ONNX Demographic Engine Loaded Successfully!");
    }

    @PreDestroy
    public void cleanup() {
        if (predictor != null) predictor.close();
        if (model != null) model.close();
    }

    public DemographicResult estimateAttributes(Image faceImage) throws Exception {
        return predictor.predict(faceImage);
    }
}