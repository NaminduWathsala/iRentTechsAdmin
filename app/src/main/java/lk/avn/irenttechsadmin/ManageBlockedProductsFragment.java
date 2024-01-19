package lk.avn.irenttechsadmin;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import lk.avn.irenttechsadmin.adapter.ProductActiveManagementAdapter;
import lk.avn.irenttechsadmin.adapter.ProductBlockedManagementAdapter;
import lk.avn.irenttechsadmin.model.Products;

public class ManageBlockedProductsFragment extends Fragment {

    private static final String TAG = ManageBlockedProductsFragment.class.getName();
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private String  s_value;

    ProductBlockedManagementAdapter productBlockedManagementAdapter;
    private OnProductSelectedListener listener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_blocked_products, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        products = new ArrayList<>();

        RecyclerView productView = fragment.findViewById(R.id.product_block_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        productBlockedManagementAdapter = new ProductBlockedManagementAdapter(products, getContext(),new ProductBlockedManagementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String selectedProductId = productBlockedManagementAdapter.getDocumentId(position);

                if (listener != null) {
                    listener.onProductSelected(selectedProductId);
                }
            }

            @Override
            public void onStatusChangeClick(int position, Button product_status_btn) {
                String selectedProductId = productBlockedManagementAdapter.getDocumentId(position);


                firestore.collection("Products").document(selectedProductId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot value) {
                        if (value.exists()) {
                            String status_value = value.getString("status");

                            if (status_value != null) {
                                if (status_value.equals("1")) {
                                    s_value = "2";
                                } else if (status_value.equals("2")) {
                                    s_value = "1";
                                }

                                if (s_value != null) {

                                    productBlockedManagementAdapter.notifyItemChanged(position);

                                    firestore.collection("Products").document(selectedProductId).update("status", s_value)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        productBlockedManagementAdapter.removeItem(position);
                                                    }else{
                                                        Log.e(TAG, "Error updating product status", task.getException());

                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.e(TAG, "Product status is null");
                            }
                        } else {
                            Log.e(TAG, "Product document does not exist or value is null");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting product document", e);
                    }
                });


            }
        });

        productView.setLayoutManager(linearLayoutManager);
        productView.setAdapter(productBlockedManagementAdapter);

        CollectionReference productCollectionReference = firestore.collection("Products");
        Query product_query = productCollectionReference.whereEqualTo("status","2");

        product_query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (DocumentChange change : value.getDocumentChanges()) {
                    Products product = change.getDocument().toObject(Products.class);
                    switch (change.getType()) {
                        case ADDED:
                            products.add(product);
                        case MODIFIED:
                            Products old = products.stream().filter(i -> i.getName().equals(product.getName())).findFirst().orElse(null);
                            if (old != null) {
                                old.setName(product.getName());
                                old.setBrand(product.getBrand());
                                old.setPrice(product.getPrice());
                                old.setQty(product.getQty());
                                old.setProduct_image(product.getProduct_image());
                            }
                            break;
                        case REMOVED:
                            products.remove(product);
                    }
                }

                productBlockedManagementAdapter.notifyDataSetChanged();
            }
        });

    }

    public interface OnProductSelectedListener {
        void onProductSelected(String productId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if the hosting activity implements the interface
        if (context instanceof OnProductSelectedListener) {
            listener = (OnProductSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnProductSelectedListener");
        }
    }
}