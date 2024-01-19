package lk.avn.irenttechsadmin.adapter;

import android.content.Context;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import lk.avn.irenttechsadmin.R;
import lk.avn.irenttechsadmin.model.UserData;

public class UserManageBlockedAdapter extends RecyclerView.Adapter<UserManageBlockedAdapter.ViewHolder> {
    private static final String TAG = UserManageBlockedAdapter.class.getName();
    private List<UserData> userData;
    private FirebaseStorage storage;
    private static Context context;
    private OnItemClickListener listener;


    public UserManageBlockedAdapter(ArrayList<UserData> products, Context context, OnItemClickListener listener) {
        this.userData = products;
        this.storage = FirebaseStorage.getInstance();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserManageBlockedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.manage_user_view, parent, false);
        return new UserManageBlockedAdapter.ViewHolder(view, listener);

    }

    @Override
    public void onBindViewHolder(@NonNull UserManageBlockedAdapter.ViewHolder holder, int position) {
        UserData user_Data = userData.get(position);
        holder.user_email.setText(user_Data.getEmail());
        holder.user_name.setText(user_Data.getName());

        try {

            StorageReference imageRef = storage.getReference("user_images/" + user_Data.getEmail());

            imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get()
                                    .load(uri)
                                    .resize(200, 200)
                                    .centerCrop()
                                    .into(holder.image);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Error Image");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "No Image");
                }
            });


        } catch (Exception e) {
            Log.i(TAG, e.getMessage());

        }

        String status_value = user_Data.getActive();

        if (status_value != null) {
            if (status_value.equals("2")) {
                holder.user_status_btn.setText("Blocked");
                holder.user_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));
            } else if (status_value.equals("1")) {
                holder.user_status_btn.setText("Active");
                holder.user_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green));
            }
        }

    }

    @Override
    public int getItemCount() {
        return userData.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onStatusChangeClick(int position, Button product_status_btn);
    }

    public String getDocumentId(int position) {
        return userData.get(position).getEmail();
    }

    public void removeItem(int position) {
        userData.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView user_email, user_name;
        ImageView image;
        Button user_status_btn;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            user_email = itemView.findViewById(R.id.user_email);
            user_name = itemView.findViewById(R.id.user_name);
            image = itemView.findViewById(R.id.profile_img);
            user_status_btn = itemView.findViewById(R.id.user_status_btn);

            user_status_btn.setText("Blocked");
            user_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red));

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

            user_status_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onStatusChangeClick(position, user_status_btn);
                        }
                    }
                }
            });
        }
    }
}