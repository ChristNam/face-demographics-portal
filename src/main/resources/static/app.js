const video = document.getElementById('webcam')
const canvas = document.getElementById('captureCanvas')
const ctx = canvas.getContext('2d')
const statusBadge = document.getElementById('connectionStatus')

let socket;
const frameIntervalTime = 150; // capure frame every 150ms

// ask for webcam access and start streaming
async function startWebcam() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ 
            video: {width: 640, height: 480 },
            audio: false
    });
    video.srcObject = stream;
    connectToServer();
    } catch (err) {
        console.error('Error accessing webcam: ', err);
        alert("Could not access webcam. Please allow access and refresh the page.");
    }
}
// Websocket to backend pipeline 
function connectToServer(){
    socket = new WebSocket('ws://localhost:8080/stream');

    socket.onopen = () => {
        statusBadge.textContent = "Live AI processing Active";
        statusBadge.style.backgroundColor = "#4CAF50"; // green
        startFrameCapture();
    };

    let reconnectTimer = null;

    socket.onclose = () => {
    statusBadge.textContent = "Server Disconnected";
    statusBadge.style.backgroundColor = "#f44336";
    if (!reconnectTimer) {
        reconnectTimer = setTimeout(() => {
            reconnectTimer = null;
            connectToServer();
        }, 5000);
    }
    };


    // get predictions back from Java and update the UI
    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        // age dashbaord elements
        document.getElementById('ageValue').textContent = data.age;
        document.getElementById('ageProgress').style.width = (data.ageConfidence * 100) + '%';
        // gender ashboard elements
        document.getElementById('genderValue').textContent = data.gender;
        document.getElementById('genderProgress').style.width = (data.genderConfidence * 100) + '%';
        // Race dashboard elements
        document.getElementById('raceValue').textContent = data.race;
        document.getElementById('raceProgress').style.width = (data.raceConfidence * 100) + '%';
    };

}

// capture Canvas Frames, compress to JPEG data matrices
function startFrameCapture() {
    setInterval(() => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

            const base64Image = canvas.toDataURL('image/jpeg', 0.7); // compress to JPEG with quality 0.7
            const rawBytesString = base64Image.split(',')[1]; // remove data URL prefix

            socket.send(rawBytesString); // send raw JPEG bytes to server
        }
    }, frameIntervalTime);
}

window.addEventListener('DOMContentLoaded', startWebcam);