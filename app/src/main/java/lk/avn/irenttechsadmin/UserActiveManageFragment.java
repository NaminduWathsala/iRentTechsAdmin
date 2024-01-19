package lk.avn.irenttechsadmin;

import android.app.Service;
import android.content.Context;
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
import android.widget.Button;


import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lk.avn.irenttechsadmin.adapter.UserManageActiveAdapter;
import lk.avn.irenttechsadmin.custom.RetrofitClient;
import lk.avn.irenttechsadmin.dto.UserDTO;
import lk.avn.irenttechsadmin.model.Products;
import lk.avn.irenttechsadmin.model.UserData;
import lk.avn.irenttechsadmin.model.UserProfile;
import lk.avn.irenttechsadmin.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActiveManageFragment extends Fragment {

    private static final String TAG = UserActiveManageFragment.class.getName();

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<UserData> userData;
    private ManageActiveProductsFragment.OnProductSelectedListenerActive listener;
    private static String s_value;
    UserManageActiveAdapter userManageActiveAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_active_manage, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        userData = new ArrayList<>();

        RecyclerView productView = fragment.findViewById(R.id.user_active_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        userManageActiveAdapter = new UserManageActiveAdapter(userData, getContext(), new UserManageActiveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String selectedProductId = userManageActiveAdapter.getDocumentId(position);

                if (listener != null) {
//                    listener.onProductSelectedActive(selectedProductId);
                }
            }

            @Override
            public void onStatusChangeClick(int position, Button product_status_btn) {
                String selectedProductId = userManageActiveAdapter.getDocumentId(position);

                ApiService apiService = RetrofitClient.getApiService();

                UserDTO userDTO = new UserDTO();
                userDTO.setEmail(selectedProductId);
                userDTO.setActive(false);

                Call<Map<String, String>> updateUserRequest = apiService.userBlock(userDTO);
                updateUserRequest.enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        Map<String, String> responseMSG = response.body();
                        Log.e(TAG, responseMSG.get("response"));
                        Log.e(TAG, responseMSG.get("results"));
                        if (responseMSG.get("results").equals("Success")) {
                            userManageActiveAdapter.removeItem(position);

                        }

                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {

                    }
                });

            }
        });


        productView.setLayoutManager(linearLayoutManager);
        productView.setAdapter(userManageActiveAdapter);


        ApiService apiService = RetrofitClient.getApiService();
        Call<List<UserDTO>> userUpdateRequest = apiService.getUserData();

        userUpdateRequest.enqueue(new Callback<List<UserDTO>>() {
            @Override
            public void onResponse(Call<List<UserDTO>> call, Response<List<UserDTO>> response) {
                if (response.isSuccessful()) {
                    List<UserDTO> users = response.body();

                    List<UserDTO> activeUsers = new ArrayList<>();
                    for (UserDTO user : users) {
                        if (user.getActive()) {
                            activeUsers.add(user);
                        }
                    }

                    for (UserDTO user : activeUsers) {
                        userData.add(new UserData(user.getId(), user.getName(), user.getEmail(), user.getContact()));
                    }

                    userManageActiveAdapter.notifyDataSetChanged();
                } else {
                    try {
                        Log.e(TAG, "Error1: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserDTO>> call, Throwable t) {
                Log.e(TAG, "Error2: " + t.getMessage());
            }
        });
    }
        public interface OnProductSelectedListenerActive {
        void onProductSelectedActive(String productId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if the hosting activity implements the interface
        if (context instanceof ManageActiveProductsFragment.OnProductSelectedListenerActive) {
            listener = (ManageActiveProductsFragment.OnProductSelectedListenerActive) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnProductSelectedListener");
        }
    }

}