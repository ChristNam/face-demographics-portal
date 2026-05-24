package com.facedetect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        
        // Inject the ONNX thread requirement into the global Java System BEFORE Spring boots
        System.setProperty("ai.djl.onnxruntime.num_threads", "4");
        
        
        
        // This line tells Spring Boot to start up our web server
        SpringApplication.run(Application.class, args);
        System.out.println("=========================================");
        System.out.println("   Face Demographics Portal is LIVE!   ");
        System.out.println("   Access it at: http://localhost:8080   ");
        System.out.println("=========================================");
    }
}