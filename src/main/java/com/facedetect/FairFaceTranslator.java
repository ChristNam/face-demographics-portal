package com.facedetect;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class FairFaceTranslator implements Translator<Image, DemographicResult> {

    private static final String[] RACE_LABELS   = {"White", "Black", "Indian", "East Asian", "Southeast Asian", "Middle Eastern", "Latino"};
    private static final String[] GENDER_LABELS = {"Male", "Female"};
    private static final String[] AGE_LABELS    = {"0-2", "3-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70+"};

    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD  = {0.229f, 0.224f, 0.225f};

    @Override
    public Batchifier getBatchifier() {
        return null;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();

        BufferedImage original = (BufferedImage) input.getWrappedImage();
        BufferedImage resized = resizeImage(original, 224, 224);

        float[] floatData = toNormalizedCHW(resized);

        NDArray array = manager.create(floatData, new Shape(1, 3, 224, 224));
        array = array.toType(DataType.FLOAT32, false);

        return new NDList(array);
    }

    @Override
    public DemographicResult processOutput(TranslatorContext ctx, NDList list) {
        DemographicResult result = new DemographicResult();

        // 1. Process Race (Index 0)
        NDArray raceArr = list.get(0).squeeze();
        int raceIndex;
        if (raceArr.getDataType() == DataType.INT64 || raceArr.getDataType() == DataType.INT32) {
            // The model already ran ArgMax inside the ONNX file and returned the final index directly
            raceIndex = (int) raceArr.getLong();
            result.raceConfidence = 1.0f; // Softmax values are omitted by the model
        } else {
            // The model returned raw floating-point logits, calculate probabilities manually
            NDArray probabilities = raceArr.softmax(0);
            raceIndex = (int) probabilities.argMax().getLong();
            result.raceConfidence = probabilities.getFloat(raceIndex);
        }
        if (raceIndex >= 0 && raceIndex < RACE_LABELS.length) {
            result.race = RACE_LABELS[raceIndex];
        } else {
            result.race = "Unknown";
        }

        // 2. Process Gender (Index 1)
        NDArray genderArr = list.get(1).squeeze();
        int genderIndex;
        if (genderArr.getDataType() == DataType.INT64 || genderArr.getDataType() == DataType.INT32) {
            genderIndex = (int) genderArr.getLong();
            result.genderConfidence = 1.0f;
        } else {
            NDArray probabilities = genderArr.softmax(0);
            genderIndex = (int) probabilities.argMax().getLong();
            result.genderConfidence = probabilities.getFloat(genderIndex);
        }
        if (genderIndex >= 0 && genderIndex < GENDER_LABELS.length) {
            result.gender = GENDER_LABELS[genderIndex];
        } else {
            result.gender = "Unknown";
        }

        // 3. Process Age (Index 2)
        NDArray ageArr = list.get(2).squeeze();
        int ageIndex;
        if (ageArr.getDataType() == DataType.INT64 || ageArr.getDataType() == DataType.INT32) {
            ageIndex = (int) ageArr.getLong();
            result.ageConfidence = 1.0f;
        } else {
            NDArray probabilities = ageArr.softmax(0);
            ageIndex = (int) probabilities.argMax().getLong();
            result.ageConfidence = probabilities.getFloat(ageIndex);
        }
        if (ageIndex >= 0 && ageIndex < AGE_LABELS.length) {
            result.age = AGE_LABELS[ageIndex];
        } else {
            result.age = "Unknown";
        }

        return result;
    }

    private BufferedImage resizeImage(BufferedImage src, int width, int height) {
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return dst;
    }

    private float[] toNormalizedCHW(BufferedImage img) {
        BufferedImage bgr = new BufferedImage(224, 224, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = bgr.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        byte[] pixels = ((DataBufferByte) bgr.getRaster().getDataBuffer()).getData();

        int size = 224 * 224;
        float[] chw = new float[3 * size];

        for (int i = 0; i < size; i++) {
            float b  = (pixels[i * 3]     & 0xFF) / 255.0f;
            float g2 = (pixels[i * 3 + 1] & 0xFF) / 255.0f;
            float r  = (pixels[i * 3 + 2] & 0xFF) / 255.0f;

            chw[i]            = (r  - MEAN[0]) / STD[0]; 
            chw[size + i]     = (g2 - MEAN[1]) / STD[1]; 
            chw[2 * size + i] = (b  - MEAN[2]) / STD[2]; 
        }

        return chw;
    }
}