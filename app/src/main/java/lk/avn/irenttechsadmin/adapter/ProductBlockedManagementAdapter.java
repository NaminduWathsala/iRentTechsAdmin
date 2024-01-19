package lk.avn.irenttechsadmin.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import lk.avn.irenttechsadmin.R;
import lk.avn.irenttechsadmin.model.Products;

public class ProductBlockedManagementAdapter extends RecyclerView.Adapter<ProductBlockedManagementAdapter.ViewHolder> {
    private static final String TAG = ListAdapter.class.getName();
    private List<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private OnItemClickListener listener;



    public ProductBlockedManagementAdapter(List<Products> products, Context context, OnItemClickListener listener) {
        this.products = products;
        this.storage = FirebaseStorage.getInstance();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductBlockedManagementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.manage_product_view, parent, false);
        return new ProductBlockedManagementAdapter.ViewHolder(view, listener);

    }

    @Override
    public void onBindViewHolder(@NonNull ProductBlockedManagementAdapter.ViewHolder holder, int position) {
        Products product = products.get(position);
        holder.textName.setText(product.getBrand()+" "+product.getName());
        holder.textQty.setText("Quantity: "+product.getQty());
        holder.textPrice.setText("Rs. "+product.getPrice()+".00");

        StorageReference productImagesRef = storage.getReference("Product_images/"+product.getProduct_image());

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

        String status_value = product.getStatus();

        if (status_value != null) {
            if (status_value.equals("2")) {
                holder.product_status_btn.setText("Blocked");
                holder.product_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));
            } else if (status_value.equals("1")) {
                holder.product_status_btn.setText("Active");
                holder.product_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green));
            }
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onStatusChangeClick(int position,Button product_status_btn);
    }

    public String getDocumentId(int position) {
        return products.get(position).getDocumentId();
    }

    public void removeItem(int position) {
        products.remove(position);
        notifyItemRemoved(position);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textPrice,textQty;
        ImageView image;
        Button product_status_btn;

        public ViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            textName = itemView.findViewById(R.id.Product_view_name);
            textQty = itemView.findViewById(R.id.Product_view_qty);
            textPrice = itemView.findViewById(R.id.Product_view_price);
            image = itemView.findViewById(R.id.Product_view_image);
            product_status_btn = itemView.findViewById(R.id.product_status_btn);

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

            product_status_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onStatusChangeClick(position,product_status_btn);
                        }
                    }
                }
            });
        }
    }
}