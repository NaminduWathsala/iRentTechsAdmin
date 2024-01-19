package lk.avn.irenttechsadmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.avn.irenttechsadmin.model.Invoice;

public class DashboardFragment extends Fragment {

    private static final String TAG = DashboardFragment.class.getName();
    private TextView userCountTextView;
    private TextView productCountTextView;
    private TextView allInvoicesCountTextView;
    private TextView cancelledInvoicesCountTextView;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private TextView Product_name;
    private TextView Product_price;
    private ImageView best_productImage;

    private Map<String, Integer> productSalesMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        userCountTextView = fragment.findViewById(R.id.userCount);
        productCountTextView = fragment.findViewById(R.id.productCount);
        allInvoicesCountTextView = fragment.findViewById(R.id.AllinvoiceCount);
        cancelledInvoicesCountTextView = fragment.findViewById(R.id.finishedInvoices);
        Product_name = fragment.findViewById(R.id.Product_name);
        Product_price = fragment.findViewById(R.id.Product_price);
        best_productImage = fragment.findViewById(R.id.best_productImage);

        fetchAndDisplayCounts();
        identifyBestSellingProduct();

    }

    private void fetchAndDisplayCounts() {
        getUserCount();
        getProductCount();
        getAllInvoicesCount();
        getCancelledInvoicesCount();
    }

    private void getUserCount() {
        CollectionReference userCollection = firestore.collection("User");
        userCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            long userCount = queryDocumentSnapshots.size();
            userCountTextView.setText(String.valueOf(userCount));
        });
    }

    private void getProductCount() {
        CollectionReference productCollection = firestore.collection("Products");
        productCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            long productCount = queryDocumentSnapshots.size();
            productCountTextView.setText(String.valueOf(productCount));
        });
    }

    private void getAllInvoicesCount() {
        CollectionReference invoiceCollection = firestore.collection("Invoice");
        invoiceCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            long allInvoicesCount = queryDocumentSnapshots.size();
            allInvoicesCountTextView.setText(String.valueOf(allInvoicesCount));
        });
    }

    private void getCancelledInvoicesCount() {
        CollectionReference cancelledInvoiceCollection = firestore.collection("Invoice");
        cancelledInvoiceCollection.whereEqualTo("status", "canceled")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    long cancelledInvoicesCount = queryDocumentSnapshots.size();
                    cancelledInvoicesCountTextView.setText(String.valueOf(cancelledInvoicesCount));
                });
    }

    private void identifyBestSellingProduct() {
        CollectionReference invoicesCollection = firestore.collection("Invoice");
        invoicesCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Invoice invoice = document.toObject(Invoice.class);
                analyzeInvoice(invoice);
            }

            String bestSellingProductId = "";
            int maxQuantitySold = 0;

            for (Map.Entry<String, Integer> entry : productSalesMap.entrySet()) {
                String productId = entry.getKey();
                int quantitySold = entry.getValue();

                if (quantitySold > maxQuantitySold) {
                    maxQuantitySold = quantitySold;
                    bestSellingProductId = productId;
                }
            }


            firestore.collection("Products").document(bestSellingProductId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value.exists()){
                        Product_name.setText("Name : "+value.getString("brand")+" "+value.getString("name"));
                        Product_price.setText("Price : Rs. "+value.getString("price")+".00");

                        StorageReference productImagesRef = storage.getReference("Product_images/" + value.getString("product_image"));

                        productImagesRef.listAll()
                                .addOnSuccessListener(listResult -> {
                                    if (!listResult.getItems().isEmpty()) {
                                        StorageReference firstImageRef = listResult.getItems().get(0);

                                        firstImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            Picasso.get()
                                                    .load(uri)
                                                    .resize(200, 200)
                                                    .centerCrop()
                                                    .into(best_productImage);
                                        });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error listing images", e);
                                });
                    }else{
                        Log.i(TAG, "Document does not exist");
                    }
                }
            });

            Log.d(TAG, bestSellingProductId);
        });
    }

    private void analyzeInvoice(Invoice invoice) {
        List<Invoice.InvoiceItem> products = invoice.getProducts();
        if (products != null) {
            for (Invoice.InvoiceItem item : products) {
                String productId = item.getProduct_id();
                int quantitySold = Integer.parseInt(item.getQty());

                if (productSalesMap.containsKey(productId)) {
                    int currentQuantity = productSalesMap.get(productId);
                    productSalesMap.put(productId, currentQuantity + quantitySold);
                } else {
                    productSalesMap.put(productId, quantitySold);
                }
            }
        }
    }
}
