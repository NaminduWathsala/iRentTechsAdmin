package lk.avn.irenttechsadmin.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import lk.avn.irenttechsadmin.R;
import lk.avn.irenttechsadmin.model.Products;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private static final String TAG = SearchResultsAdapter.class.getName();
    private List<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private OnItemClickListener listener;

    public SearchResultsAdapter(List<Products> products, Context context, OnItemClickListener listener) {
        this.products = products;
        this.storage = FirebaseStorage.getInstance();
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.home_product_list, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Products product = products.get(position);
        holder.textName.setText(product.getBrand()+" "+product.getName());
        holder.textQty.setText("Qty: "+product.getQty());
        holder.textPrice.setText("Rs. " + product.getPrice() + ".00");

        StorageReference productImagesRef = storage.getReference("Product_images/" + product.getProduct_image());

        productImagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (!listResult.getItems().isEmpty()) {
                        StorageReference firstImageRef = listResult.getItems().get(0);

                        firstImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Picasso.get()
                                    .load(uri)
                                    .resize(200, 200)
                                    .centerCrop()
                                    .into(holder.image);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error listing images", e);
                });


    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public String getDocumentId(int position) {
        return products.get(position).getDocumentId();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textPrice, textQty;
        ImageView image;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textName = itemView.findViewById(R.id.Home_Product_view_name);
            textQty = itemView.findViewById(R.id.Home_Product_view_qty);
            textPrice = itemView.findViewById(R.id.Home_Product_view_price);
            image = itemView.findViewById(R.id.Home_Product_view_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
