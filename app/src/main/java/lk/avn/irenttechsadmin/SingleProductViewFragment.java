package lk.avn.irenttechsadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Date;


import lk.avn.irenttechsadmin.custom.CustomLoading;
import lk.avn.irenttechsadmin.model.Products;

public class SingleProductViewFragment extends Fragment {
    private static final String TAG = SingleProductViewFragment.class.getName();
    Bundle args;
    private static String errorName, successName, successtitle, availableQTY;
    private static FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private String productValue, s_value;
    private TextView spv_name, spv_price, spv_description, spv_qty;
    private ImageButton wishlist;
    ImageSlider imageSlider;
    private static CustomLoading customLoading;
    ArrayList<SlideModel> slideModels;
    Button product_update, product_status;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_product_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        customLoading = new CustomLoading(getActivity());

        args = getArguments();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        spv_name = fragment.findViewById(R.id.spv_name);
        spv_price = fragment.findViewById(R.id.spv_price);
        spv_description = fragment.findViewById(R.id.spv_description);
        spv_qty = fragment.findViewById(R.id.spv_qty);

        product_update = fragment.findViewById(R.id.product_update);
        product_status = fragment.findViewById(R.id.product_status);

        imageSlider = fragment.findViewById(R.id.product_image_slider);
        slideModels = new ArrayList<>();


        if (getArguments() != null) {
            productValue = getArguments().getString("product_id");
        }

        CollectionReference products_list = firestore.collection("Products");

        Query query = products_list.whereEqualTo("documentId", productValue);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (QueryDocumentSnapshot doc : value) {

                    spv_name.setText(doc.getString("brand") + " " + doc.getString("name"));
                    spv_price.setText("Rs." + doc.getString("price") + ".00");
                    spv_description.setText(doc.getString("description"));
                    spv_qty.setText("Available Items: " + doc.getString("qty"));
                    addImages(doc.getString("product_image"));
                    availableQTY = doc.getString("qty");
                }
            }
        });

        firestore.collection("Products").document(productValue).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot value) {
                if (value.exists()) {
                    String status_value = value.getString("status");

                    if (status_value != null) {
                        if (status_value.equals("2")) {
                            product_status.setText("Blocked");
                            product_status.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.red));
                        } else if (status_value.equals("1")) {
                            product_status.setText("Active");
                            product_status.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green));
                        }
                    }
                }
            }
        });


        product_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("product_id", productValue);

                UpdateProductsFragment updateProductsFragment = new UpdateProductsFragment();
                updateProductsFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frame_layouts, updateProductsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        product_status.setOnClickListener(new View.OnClickListener() {
            private boolean isUpdatingStatus = false;

            @Override
            public void onClick(View v) {
                if (isUpdatingStatus) {
                    return;
                }

                isUpdatingStatus = true;

                firestore.collection("Products").document(productValue).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot value) {
                        if (value.exists()) {
                            String status_value = value.getString("status");

                            if (status_value != null) {
                                if (status_value.equals("1")) {
                                    s_value = "2";
                                    product_status.setText("Blocked");
                                    product_status.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.red));
                                } else if (status_value.equals("2")) {
                                    s_value = "1";
                                    product_status.setText("Active");
                                    product_status.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green));
                                }

                                if (s_value != null) {
                                    firestore.collection("Products").document(productValue).update("status", s_value)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    isUpdatingStatus = false;
                                                    if (!task.isSuccessful()) {
                                                        Log.e(TAG, "Error updating product status", task.getException());
                                                        Toast.makeText(getContext(), "Error updating product status", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.e(TAG, "Product status is null");
                                Toast.makeText(getContext(), "Product status not available", Toast.LENGTH_SHORT).show();
                                isUpdatingStatus = false;
                            }
                        } else {
                            Log.e(TAG, "Product document does not exist or value is null");
                            Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
                            isUpdatingStatus = false;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting product document", e);
                        Toast.makeText(getContext(), "Error getting product document", Toast.LENGTH_SHORT).show();
                        isUpdatingStatus = false;
                    }
                });
            }
        });


    }


    public void addImages(String imageId) {
        StorageReference imageRef = storage.getReference("/Product_images/" + imageId + "/");

        imageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                if (listResult != null && listResult.getItems() != null) {
                    // Clear the existing list of slideModels
                    slideModels.clear();

                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    if (isAdded()) {
                                        slideModels.add(new SlideModel(uri.toString(), ScaleTypes.CENTER_INSIDE));
                                        if (isAdded()) {
                                            requireActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (imageSlider != null) {
                                                        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_INSIDE);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Image URL is null");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting image download URL", e);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "No images found in the storage reference");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error listing images", e);
            }
        });
    }


    public static class cardQTYDialog extends DialogFragment {
        private String email;
        private String productId;
        private String availableQTY;
        FragmentManager fragmentManager;


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
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog);
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

    }
}
