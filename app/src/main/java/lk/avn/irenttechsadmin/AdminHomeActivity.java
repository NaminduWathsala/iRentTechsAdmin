package lk.avn.irenttechsadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class AdminHomeActivity extends AppCompatActivity implements ManageBlockedProductsFragment.OnProductSelectedListener, ManageActiveProductsFragment.OnProductSelectedListenerActive{

    private static final String TAG = AdminHomeActivity.class.getName();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private MenuItem activeMenuItem;
    private TextView toolbar_name;
    private ImageButton search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        toolbar_name = findViewById(R.id.toolbar_name);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        search = findViewById(R.id.admin_product_search);

        setSupportActionBar(toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(AdminHomeActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        SharedPreferences preferences = getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

        View headerView = navigationView.getHeaderView(0);

        TextView tv_name_nav = headerView.findViewById(R.id.side_nav_name);
        TextView tv_email_nav = headerView.findViewById(R.id.side_nav_email);

        String name = preferences.getString("NAME", null);
        String email = preferences.getString("EMAIL", null);

        if (name != null) {
            tv_name_nav.setText(name);
        }

        if (email != null) {
            tv_email_nav.setText(email);
        }


        Menu menu = navigationView.getMenu();

//        if ("namiduwathsala@gmail.com".equals(email)) {
//            menu.setGroupVisible(R.id.adminAddProducts, true);
//        }

        addFragment(new DashboardFragment());

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem itemId) {

                if (activeMenuItem != null) {
                    activeMenuItem.setChecked(false);
                }

                if (itemId.getItemId() == R.id.admin_add_products) {
                    addFragment(new AddProductsFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                } else if (itemId.getItemId() == R.id.admin_products_list) {
                    addFragment(new ListProductFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                }else if (itemId.getItemId() == R.id.admin_orders) {
                    addFragment(new OrdersFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                }else if (itemId.getItemId() == R.id.admin_dashboard) {
                    addFragment(new DashboardFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                }else if (itemId.getItemId() == R.id.admin_manage_products) {
                    addFragment(new ManageProductsFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                }else if (itemId.getItemId() == R.id.admin_manage_users) {
                    addFragment(new UserProfilesFragment());
                    activeMenuItem = itemId;
                    itemId.setChecked(true);
                    drawerLayout.close();
                }

                return true;
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment searchFragment = new SearchFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layouts, searchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        toolbar.setNavigationIcon(R.drawable.menu);

    }

    @Override
    public void onProductSelected(String productId) {
        Bundle bundle = new Bundle();
        bundle.putString("product_id", productId);

        SingleProductViewFragment singleProductViewFragment = new SingleProductViewFragment();
        singleProductViewFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layouts, singleProductViewFragment)
                .addToBackStack(null)
                .commit();
    }
    @Override
    public void onProductSelectedActive(String productId) {
        Bundle bundle = new Bundle();
        bundle.putString("product_id", productId);

        SingleProductViewFragment singleProductViewFragment = new SingleProductViewFragment();
        singleProductViewFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layouts, singleProductViewFragment)
                .addToBackStack(null)
                .commit();
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layouts, fragment);
        transaction.commit();
    }
}