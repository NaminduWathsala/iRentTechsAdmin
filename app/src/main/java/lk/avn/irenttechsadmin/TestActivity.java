package lk.avn.irenttechsadmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

import lk.avn.irenttechsadmin.custom.CustomLoading;

public class TestActivity extends AppCompatActivity {

    private static final int PICK_IMG = 1;
    private ArrayList<Uri> ImageList = new ArrayList<>();
    private int uploads = 0;
    TextView textView;
    Button choose, send;
    CustomLoading customLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        customLoading = new CustomLoading(this);

        textView = findViewById(R.id.imagetext);
        choose = findViewById(R.id.choose);
        send = findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload(v);
            }
        });

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choose(v);
            }
        });
    }

    public void choose(View view) {
        ImageList.clear();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMG);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMG) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            ImageList.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        ImageList.add(imageUri);
                    }

                    textView.setVisibility(View.VISIBLE);
                    textView.setText("You Have Selected " + ImageList.size() + " Pictures");
                }
            }
        }
    }



    @SuppressLint("SetTextI18n")
    public void upload(View view) {
        if (ImageList.isEmpty()) {
            textView.setText("Please select images before uploading.");
            return;
        }

        textView.setText("Please Wait ... If Uploading takes Too much time please press the button again ");
        customLoading.show();
        final StorageReference imageFolder = FirebaseStorage.getInstance().getReference().child("Products");
        String uniqueID = UUID.randomUUID().toString();
        int image_name = 1;
        for (uploads = 0; uploads < ImageList.size(); uploads++) {
            Uri image = ImageList.get(uploads);

            final StorageReference imageName = imageFolder.child(uniqueID + "/" + image_name);
            image_name++;
            imageName.putFile(image)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    SendLink(url);
                                }
                            });
                        }
                    });
        }
    }

    private void SendLink(String url) {
        customLoading.dismiss();
        textView.setText("Image Uploaded Successfully");
//        send.setVisibility(View.GONE);
        ImageList.clear();
    }
}