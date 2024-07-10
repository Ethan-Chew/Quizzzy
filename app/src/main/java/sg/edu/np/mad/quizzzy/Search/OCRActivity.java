package sg.edu.np.mad.quizzzy.Search;

import static android.Manifest.permission_group.CAMERA;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.GeminiHandler;
import sg.edu.np.mad.quizzzy.Models.GeminiHandlerResponse;
import sg.edu.np.mad.quizzzy.Models.GeminiResponseEventHandler;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.R;

public class OCRActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    Gson gson = new Gson();

    PreviewView cameraView;
    SurfaceHolder holder;
    SurfaceView surfaceView;
    TextView decodedText;
    Canvas canvas;
    Paint paint;

    // Bounding Box Values
    float left, top, right, bottom;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ocractivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.oAViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (checkCameraPermission()) {
            startCameraPreview();
        } else {
            requestCameraPermission();
        }

        // Save Decoded Text Label
        decodedText = findViewById(R.id.oADecodedText);

        // Create Bounding Rect
        surfaceView = findViewById(R.id.oAOverlay);
        setupBoundingRect(surfaceView);

        // Handle Complete Button Click
        Button completeButton = findViewById(R.id.oAComplete);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the Bottom Sheet to Edit OCR Text
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OCRActivity.this);
                View dialogView = LayoutInflater.from(OCRActivity.this).inflate(R.layout.ocr_bottom_sheet_layout, null);
                bottomSheetDialog.setContentView(dialogView);
                bottomSheetDialog.show();

                TextInputEditText editText = dialogView.findViewById(R.id.oBSEditText);
                Button searchBtn = dialogView.findViewById(R.id.oBSSearchBtn);
                Button generateBtn = dialogView.findViewById(R.id.oBSGenerateBtn);

                editText.setText(decodedText.getText().toString());

                // Handle Search Button Click
                searchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("result", editText.getText().toString());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

                // Handle Generate Button Click
                generateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Disable the Button and show loading
                        generateBtn.setText("Loading...");
                        generateBtn.setEnabled(false);
                        searchBtn.setEnabled(false);

                        TextInputEditText editText = dialogView.findViewById(R.id.oBSEditText);
                        GeminiHandler.generateFlashletOnKeyword(editText.getText().toString(), new GeminiResponseEventHandler() {
                            @Override
                            public void onResponse(GeminiHandlerResponse handlerResponse) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Send Intent to CreateFlashlet
                                        Intent sendToCreateFlashlet = new Intent(OCRActivity.this, CreateFlashlet.class);
                                        sendToCreateFlashlet.putExtra("autofilledFlashletJSON", gson.toJson(handlerResponse));
                                        startActivity(sendToCreateFlashlet);

                                        // Reset the Button
                                        generateBtn.setText("Generate Flashlet");
                                        generateBtn.setEnabled(true);
                                        searchBtn.setEnabled(true);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception err) {
                                Toast.makeText(OCRActivity.this, err.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                // Enable the Button
                                generateBtn.setText("Generate Flashlet");
                                generateBtn.setEnabled(true);
                                searchBtn.setEnabled(true);
                            }
                        });
                    }
                });
            }
        });
    }

    // Start CameraX
    void startCameraPreview() {
        cameraView = findViewById(R.id.oACameraPreviewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    OCRActivity.this.bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {

                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        // Setup the AndroidX Camera
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        // Analyse the Image
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            // Handle OCR Image Recognition
            @OptIn(markerClass = ExperimentalGetImage.class) @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                Bitmap bitmap = imageProxy.toBitmap();
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                if (bitmap != null) {
                    // Get Cropping Values
                    cameraView = findViewById(R.id.oACameraPreviewView);
                    int cameraHeight = cameraView.getHeight();
                    int cameraWidth = cameraView.getWidth();

                    // Scale the bitmap to match the cameraView dimensions
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, cameraWidth, cameraHeight, false);
                    int scaledWidth = scaledBitmap.getWidth();
                    int scaledHeight = scaledBitmap.getHeight();

                    float diameter;

                    diameter = scaledWidth;
                    if (scaledHeight < scaledWidth) {
                        diameter = scaledHeight;
                    }

                    int offset = (int) (0.05 * diameter);
                    diameter -= offset;

                    // Calculate the Bounding Box's Boundaries
                    left = (float) (scaledWidth / 2.0 - diameter / 2.5);
                    top = (float) (scaledHeight / 2.0 - diameter / 5.0);
                    right = (float) (scaledWidth / 2.0 + diameter / 2.5);
                    bottom = (float) (scaledHeight / 2.0 + diameter / 5.0);

                    int cropWidth = (int) (right - left);
                    int cropHeight = (int) (bottom - top);
                    Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, (int) left, (int) top, cropWidth, cropHeight);
                    InputImage croppedImage = InputImage.fromBitmap(croppedBitmap, 0);

                    Task<Text> result = recognizer.process(croppedImage)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text text) {
                                    StringBuilder result = new StringBuilder();
                                    for (Text.TextBlock block : text.getTextBlocks()) {
                                        String blockText = block.getText();
                                        Rect blockRect = block.getBoundingBox();
//                                        drawTextBounding(blockRect);
                                        for (Text.Line line : block.getLines()) {
                                            String lineText = line.getText();
                                            for (Text.Element element : line.getElements()) {
                                                String elementText = element.getText();
                                                result.append(elementText);
                                            }
                                            decodedText.setText(blockText);
                                        }
                                    }
                                    imageProxy.close();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(OCRActivity.this, "Failed to detect text from image", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
    }

    // Draw Detection Bounding Box on the Screen
    private void setupBoundingRect(SurfaceView surfaceView) {
        surfaceView.setZOrderOnTop(true);
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(OCRActivity.this);
    }

    private void drawBoundingRect() {
        // Calculate Size of Bounding Box
        cameraView = findViewById(R.id.oACameraPreviewView);
        int height = cameraView.getHeight();
        int width = cameraView.getWidth();

        float diameter;

        diameter = width;
        if (height < width) {
            diameter = height;
        }

        int offset = (int) (0.05 * diameter);
        diameter -= offset;

        canvas = holder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // Customise the Bounding Box's Line Stroke
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#ffffff"));
        paint.setStrokeWidth(5);

        // Calculate the Bounding Box's Boundaries
        left = (float) (width / 2.0 - diameter / 2.5);
        top = (float) (height / 2.0 - diameter / 5.0);
        right = (float) (width / 2.0 + diameter / 2.5);
        bottom = (float) (height / 2.0 + diameter / 5.0);
        // Draw Outlines of each corner on the Bounding Box
        float cornerLength = 50f;
        /// Top-left corner
        canvas.drawLine(left, top, left + cornerLength, top, paint);
        canvas.drawLine(left, top, left, top + cornerLength, paint);

        /// Top-right corner
        canvas.drawLine(right, top, right - cornerLength, top, paint);
        canvas.drawLine(right, top, right, top + cornerLength, paint);

        /// Bottom-left corner
        canvas.drawLine(left, bottom, left + cornerLength, bottom, paint);
        canvas.drawLine(left, bottom, left, bottom - cornerLength, paint);

        /// Bottom-right corner
        canvas.drawLine(right, bottom, right - cornerLength, bottom, paint);
        canvas.drawLine(right, bottom, right, bottom - cornerLength, paint);

        // Add the Outlines to the Holder
        holder.unlockCanvasAndPost(canvas);
    }

    // Draw Detected Text Bounding Box
    private void drawTextBounding(Rect bounds) {
        canvas = holder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // Customise the Bounding Box's Line Stroke
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#aaaaaa"));
        paint.setStrokeWidth(5);

        // Draw Box
        canvas.drawRect(bounds.left, bounds.top, bounds.width(), bounds.height(), paint);
        holder.unlockCanvasAndPost(canvas);
    }

    // Manage Camera Permissions
    private boolean checkCameraPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.CAMERA }, PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermission) {
                startCameraPreview();
            }
        }
    }

    // Surface Holder
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        drawBoundingRect();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}