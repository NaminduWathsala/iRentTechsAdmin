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

public class OrdersFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        tabLayout = fragment.findViewById(R.id.tabLayout);
        viewPager = fragment.findViewById(R.id.viewPager);

        OrderFragmentAdapter orderFragmentAdapter = new OrderFragmentAdapter(this);
        orderFragmentAdapter.addFragment(new OrderPendingFragment(), "Pending Orders");
        orderFragmentAdapter.addFragment(new OrderCancelPendingFragment(), "Cancel Pending");
        orderFragmentAdapter.addFragment(new OrderOngoingFragment(), "Ongoing Orders");
        orderFragmentAdapter.addFragment(new OrderCancelFragment(), "Canceled Orders");
        orderFragmentAdapter.addFragment(new OrderFinishedFragment(), "Finished Orders");

        viewPager.setAdapter(orderFragmentAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(orderFragmentAdapter.getPageTitle(position));
        }).attach();

    }
}