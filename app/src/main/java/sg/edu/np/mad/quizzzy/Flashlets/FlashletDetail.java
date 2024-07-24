package sg.edu.np.mad.quizzzy.Flashlets;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.MainActivity;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.PushNotificationService;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.SwipeGestureDetector;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.QrCodeScannerActivity;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class FlashletDetail extends AppCompatActivity {
    Gson gson = new Gson();

    // Data Variables
    Flashlet flashlet;

    // View Variables
    TextView flashletTitleLbl;
    TextView flashletFlashcardCountLbl;
    Button studyFlashcardBtn;
    LinearLayout flashcardViewList;
    ViewFlipper flashcardPreview;
    GestureDetector gestureDetector;
    TextView optionbtn;
    TextView flashletNameTextView;
    ImageView dialogQrCodeImageView;
    UserWithRecents userWithRecents;
    SQLiteManager localDB;
    UsageStatistic usage;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashlet_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get User from SQLite DB
        localDB = SQLiteManager.instanceOfDatabase(FlashletDetail.this);
        userWithRecents = localDB.getUser();

        // Create new UsageStatistic class and start the update loop
        usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 1, userWithRecents.getUser().getId());

        // Get Flashlet from Intent
        Intent receiveIntent = getIntent();
        flashlet = gson.fromJson(receiveIntent.getStringExtra("flashletJSON"), Flashlet.class);
        ArrayList<Flashcard> flashcards = flashlet.getFlashcards();

        // Update SQLite with Recently Opened
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashletDetail.this);
        ArrayList<String> recentlyViewed = localDB.getUser().getRecentlyOpenedFlashlets();
        String userId = localDB.getUser().getUser().getId();

        // Create new UsageStatistic class and start the update loop
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 1, userId);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.flashlets);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.search) {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    startActivity(new Intent(getApplicationContext(), FlashletList.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fDViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                FlashletDetail.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Handle Back Button Click
        // Enabled is true so that the code within handleOnBackPressed will be executed
        // This also disables the back button press from going to the previous screen
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                // Enable the back button to be able to be used to go to the previous screen
                setEnabled(false);
                // Call the default back press behavior again to return to previous screen
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        // Handle Edit Button Pressed
        ImageView editFlashletBtn = findViewById(R.id.fDEditOption);
        ImageView cloneFlashletBtn = findViewById(R.id.fDCloneOption);

        /// If User ID does not match the Owner of the Flashlet, disable editing
        if (!flashlet.getCreatorID().contains(userId)) {
            editFlashletBtn.setVisibility(View.GONE);
            /// Handle clone onClick
            cloneFlashletBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FlashletDetail.this);
                    builder.setTitle("Clone Flashlet")
                            .setMessage("Do you want to clone this flashlet?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                final String id = UUID.randomUUID().toString();
                                final ArrayList<String> originalCreatorId = flashlet.getCreatorID();
                                Flashlet newFlashlet = flashlet;
                                newFlashlet.setId(id);
                                newFlashlet.setCreatorID(new ArrayList<String>(Arrays.asList(userId)));
                                db.collection("flashlets")
                                        .document(id)
                                        .set(newFlashlet)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashletDetail.this);
                                                ArrayList<String> createdFlashlets = localDB.getUser().getUser().getCreatedFlashlets();
                                                createdFlashlets.add(id);
                                                localDB.updateCreatedFlashcards(localDB.getUser().getUser().getId(), createdFlashlets);

                                                // Save Flashlet ID to User's Firebase
                                                db.collection("users").document(userId).update("createdFlashlets", createdFlashlets)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(FlashletDetail.this, "Flashlet Created!", Toast.LENGTH_LONG).show();
                                                                // Send Message via Firebase FCM notifying the each Owner of the flashlet their flashlet was cloned
                                                                PushNotificationService pushNotificationService = new PushNotificationService();
                                                                for (String creatorId : originalCreatorId) {
                                                                    pushNotificationService.sendFlashletCloneMessage(creatorId, flashlet.getTitle());
                                                                }

                                                                // Send User to their cloned flashlet
                                                                Intent flashletCloneIntent = new Intent(getApplicationContext(), FlashletDetail.class);
                                                                flashletCloneIntent.putExtra("flashletJSON", gson.toJson(newFlashlet));
                                                                flashletCloneIntent.putExtra("userId", userId);

                                                                startActivity(flashletCloneIntent);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getApplicationContext(), "Failed to Clone Flashlet", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Failed to Clone Flashlet", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            })
                            .setNegativeButton("Cancel", ((dialog, which) -> {}))
                            .setCancelable(false);
                    builder.create().show(); // Show Alert
                }
            });
        } else {
            cloneFlashletBtn.setVisibility(View.GONE);
            /// Handle edit onClick
            editFlashletBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendToEdit = new Intent(FlashletDetail.this, UpdateFlashlet.class);
                    sendToEdit.putExtra("flashletJSON", gson.toJson(flashlet));

                    // Save statistics to SQLite DB before changing Activity.
                    // timeType of 1 because this is a Flashlet Activity
                    localDB.updateStatistics(usage, 1, userId);
                    // Kills updateStatisticsLoop as we are switching to another activity.
                    usage.setActivityChanged(true);

                    startActivity(sendToEdit);
                }
            });
        }

        // Find View Components
        flashletTitleLbl = findViewById(R.id.fDFlashletTitle);
        flashletFlashcardCountLbl = findViewById(R.id.fDCounterLabel);
        studyFlashcardBtn = findViewById(R.id.fDStudyFlashcards);
        flashcardViewList = findViewById(R.id.fDFlashcardsContainer);
        flashcardPreview = findViewById(R.id.fDFlashcardPreview);

        // Configure Study Flashcards Button
        Button studyFlashcards = findViewById(R.id.fDStudyFlashcards);
        studyFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendToStudyFlashcards = new Intent(FlashletDetail.this, FlashcardList.class);
                sendToStudyFlashcards.putExtra("flashletJson", gson.toJson(flashlet));

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                startActivity(sendToStudyFlashcards);
            }
        });

        if (recentlyViewed.size() == 5) {
            recentlyViewed.remove(4);
        }
        if (!recentlyViewed.contains(flashlet.getId())) {
            recentlyViewed.add(flashlet.getId());
            localDB.updateRecentlyViewed(userId, recentlyViewed);
        }

        // Update UI based on Flashlet Info
        flashletTitleLbl.setText(flashlet.getTitle());
        String flashcardCount = flashcards.size() + " Total Flashcard" + (flashcards.size() == 1 ? "" : "s");
        flashletFlashcardCountLbl.setText(flashcardCount);

        //Set gesture detector
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector() {
            @Override
            public boolean onSwipeRight() {
                flashcardPreview.setInAnimation(FlashletDetail.this, R.anim.slide_in_left);
                flashcardPreview.setOutAnimation(FlashletDetail.this, R.anim.slide_out_right);
                flashcardPreview.showPrevious();
                return true;
            }

            @Override
            public boolean onSwipeLeft() {
                flashcardPreview.setInAnimation(FlashletDetail.this, R.anim.slide_in_right);
                flashcardPreview.setOutAnimation(FlashletDetail.this, R.anim.slide_out_left);
                flashcardPreview.showNext();
                return true;
            }
        });

        //Set flashcard preview
        for (int i = 0; i < flashcards.size() && i < 8; i++) {
            View flashcardView = LayoutInflater.from(this).inflate(R.layout.flashcard_view_item, flashcardPreview, false);
            TextView keyword = flashcardView.findViewById(R.id.flashcardKeyword);
            TextView definition = flashcardView.findViewById(R.id.flashcardDefinition);
            keyword.setText(flashcards.get(i).getKeyword());
            definition.setText(flashcards.get(i).getDefinition());

            //Add flashcard to ViewFlipper
            flashcardPreview.addView(flashcardView);
        }

        // Add Flashlets to Screen
        for (int i = 0; i < flashcards.size(); i++) {
            // Create Flashcard
            View flashcardView = LayoutInflater.from(FlashletDetail.this).inflate(R.layout.flashcard_list_item, null, false);
            TextView flashcardKeyword = flashcardView.findViewById(R.id.fCLIKeyword);
            TextView flashcardDefinition = flashcardView.findViewById(R.id.fCLIDefinition);

            flashcardKeyword.setText(flashcards.get(i).getKeyword());
            flashcardDefinition.setText(flashcards.get(i).getDefinition());

            /// Add Inflated Flashcard to Containner
            flashcardViewList.addView(flashcardView);

            // Add Spacer View
            View spacerView = new View(FlashletDetail.this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    20
            );
            flashcardViewList.addView(spacerView, spacerParams);
        }

        //On touch listener for gesture
        flashcardPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        /// If User is somehow null, return user back to login page
        if (userWithRecents == null) {
            Intent returnToLoginIntent = new Intent(FlashletDetail.this, MainActivity.class);

            // Save statistics to SQLite DB before changing Activity.
            // timeType of 1 because this is a Flashlet Activity
            localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
            // Kills updateStatisticsLoop as we are switching to another activity.
            usage.setActivityChanged(true);

            startActivity(returnToLoginIntent);
        }


        // sharing qr code and download flashlet
        optionbtn = findViewById(R.id.fDOptionbtn);

        optionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(FlashletDetail.this, v);
                popupMenu.inflate(R.menu.flashlet_detail_options);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();

                        // Save statistics to SQLite DB before changing Activity.
                        // timeType of 1 because this is a Flashlet Activity
                        localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
                        // Kills updateStatisticsLoop as we are switching to another activity.
                        usage.setActivityChanged(true);

                        if (itemId == R.id.fDOShare) {
                            String flashletId = flashlet.getId();
                            if (flashletId != null && !flashletId.isEmpty()) {
                                showDialog(flashletId);
                            } else {
                                Toast.makeText(FlashletDetail.this, "Flashlet ID is invalid.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (itemId == R.id.fDODownload) {
                            showDownloadPdfDialog();
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void showDialog(String id){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_qr_code);
        dialog.setCancelable(false);

        dialogQrCodeImageView = dialog.findViewById(R.id.dialogQrCodeImageView);
        flashletNameTextView = dialog.findViewById(R.id.flashletNameTextView);
        flashletNameTextView.setText(flashlet.getTitle());
        generateQrCode(id, dialogQrCodeImageView);

        dialog.show();

        ImageView dialogCloseButton = dialog.findViewById(R.id.dialogCloseButton);
        dialogCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    private void generateQrCode(String flashletId, ImageView imageView) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            // Create a URL with a custom scheme for the QR code
            String qrContent = "quizzzy://flashlet/?id=" + flashletId;
            Bitmap bitmap = toBitmap(writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512));
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    private Bitmap toBitmap(com.google.zxing.common.BitMatrix matrix){
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    private void showDownloadPdfDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download PDF")
                .setMessage("Do you want to download a PDF version of the flashlet?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createPdf();
                    }
                })
                .setNegativeButton("No", null);
        builder.show();
    }

    private void createPdf() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);

        int x = pageInfo.getPageWidth() / 2;
        int y = 60;

        // Flashlet Title
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(flashlet.getTitle().toUpperCase(), x, y, paint);

        // Flashcard Count
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        y += 40;  // Increase space after the title
        canvas.drawText(flashlet.getFlashcards().size() + " Total Flashcards", x, y, paint);

        // Flashcards
        y += 60;  // Increase space before the flashcards
        paint.setTextAlign(Paint.Align.LEFT);
        for (Flashcard flashcard : flashlet.getFlashcards()) {
            paint.setTextSize(24);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Keyword:", 40, y, paint);
            paint.setTextSize(20);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(flashcard.getKeyword(), 150, y, paint);

            y += 40;  // Increase space between keyword and description
            paint.setTextSize(24);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Description:", 40, y, paint);
            paint.setTextSize(20);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(flashcard.getDefinition(), 180, y, paint);

            y += 70;  // Increase space between flashcards
        }

        // Date and Time
        String dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(dateTime, x, pageInfo.getPageHeight() - 40, paint);

        document.finishPage(page);

        // Write the document content to a file in the Downloads directory
        String fileName = flashlet.getTitle().toUpperCase() + ".pdf";
        OutputStream outputStream = null;
        Uri pdfUri = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            pdfUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

            if (pdfUri != null) {
                try {
                    outputStream = getContentResolver().openOutputStream(pdfUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            try {
                outputStream = new FileOutputStream(filePath);
                pdfUri = Uri.fromFile(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (outputStream != null) {
            try {
                document.writeTo(outputStream);
                Toast.makeText(this, "PDF downloaded: " + fileName, Toast.LENGTH_LONG).show();

                // Open the PDF file
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        document.close();
    }

    // To re-initialize the DB update loop when returning to the screen
    @Override
    protected void onRestart() {
        super.onRestart();
        // Recreate the activity so that the DB update loop will be called again and be able to be terminated
        recreate();
    }
}