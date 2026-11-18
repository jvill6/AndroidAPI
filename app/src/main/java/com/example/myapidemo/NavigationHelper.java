package com.example.myapidemo;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    public static void setupBottomNavigation(AppCompatActivity activity, int currentTabId) {
        BottomNavigationView bnv = activity.findViewById(R.id.bnv_button);
        if (bnv == null) return;

        bnv.setSelectedItemId(currentTabId);

        bnv.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentTabId) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.tab_Dashboard) {
                intent = new Intent(activity, MainActivity.class);
            } else if (itemId == R.id.tab_Stocks) {
                intent = new Intent(activity, StocksActivity.class);
            } else if (itemId == R.id.tab_Calculator) {
                intent = new Intent(activity, CalculatorActivity.class);
            } else if (itemId == R.id.tab_NewTank) {
                intent = new Intent(activity, NewTankActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
