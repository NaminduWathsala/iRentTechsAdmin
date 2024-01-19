package lk.avn.irenttechsadmin.custom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class Logout {
    private static String errorName;
    public static void logout(Context context) {

            SharedPreferences preferences = context.getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

    }

}
