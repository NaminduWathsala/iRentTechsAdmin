package lk.avn.irenttechsadmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import lk.avn.irenttechsadmin.adapter.OrderFragmentAdapter;
import lk.avn.irenttechsadmin.adapter.ProductManagementAdapter;

public class ManageProductsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        tabLayout = fragment.findViewById(R.id.tabLayout);
        viewPager = fragment.findViewById(R.id.viewPager);

        ProductManagementAdapter productManagementAdapter = new ProductManagementAdapter(this);
        productManagementAdapter.addFragment(new ManageActiveProductsFragment(), "Active Products");
        productManagementAdapter.addFragment(new ManageBlockedProductsFragment(), "Blocked Products");

        viewPager.setAdapter(productManagementAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(productManagementAdapter.getPageTitle(position));
        }).attach();
    }
}