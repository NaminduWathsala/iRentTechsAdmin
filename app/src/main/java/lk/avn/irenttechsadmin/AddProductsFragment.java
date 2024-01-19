package lk.avn.irenttechsadmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lk.avn.irenttechsadmin.custom.CustomLoading;
import lk.avn.irenttechsadmin.model.Products;

public class AddProductsFragment extends Fragment {

    private static final String TAG = AddProductsFragment.class.getName();
    private static String errorName;
    private static String successName;
    private static String successtitle;
    private FirebaseFirestore fireStore;
    private Spinner category_spinner;
    private EditText product_brand, product_name, product_price, product_qty;
    private TextInputEditText product_description;
    ListenerRegistration listenerRegistration;
    CustomLoading customLoading;
    private String randomId;
    private static final int PICK_IMG = 1;
    private ArrayList<Uri> ImageList = new ArrayList<>();
    private int uploads = 0;
    private TextView imageCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        customLoading = new CustomLoading(getActivity());

        category_spinner = fragment.findViewById(R.id.product_category_input);

        product_brand = fragment.findViewById(R.id.product_brand_input);
        product_name = fragment.findViewById(R.id.product_name_input);
        product_description = fragment.findViewById(R.id.product_description_input);
        product_qty = fragment.findViewById(R.id.product_qty_input);
        product_price = fragment.findViewById(R.id.product_price_input);

        imageCount = fragment.findViewById(R.id.image_count);


        fireStore = FirebaseFirestore.getInstance();

        load_category_spinner();

        fragment.findViewById(R.id.add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageList.clear();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
            }
        });


        fragment.findViewById(R.id.product_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedItem = category_spinner.getSelectedItem().toString();

                String brand = product_brand.getText().toString();
                String name = product_name.getText().toString();
                String description = product_description.getText().toString();
                String qty = product_qty.getText().toString();
                String price = product_price.getText().toString();


                if (selectedItem.equals("Select the Category")) {
                    errorName = "Please Select the product Category";
                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                } else if (brand == null || brand.trim().isEmpty()) {
                    errorName = "Please Enter the product Brand Name";
                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                } else if (name == null || name.trim().isEmpty()) {
                    errorName = "Please Enter the product Name";
                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                } else if (description == null || description.trim().isEmpty()) {
                    errorName = "Please Enter the product Description";
                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                } else if (qty.equals("0") || qty == null || qty.trim().isEmpty()) {
                    errorName = "Please Enter the product Quantity";
                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                } else if (price.equals("0") || price == null || price.trim().isEmpty()) {
                    errorName = "Please Enter the product Price";
                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                } else if (ImageList.isEmpty()) {
                    imageCount.setText("Please select images before uploading.");
                    errorName = "Please select images before uploading.";
                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");

                } else {
                    randomId = UUID.randomUUID().toString();
                    Date date = new Date();


                    Products products = new Products(selectedItem, brand, name, description, qty, price, randomId,date.toString(),"1");


                    customLoading.show();


                    fireStore.collection("Products").add(products).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                String generatedDocumentId = task.getResult().getId();
                                Log.d(TAG, "Generated Document ID: " + generatedDocumentId);

                                DocumentReference productDocument = fireStore.collection("Products").document(generatedDocumentId);
                                productDocument.update("documentId", generatedDocumentId);

                                upload(v, randomId);
                            } else {
                                customLoading.dismiss();
                                errorName = "Your Product has been Not Added Successfully";
                                new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            customLoading.dismiss();
                            errorName = "Your Product has been Not Added Successfully";
                            new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                        }
                    });


                }
            }
        });


    }


    public static class ErrorDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.error_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(errorName);

            Button okButton = customView.findViewById(R.id.success_dialog_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }

    public static class SuccessDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.success_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(successName);

            TextView messageTextView2 = customView.findViewById(R.id.success_dialog_title);
            messageTextView2.setText(successtitle);

            Button okButton = customView.findViewById(R.id.success_dialog_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }

    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            ImageList.add(imageUri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        ImageList.add(imageUri);
                    }

                    imageCount.setText("You Have Selected " + ImageList.size() + " Pictures");
                }
            }
        }
    });

    @SuppressLint("SetTextI18n")
    public void upload(View view, String uniqueID) {
        if (ImageList.isEmpty()) {
            imageCount.setText("Please select images before uploading.");
            return;
        }

        imageCount.setText("Please Wait ... If Uploading takes Too much time please press the button again ");
        customLoading.show();
        final StorageReference imageFolder = FirebaseStorage.getInstance().getReference().child("Product_images");
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
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    customLoading.dismiss();
                                    errorName = "Your Images have been Not Added Successfully";
                                    new AddProductsFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                                }
                            });
                        }
                    });
        }
    }

    private void SendLink(String url) {
        customLoading.dismiss();
        successtitle = "Successful!";
        successName = "Your Product has been Added Successfully";
        new AddProductsFragment.SuccessDialog().show(getActivity().getSupportFragmentManager(), "Success");
        imageCount.setText("Image Uploaded Successfully");
        clearFealds();
        ImageList.clear();
    }

    private void clearFealds() {
        product_brand.setText("");
        product_name.setText("");
        product_description.setText("");
        product_qty.setText("");
        product_price.setText("");
        imageCount.setText("0 Images Selected");
        category_spinner.setSelection(0);

    }

    private void load_category_spinner() {
        if (listenerRegistration == null) {
            listenerRegistration = fireStore.collection("Product_Category").document("Category")
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (value.exists()) {
                                List<String> categoryNames = (List<String>) value.get("name");

                                if (categoryNames != null) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                            getActivity(),
                                            android.R.layout.simple_spinner_item,
                                            categoryNames
                                    );
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    category_spinner.setAdapter(adapter);
                                }
                            }
                        }
                    });
        }
    }

}
