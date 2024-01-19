package lk.avn.irenttechsadmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import lk.avn.irenttechsadmin.adapter.ListAdapter;
import lk.avn.irenttechsadmin.model.Products;

public class ListProductFragment extends Fragment {

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    ListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        products = new ArrayList<>();

        RecyclerView productView = fragment.findViewById(R.id.productView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        listAdapter = new ListAdapter(products, getContext(),new ListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String selectedProductId = listAdapter.getDocumentId(position);

                Bundle bundle = new Bundle();
                bundle.putString("product_id", selectedProductId);

                SingleProductViewFragment singleProductViewFragment = new SingleProductViewFragment();
                singleProductViewFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frame_layouts, singleProductViewFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        productView.setLayoutManager(linearLayoutManager);
        productView.setAdapter(listAdapter);

        firestore.collection("Products").addSnapshotListener(new EventListener<QuerySnapshot>() {
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

                listAdapter.notifyDataSetChanged();
            }
        });

    }
}