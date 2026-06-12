# AI Facial Demographic Analysis Portal

An end-to-end, real-time web application that captures live webcam footage and utilizes deep learning to estimate the age, gender, and race/ethnicity of a detected face.

This project demonstrates a high-performance streaming architecture, passing compressed video frames over WebSockets to a Java Spring Boot backend, which dynamically processes the images using two distinct AI engines.

## Key Features
    - Real-Time Processing: Captures webcam frames every 150 milliseconds for a smooth, continuous live feed.
    - Dual-Engine AI Pipeline: * Engine 1 (Face Detection): Utilizes a PyTorch ResNet50 model to scan the frame, detect a face, and crop the bounding box.
    - Dynamic UI: A responsive, dark-themed dashboard that updates prediction confidence bars in real-time.
    - Optimized WebSockets: Custom WebSocket configuration with expanded 2MB memory buffers to handle continuous Base64 image streaming.

## Tech stack
### Front-end
    - HTML5 & CSS3: Responsive UI with real-time progress bar visualizations.
    - Vanilla JavaScript: Hardware integration via navigator.mediaDevices.getUserMedia and Canvas API for image capture and JPEG compression.
    - WebSockets: Persistent, low-latency bidirectional connection to the backend.
### Back-end
    - Java & Spring Boot: Core application framework and web server hosting.
    - Spring WebSockets: Handles the /stream endpoint and incoming Base64 image payloads.
    - Deep Java Library (DJL): Abstraction layer to run deep learning models natively in Java.
### AI models
    - PyTorch: Used for precise object/face detection and bounding box extraction.
    - ONNX: A multi-task neural network optimized with 4 threads to predict demographics concurrently.
## How It Works
    1. Capture: The browser requests webcam access and draws the video feed to a hidden HTML canvas.

    2. Stream: Every 150ms, the canvas frame is compressed into a JPEG, converted to Base64, and sent to the server via WebSockets.

    3. Detect: FaceStreamHandler.java receives the frame and passes it to the FaceDetectionService. If a face is found, it calculates exact pixel coordinates and isolates the face.

    4. Translate & Predict: The isolated face is resized to 224x224, normalized, and converted into an N-Dimensional Array (NDArray) by FairFaceTranslator.java. The AttributeEstimationService then runs the ONNX prediction.

    5. Render: The backend formats the demographic results and confidence scores into a JSON payload, sending it back to the client to update the UI instantly.
## Setup and Installation
### Prerequisites
    - Java Development Kit (JDK) 17 or higher.

    - Maven or Gradle (depending on your build tool).

    - A working webcam.
### Running application
    1. Clone the repository
    2. Add the ONNX Model: download the fairface_multi_task.onnx
    3. Start the Spring Boot Server: ./mvnw spring-boot:run
    4. Access the Portal: http://localhost:8080
