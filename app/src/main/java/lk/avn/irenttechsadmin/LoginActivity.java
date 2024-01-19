package lk.avn.irenttechsadmin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.GsonBuilder;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getName();
    private EditText email;
    private EditText password;
    private static String errorName;
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.setStatusBarColor(Color.TRANSPARENT);

            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }



        findViewById(R.id.lg_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                email = findViewById(R.id.su_email);
                password = findViewById(R.id.su_password);

                if (email.getText() == null || email.getText().toString().trim().isEmpty()) {
                    errorName = "Please Enter Your Username";
                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");

                } else if (password.getText() == null || password.getText().toString().trim().isEmpty()) {
                    errorName = "Please Enter Your Password";
                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");

                }  else {

                    if(email.getText().toString().equals("admin") && password.getText().toString().equals("admin")){
                        Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                        startActivity(intent);
                    }else{
                        errorName = "Wrong username or Password";
                        new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");
                    }

                }
            }
        });


    }




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


}


