package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class QrCodeScannerActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private static final String TAG = "QrCodeScannerActivity";
    PreviewView previewView;
    TextView textViewResult;
    TextView scanComplete;
    LinearLayout bottomPart;
    Button joinFlashletButton;
    String scannedFlashletId;
    FirebaseFirestore db;
    FirebaseAuth auth;
    SQLiteManager localDB;
    UsageStatistic usage;
    UserWithRecents userWithRecents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.qr_code_scanner);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.oACameraPreviewView);
        textViewResult = findViewById(R.id.textViewResult);
        scanComplete = findViewById(R.id.scancomplete);
        bottomPart = findViewById(R.id.bottomPart);
        joinFlashletButton = findViewById(R.id.joinFlashletButton);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        textViewResult.setText("Scanning...");
        joinFlashletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinFlashlet();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.qcsViewToolbar);
        toolbar.setNavigationOnClickListener(v -> handleBackNavigation());

        // Handle Back Button Click
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void handleBackNavigation() {
        // Call the default back press behavior again to return to the previous screen
        previewView.setEnabled(false);
        QrCodeScannerActivity.this.getOnBackPressedDispatcher().onBackPressed();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera provider error: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> {
            scanBarcode(image);
            image.close();
        });

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void scanBarcode(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        if (planes.length > 0) {
            ByteBuffer buffer = planes[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                    bytes, image.getWidth(), image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new MultiFormatReader();
            try {
                Result result = reader.decode(bitmap);
                String scannedContent = result.getText();
                Log.d(TAG, "Scanned content: " + scannedContent);

                runOnUiThread(() -> {
                    textViewResult.setVisibility(View.GONE);
                    scanComplete.setVisibility(View.VISIBLE);
                    bottomPart.setVisibility(View.VISIBLE);
                    scanComplete.setText("Scanning complete");

                    // Extract flashlet ID from the scanned content
                    scannedFlashletId = extractFlashletId(scannedContent);
                    if (scannedFlashletId != null) {
                        joinFlashletButton.setEnabled(true);
                        Log.d(TAG, "Extracted Flashlet ID: " + scannedFlashletId);
                    } else {
                        Toast.makeText(QrCodeScannerActivity.this, "Invalid QR code scanned.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error decoding barcode: ", e);
            }
        } else {
            Log.e(TAG, "No planes available in image");
        }
    }

    private String extractFlashletId(String scannedContent) {
        // Ensure the scanned content matches the expected format
        if (scannedContent != null && scannedContent.startsWith("quizzzy://flashlet/?id=")) {
            return scannedContent.substring("quizzzy://flashlet/?id=".length());
        }
        return null;
    }

    private void joinFlashlet() {
        if (scannedFlashletId != null) {
            String userId = auth.getCurrentUser().getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> createdFlashlets = (List<String>) documentSnapshot.get("createdFlashlets");

                    if (createdFlashlets != null && createdFlashlets.contains(scannedFlashletId)) {
                        Toast.makeText(this, "You have already joined this flashlet.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with joining the flashlet
                        DocumentReference flashletRef = db.collection("flashlets").document(scannedFlashletId);

                        WriteBatch batch = db.batch();
                        batch.update(userRef, "createdFlashlets", FieldValue.arrayUnion(scannedFlashletId));
                        batch.update(flashletRef, "creatorID", FieldValue.arrayUnion(userId));

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Flashlet and user updated successfully");
                                    Toast.makeText(this, "Flashlet joined successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, FlashletDetail.class);
                                    intent.putExtra("FLASHLET_ID", scannedFlashletId);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error joining flashlet: ", e);
                                    Toast.makeText(this, "Error opening flashlet. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(this, "User data not found. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching user data: ", e);
                Toast.makeText(this, "Error fetching user data. Please try again.", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No Flashlet ID found. Please scan a valid QR code.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release any resources used by the camera if necessary
    }
}
