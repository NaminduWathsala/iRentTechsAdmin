package lk.avn.irenttechsadmin.service;

import java.util.List;
import java.util.Map;

import lk.avn.irenttechsadmin.dto.UserDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ApiService {
    @POST("auth/user_data")
    Call<List<UserDTO>> getUserData();

    @POST("auth/user_block")
    Call<Map<String,String>> userBlock(@Body UserDTO userDTO);
   @POST("auth/user_active")
    Call<Map<String,String>> userActive(@Body UserDTO userDTO);


}
