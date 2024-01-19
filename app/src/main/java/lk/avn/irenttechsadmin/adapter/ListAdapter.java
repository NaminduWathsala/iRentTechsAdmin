package lk.avn.irenttechsadmin.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import lk.avn.irenttechsadmin.AdminHomeActivity;
import lk.avn.irenttechsadmin.R;
import lk.avn.irenttechsadmin.model.Products;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private static final String TAG = ListAdapter.class.getName();
    private List<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private OnItemClickListener listener;



    public ListAdapter(List<Products> products, Context context,OnItemClickListener listener) {
        this.products = products;
        this.storage = FirebaseStorage.getInstance();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.product_list_item_view, parent, false);
        return new ViewHolder(view, listener);

    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
        Products product = products.get(position);
        holder.textName.setText(product.getName());
        holder.textBrand.setText(product.getBrand());
        holder.textQty.setText(product.getQty());
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

        TextView textName, textPrice,textBrand,textQty;
        ImageView image;

        public ViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            textName = itemView.findViewById(R.id.Product_view_name);
            textBrand = itemView.findViewById(R.id.Product_view_brand);
            textQty = itemView.findViewById(R.id.Product_view_qty);
            textPrice = itemView.findViewById(R.id.Product_view_price);
            image = itemView.findViewById(R.id.Product_view_image);

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
