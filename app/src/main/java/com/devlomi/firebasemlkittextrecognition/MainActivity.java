package com.devlomi.firebasemlkittextrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private static final int PICK_IMG_REQUEST = 1458;
    Bitmap pickedBitmap, mBitmap;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button button = findViewById(R.id.btn);
        imageView = findViewById(R.id.img);
        textView = findViewById(R.id.textView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromGallery();
            }
        });

    }

    private void pickImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMG_REQUEST);
    }


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (reqCode == PICK_IMG_REQUEST && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();


            try {
                pickedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mBitmap = pickedBitmap.copy(Bitmap.Config.ARGB_8888, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final Canvas canvas = new Canvas(mBitmap);


            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(pickedBitmap);
            FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();


            detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {

//                            setBlockText(firebaseVisionText, canvas);

//                            setLineText(firebaseVisionText, canvas);

                            setElementText(firebaseVisionText, canvas);

                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                }

                            });


        } else {

            Toast.makeText(this, "no image selected :/", Toast.LENGTH_SHORT).show();
        }
    }

    private void setElementText(FirebaseVisionText firebaseVisionText, Canvas canvas) {
        String text = "";
        Paint paint = getPaint();
        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                for (int i = 0; i < line.getElements().size(); i++) {
                    FirebaseVisionText.Element element = line.getElements().get(i);
                    text += element.getText();
                    canvas.drawRect(element.getBoundingBox(), paint);

                    if (i == line.getElements().size() - 1) {
                        textView.setText(text);
                        imageView.setImageBitmap(mBitmap);
                    }

                }
            }
        }
    }

    private void setLineText(FirebaseVisionText firebaseVisionText, Canvas canvas) {
        String text = "";
        Paint paint = getPaint();


        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {


            for (int i = 0; i < block.getLines().size(); i++) {
                FirebaseVisionText.Line line = block.getLines().get(i);
                text += line.getText();
                Log.d("3llomi", "liine text " + line.getText());
                canvas.drawRect(line.getBoundingBox(), paint);


                if (i == block.getLines().size() - 1) {
                    textView.setText(text);
                    imageView.setImageBitmap(mBitmap);
                }


            }
        }
    }

    private void setBlockText(FirebaseVisionText firebaseVisionText, Canvas canvas) {
        Paint paint = getPaint();
        String text = "";
        for (int i = 0; i < firebaseVisionText.getBlocks().size(); i++) {

            FirebaseVisionText.Block block = firebaseVisionText.getBlocks().get(i);
            canvas.drawRect(block.getBoundingBox(), paint);
            text += block.getText();


            if (i == firebaseVisionText.getBlocks().size() - 1) {
                textView.setText(text);
                imageView.setImageBitmap(mBitmap);
            }


        }
    }

    @NonNull
    private Paint getPaint() {
        final Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        p.setDither(true);
        p.setColor(Color.RED);
        return p;
    }
}
