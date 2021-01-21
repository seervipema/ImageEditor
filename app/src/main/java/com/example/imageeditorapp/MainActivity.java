package com.example.imageeditorapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ImageView selectedImage,cropImage,rotateImage,rotateAroundYImage;
    private Uri filePath;
    private Bitmap bitmap;
    private final int PICK_IMAGE_REQUEST = 71;
    private TextView fileName,fileSize,fileMimeType;
    private LinearLayout imageDetailsLinearLayout;
    //keep track of cropping intent
    final int PIC_CROP = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedImage= findViewById(R.id.select_image);
        cropImage=findViewById(R.id.crop_image);
        rotateImage=findViewById(R.id.rotate_image);
        rotateAroundYImage= findViewById(R.id.rotate_around_y_image);
        fileName=findViewById(R.id.file_name);
        fileSize=findViewById(R.id.file_size);
        fileMimeType=findViewById(R.id.file_mime_type);
        imageDetailsLinearLayout= findViewById(R.id.image_details_linear_layout);
        rotateAroundYImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedImage.animate().rotationYBy(180f).start();
            }
        });
        rotateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedImage.animate().rotationXBy(180f).start();
            }
        });
        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filePath==null){
                    Toast.makeText(MainActivity.this,"Please select a image first for cropping",Toast.LENGTH_LONG).show();
                }else{
                    performCrop();
                }
            }
        });
        selectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }


    private void selectImage() {
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

        }catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Your device doesn't support capturing images!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode ==RESULT_OK && data!=null && data.getData()!=null){
            filePath =data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),filePath);

                imageDetailsLinearLayout.setVisibility(View.VISIBLE);
                selectedImage.setImageBitmap(bitmap);
                Cursor returnCursor =
                        getContentResolver().query(filePath, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String mimeType = getContentResolver().getType(filePath);
                fileName.setText(returnCursor.getString(nameIndex));
                fileSize.setText(Long.toString(returnCursor.getLong(sizeIndex)));
                fileMimeType.setText(mimeType);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else if(requestCode==PIC_CROP){
            //get the returned data
            Bundle extras = data.getExtras();
            //get the cropped bitmap
            Bitmap thePic = extras.getParcelable("data");
            //display the returned cropped image
            selectedImage.setImageBitmap(thePic);

        }
    }
    private void performCrop(){
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(filePath, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
