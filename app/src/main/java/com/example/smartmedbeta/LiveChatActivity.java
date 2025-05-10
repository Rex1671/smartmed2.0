package com.example.smartmedbeta;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LiveChatActivity extends AppCompatActivity {

    private TextureView textureView;
    private Button captureButton;
    private Button startButton;
    private Button stopButton;
    private TextView chatLog;
    private ImageView statusIndicator;

    private String currentFrameB64 = null;
    private WebSocketClient webSocket = null;
    private boolean isRecording = false;
    private AudioRecord audioRecord = null;
    private final List<Short> pcmData = new ArrayList<>();


    private final String MODEL = "models/gemini-2.0-flash-exp";
    private final String API_KEY = "AIzaSyCWr7fyy5voqODSyr6h3rhbGT4pWN-IdHI";
    private final String HOST = "generativelanguage.googleapis.com";
    private final String URL = "wss://" + HOST + "/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=" + API_KEY;

    private final int CAMERA_REQUEST_CODE = 100;
    private final int AUDIO_REQUEST_CODE = 200;
    private final int AUDIO_SAMPLE_RATE = 24000;
    private final int RECEIVE_SAMPLE_RATE = 24000;
    private final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int AUDIO_BUFFER_SIZE = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_ENCODING);

    private final List<byte[]> audioQueue = new ArrayList<>();
    private boolean isPlaying = false;
    private AudioTrack audioTrack = null;
    private TextView connectionStatusText;
    private final int MAX_IMAGE_DIMENSION = 1024;
    private final int JPEG_QUALITY = 70;
    private long lastImageSendTime = 0;
    private final long IMAGE_SEND_INTERVAL = 3000;
    private boolean isConnected = false;
    private boolean isSpeaking = false;

    private CameraDevice cameraDevice = null;
    private CameraCaptureSession cameraCaptureSession = null;
    private CaptureRequest.Builder captureRequestBuilder = null;
    private ImageReader imageReader = null;
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private String cameraId;
    private Size previewSize;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    private boolean isCameraActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_chat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        textureView = findViewById(R.id.textureView);
        captureButton = findViewById(R.id.captureButton);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        chatLog = findViewById(R.id.chatLog);
        statusIndicator = findViewById(R.id.statusIndicator);
        updateStatusIndicator();    connectionStatusText = findViewById(R.id.connectionStatusText);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCameraActive) {
                    stopCameraPreview();
                    captureButton.setText("Start Capture");
                    isCameraActive = false;
                } else {
                    if (textureView.isAvailable()) {
                        startCameraPreview();
                        captureButton.setText("Stop Capture");
                        isCameraActive = true;
                    } else {
                        textureView.setSurfaceTextureListener(surfaceTextureListener);
                    }
                }
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRecordAudioPermission();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudioInput();
            }
        });

        connect();

        cameraThread = new HandlerThread("CameraThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            startCameraPreview();
            captureButton.setText("Stop Capture");
            isCameraActive = true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            stopCameraPreview();
            captureButton.setText("Start Capture");
            isCameraActive = false;
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
    };

    private void startCameraPreview() {
        checkCameraPermissionForPreview();
    }

    private void stopCameraPreview() {
        closeCamera();
        if (textureView.getSurfaceTexture() != null) {
            Surface surface = new Surface(textureView.getSurfaceTexture());
            Canvas canvas = null;
            try {
                canvas = surface.lockCanvas(null);
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    surface.unlockCanvasAndPost(canvas);
                }
                surface.release();
            }
        }
    }

    private void checkCameraPermissionForPreview() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            openCameraForPreview();
        }
    }

    private void openCameraForPreview() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) return;
            previewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            imageReader = ImageReader.newInstance(
                    MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION,
                    ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(imageAvailableListener, cameraHandler);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                return;
            }
            cameraManager.openCamera(cameraId, cameraStateCallback, cameraHandler);
        } catch (CameraAccessException e) {
            Log.e("Camera", "Error opening camera", e);
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            Log.e("Camera", "Camera error: " + error);
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            if (surfaceTexture != null) {
                surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            }
            Surface previewSurface = new Surface(surfaceTexture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(LiveChatActivity.this, "Configuration failed", Toast.LENGTH_SHORT).show();
                        }
                    }, cameraHandler);
        } catch (CameraAccessException e) {
            Log.e("Camera", "Error creating preview session", e);
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) return;
        if (captureRequestBuilder != null) {
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        }
        HandlerThread thread = new HandlerThread("UpdatePreview");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
        } catch (CameraAccessException e) {
            Log.e("Camera", "Error starting preview repeat request", e);
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private final ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastImageSendTime >= IMAGE_SEND_INTERVAL) {
                Image image = reader.acquireLatestImage();
                if (image == null) return;
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                image.close();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processAndSendImage(bytes);
                    }
                }).start();

                lastImageSendTime = currentTime;
                Log.d("ImageCapture", "Image processed and sent based on time interval");
            } else {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    image.close();
                }
                Log.d("ImageCapture", "Image capture skipped: Not enough time elapsed");
            }
        }
    };

    private void processAndSendImage(byte[] imageBytes) {
        String currentTime = timeFormat.format(new Date());
        Log.d("ImageCapture", "Image processed and sending at: " + currentTime);

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        Bitmap scaledBitmap = scaleBitmap(bitmap, MAX_IMAGE_DIMENSION);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream);

        String b64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT | Base64.NO_WRAP);
        sendMediaChunk(b64Image, "image/jpeg");

        scaledBitmap.recycle();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }

        int newWidth;
        int newHeight;
        if (width > height) {
            float ratio = (float) width / maxDimension;
            newWidth = maxDimension;
            newHeight = Math.round(height / ratio);
        } else {
            float ratio = (float) height / maxDimension;
            newHeight = maxDimension;
            newWidth = Math.round(width / ratio);
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraPreview();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case AUDIO_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAudioInput();
                } else {
                    Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        } else {
            startAudioInput();
        }
    }

    private void connect() {
        Log.d("WebSocket", "Connecting to: " + URL);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            webSocket = new WebSocketClient(new URI(URL), new Draft_6455(), headers, 0) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d("WebSocket", "Connected. Server handshake: " + (handshakedata != null ? handshakedata.getHttpStatus() : ""));
                    isConnected = true;
                    updateStatusIndicator();
                  sendInitialSetupMessage();
//                    sendPreRecordedAudio();
//                    enableVoiceButton();
                }

                @Override
                public void onMessage(String message) {
                    Log.d("WebSocket", "Message Received: " + message);
                    receiveMessage(message);
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    if (bytes != null) {
                        String message = new String(bytes.array(), StandardCharsets.UTF_8);
                        receiveMessage(message);
                    }
                }

                @Override

                public void onClose(int code, String reason, boolean remote) {
                    Log.d("WebSocket", "Connection Closed: Code = " + code + ", Reason = " + reason + ", Remote = " + remote);
                    isConnected = false;
                    updateStatusIndicator();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LiveChatActivity.this, "Connection closed: " + reason, Toast.LENGTH_LONG).show();

                            connectionStatusText.setText("Disconnected: " + reason);
                            connectionStatusText.setTextColor(Color.RED);
                        }
                    });
                }



                @Override
                public void onError(Exception ex) {
                    Log.e("WebSocket", "Error: " + (ex != null ? ex.getMessage() : "Unknown error"));
                    isConnected = false;
                    updateStatusIndicator();
                }
            };
            webSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendInitialSetupMessage() {
        Log.d("WebSocket", "Sending initial setup message");
        try {
            JSONObject setupMessage = new JSONObject();
            JSONObject setup = new JSONObject();
            JSONObject generationConfig = new JSONObject();
            JSONArray responseModalities = new JSONArray();
            responseModalities.put("AUDIO");
            generationConfig.put("response_modalities", responseModalities);
            setup.put("model", MODEL);
            setup.put("generation_config", generationConfig);
            setupMessage.put("setup", setup);
            Log.d("WebSocket", "Sending config payload: " + setupMessage.toString());
            if (webSocket != null) {
                webSocket.send(setupMessage.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void sendPreRecordedAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.smartmed_intro);
                    if (afd == null) {
                        Log.e("Audio", "❌ Pre-recorded audio file NOT found in raw folder!");
                        updateStatusText("❌ Pre-recorded audio file not found!");
                        return;
                    }
                    Log.d("Audio", "✅ Found pre-recorded MP3 file in raw folder.");
                    updateStatusText("✅ Found pre-recorded audio...");

                    byte[] pcmData = convertMp3ToPcm(afd);
                    if (pcmData == null || pcmData.length == 0) {
                        Log.e("Audio", "❌ PCM conversion failed or empty.");
                        updateStatusText("❌ Audio conversion failed.");
                        return;
                    }
                    Log.d("Audio", "✅ MP3 converted to PCM, size: " + pcmData.length + " bytes");
                    updateStatusText("🔄 Converting MP3 to PCM...");


                    String base64Audio = Base64.encodeToString(pcmData, Base64.DEFAULT | Base64.NO_WRAP);
                    Log.d("Audio", "🔁 Base64 encoding complete, sending to WebSocket...");
                    updateStatusText("📡 Sending audio to WebSocket...");

                    sendMediaChunk(base64Audio, "audio/pcm");
                    Log.d("Audio", "🎤 ✅ Pre-recorded PCM audio sent to WebSocket.");
                    updateStatusText("🎤 ✅ Audio sent successfully!");

                } catch (IOException e) {
                    Log.e("Audio", "❌ Error reading or processing pre-recorded audio", e);
                    updateStatusText("❌ Error sending pre-recorded audio!");
                }
            }
        }).start();
    }

    private byte[] convertMp3ToPcm(AssetFileDescriptor afd) throws IOException {
        Log.d("AudioConversion", "🔄 Converting MP3 to PCM...");
        updateStatusText("🔄 Converting MP3 to PCM...");

        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();

        MediaFormat format = extractor.getTrackFormat(0);
        String mime = format.getString(MediaFormat.KEY_MIME);
        Log.d("AudioConversion", "🎵 Detected Audio MIME type: " + mime);
        updateStatusText("🎵 Detected audio format: " + mime);

        if (!mime.startsWith("audio/")) {
            Log.e("AudioConversion", "❌ Not an audio file!");
            updateStatusText("❌ Error: Invalid audio format!");
            throw new IOException("Not an audio file");
        }

        MediaCodec codec = MediaCodec.createDecoderByType(mime);
        codec.configure(format, null, null, 0);
        codec.start();

        ByteArrayOutputStream pcmOutputStream = new ByteArrayOutputStream();
        ByteBuffer[] inputBuffers = codec.getInputBuffers();
        ByteBuffer[] outputBuffers = codec.getOutputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        extractor.selectTrack(0);
        Log.d("AudioConversion", "📡 Extracting and decoding audio...");
        updateStatusText("📡 Extracting and decoding audio...");

        while (true) {
            int inputIndex = codec.dequeueInputBuffer(10000);
            if (inputIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputIndex];
                int sampleSize = extractor.readSampleData(inputBuffer, 0);
                if (sampleSize < 0) {
                    Log.d("AudioConversion", "✅ End of MP3 file reached.");
                    codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                    extractor.advance();
                }
            }

            int outputIndex = codec.dequeueOutputBuffer(info, 10000);
            if (outputIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputIndex];
                byte[] chunk = new byte[info.size];
                outputBuffer.get(chunk);
                pcmOutputStream.write(chunk);
                codec.releaseOutputBuffer(outputIndex, false);
            }

            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }
        }

        codec.stop();
        codec.release();
        extractor.release();

        Log.d("AudioConversion", "✅ PCM conversion complete, size: " + pcmOutputStream.size() + " bytes.");
        updateStatusText("✅ PCM conversion complete.");
        return pcmOutputStream.toByteArray();
    }

    private void updateStatusText(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView connectionStatusText = findViewById(R.id.connectionStatusText);
                connectionStatusText.setText(message);
            }
        });
    }



    private void enableVoiceButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startButton.setEnabled(true);
                Toast.makeText(LiveChatActivity.this, "You can now speak!", Toast.LENGTH_SHORT).show();
                startAudioInput();
            }
        });
    }



//
//    private void sendInitialContext() {
//        if (webSocket == null || !isConnected) return;
//
//        try {
//            JSONObject message = new JSONObject();
//            message.put("text_input", "You are an AI Medical Advisor. Your task is to assist patients with medical inquiries. Ask follow-up questions and provide possible diagnoses while reminding the user that this is not a substitute for a real doctor.");
//            webSocket.send(message.toString());
//            Log.d("WebSocket", "Sent initial AI context.");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    private void sendMediaChunk(String b64Data, String mimeType) {
        if (!isConnected) {
            Log.d("WebSocket", "WebSocket not connected");
            return;
        }
        try {
            JSONObject mediaChunk = new JSONObject();
            mediaChunk.put("mime_type", mimeType);
            mediaChunk.put("data", b64Data);

            JSONArray mediaChunks = new JSONArray();
            mediaChunks.put(mediaChunk);

            JSONObject realtimeInput = new JSONObject();
            realtimeInput.put("media_chunks", mediaChunks);

            JSONObject msg = new JSONObject();
            msg.put("realtime_input", realtimeInput);

            String jsonString = msg.toString();
             Log.d("WebSocket", "Sending media chunk (MIME: " + mimeType + "): " + jsonString);
            webSocket.send(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessage(String message) {
        if (message == null) return;
        try {
            JSONObject messageData = new JSONObject(message);
            if (messageData.has("serverContent")) {
                JSONObject serverContent = messageData.getJSONObject("serverContent");
                if (serverContent.has("modelTurn")) {
                    JSONObject modelTurn = serverContent.getJSONObject("modelTurn");
                    if (modelTurn.has("parts")) {
                        JSONArray parts = modelTurn.getJSONArray("parts");
                        for (int i = 0; i < parts.length(); i++) {
                            JSONObject part = parts.getJSONObject(i);
                            if (part.has("text")) {
                                String text = part.getString("text");
                                displayMessage("GEMINI: " + text);
                            }
                            if (part.has("inlineData")) {
                                JSONObject inlineData = part.getJSONObject("inlineData");
                                if (inlineData.has("mimeType") &&
                                        inlineData.getString("mimeType").equals("audio/pcm;rate=24000")) {
                                    String audioData = inlineData.getString("data");
                                    injestAudioChunkToPlay(audioData);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("Receive", "Error parsing message", e);
        }
    }

    private byte[] base64ToArrayBuffer(String base64) {
        return Base64.decode(base64, Base64.DEFAULT);
    }

//    private float[] convertPCM16LEToFloat32(byte[] pcmData) {
//        short[] shortArray = asShortArray(pcmData);
//        float[] floatArray = new float[shortArray.length];
//        for (int i = 0; i < shortArray.length; i++) {
//            floatArray[i] = shortArray[i] / 32768f;
//        }
//        return floatArray;
//    }

    private short[] asShortArray(byte[] byteArray) {
        short[] shortArray = new short[byteArray.length / 2];
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < shortArray.length; i++) {
            shortArray[i] = byteBuffer.getShort();
        }
        return shortArray;
    }

    private void injestAudioChunkToPlay(final String base64AudioChunk) {
        if (base64AudioChunk == null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] arrayBuffer = base64ToArrayBuffer(base64AudioChunk);
                    synchronized (audioQueue) {
                        audioQueue.add(arrayBuffer);
                    }
                    if (!isPlaying) {
                        playNextAudioChunk();
                    }
                    Log.d("AudioChunk", "Audio chunk added to the queue");
                } catch (Exception e) {
                    Log.e("AudioChunk", "Error processing chunk", e);
                }
            }
        }).start();
    }

    private void playNextAudioChunk() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    byte[] chunk;
                    synchronized (audioQueue) {
                        if (!audioQueue.isEmpty()) {
                            chunk = audioQueue.remove(0);
                        } else {
                            break;
                        }
                    }
                    isPlaying = true;
                    playAudio(chunk);
                }
                isPlaying = false;

                synchronized (audioQueue) {
                    if (!audioQueue.isEmpty()) {
                        playNextAudioChunk();
                    }
                }
            }
        }).start();
    }

    private void playAudio(final byte[] byteArray) {
        if (audioTrack == null) {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    RECEIVE_SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioTrack.getMinBufferSize(RECEIVE_SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT),
                    AudioTrack.MODE_STREAM);
        }
        audioTrack.write(byteArray, 0, byteArray.length);
        audioTrack.play();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        Thread.sleep(10);
                    }
                    audioTrack.stop();
                      } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startAudioInput() {
        if (isRecording) return;
        isRecording = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL_CONFIG,
                AUDIO_ENCODING,
                AUDIO_BUFFER_SIZE);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("Audio", "AudioRecord initialization failed");
            return;
        }

        audioRecord.startRecording();
        Log.d("Audio", "Start Recording");
        isSpeaking = true;
        updateStatusIndicator();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRecording) {
                    short[] buffer = new short[AUDIO_BUFFER_SIZE];
                    int readSize = audioRecord.read(buffer, 0, buffer.length);
                    if (readSize > 0) {
                        synchronized (pcmData) {
                            for (int i = 0; i < readSize; i++) {
                                pcmData.add(buffer[i]);
                            }
                        }
                        if (pcmData.size() >= readSize) {
                            recordChunk();
                        }
                    }
                }
            }
        }).start();
    }

    private void recordChunk() {
        synchronized (pcmData) {
            if (pcmData.isEmpty()) return;
            final List<Short> chunkData = new ArrayList<>(pcmData);
            pcmData.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ByteBuffer buffer = ByteBuffer.allocate(chunkData.size() * 2).order(ByteOrder.LITTLE_ENDIAN);
                    for (Short value : chunkData) {
                        buffer.putShort(value);
                    }
                    byte[] byteArray = buffer.array();
                    String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT | Base64.NO_WRAP);
                    Log.d("Audio", "Send Audio Chunk");
                    sendMediaChunk(base64, "audio/pcm");
                }
            }).start();
        }
    }

    private void stopAudioInput() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        Log.d("Audio", "Stop Recording");
        isSpeaking = false;
        updateStatusIndicator();
    }

    private void displayMessage(final String message) {
        Log.d("Chat", "Displaying message: " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentText = chatLog.getText().toString();
                String newMessage = currentText + "\n" + message;
                chatLog.setText(newMessage);
            }
        });
    }

    private void updateStatusIndicator() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    statusIndicator.setImageResource(R.drawable.baseline_error_24);
                    statusIndicator.setColorFilter(Color.RED);
                } else if (!isSpeaking) {
                    statusIndicator.setImageResource(R.drawable.baseline_equalizer_24);
                    statusIndicator.setColorFilter(Color.GRAY);
                } else {
                    statusIndicator.setImageResource(R.drawable.baseline_equalizer_24);
                    statusIndicator.setColorFilter(Color.GREEN);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraThread != null) {
            cameraThread.quitSafely();
        }
        closeCamera();
    }
}
