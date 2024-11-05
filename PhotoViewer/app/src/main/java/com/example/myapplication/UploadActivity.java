package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private EditText titleEditText, textEditText, authorIdEditText;
    private ImageView imageView;
    private Bitmap selectedImageBitmap;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        titleEditText = findViewById(R.id.titleEditText);
        textEditText = findViewById(R.id.textEditText);
        authorIdEditText = findViewById(R.id.authorIdEditText);
        imageView = findViewById(R.id.imageView);
        uploadButton = findViewById(R.id.uploadButton);

        // 이미지 선택
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        });

        // 업로드 버튼 클릭 시
        uploadButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String text = textEditText.getText().toString();
            String authorId = authorIdEditText.getText().toString();

            if (selectedImageBitmap != null && !title.isEmpty() && !text.isEmpty() && !authorId.isEmpty()) {
                new PutPost().execute(title, text, authorId, selectedImageBitmap);
            } else {
                Toast.makeText(this, "모든 필드를 입력하고 이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                imageView.setImageBitmap(selectedImageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PutPost extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            try {
                String title = (String) params[0];
                String text = (String) params[1];
                String authorId = (String) params[2];
                Bitmap bitmap = (Bitmap) params[3];

                String token = "dc2370468c357d774045c432acd5936362161aa7";
                String apiUrl = "https://meongju0o0.pythonanywhere.com/api_root/Post/";

                // 현재 날짜와 시간을 ISO 8601 형식으로 설정
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                String publishedDate = sdf.format(new Date());

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=boundary");

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("--boundary\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"title\"\r\n\r\n" + title + "\r\n");

                dos.writeBytes("--boundary\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"text\"\r\n\r\n" + text + "\r\n");

                dos.writeBytes("--boundary\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"author\"\r\n\r\n" + authorId + "\r\n");

                // 자동으로 현재 날짜 설정
                dos.writeBytes("--boundary\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"published_date\"\r\n\r\n" + publishedDate + "\r\n");

                dos.writeBytes("--boundary\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"upload.jpg\"\r\n");
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();
                dos.write(imageData);

                dos.writeBytes("\r\n");
                dos.writeBytes("--boundary--\r\n");
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("PutPost", "Post uploaded successfully.");
                } else {
                    Log.d("PutPost", "Post upload failed.");
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(UploadActivity.this, "Image and post uploaded successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}