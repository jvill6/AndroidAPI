package com.example.myapidemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ProductAdapter extends BaseAdapter {

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private Context context;
    private List<Product> products;
    private OnProductActionListener listener;

    public ProductAdapter(
            Context context,
            List<Product> products,
            OnProductActionListener listener) {

        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return products.get(position).getId();
    }

    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent) {

        if (convertView == null) {

            convertView = LayoutInflater.from(context)
                    .inflate(
                            R.layout.stock_display,
                            parent,
                            false
                    );
        }

        TextView txtName =
                convertView.findViewById(R.id.textView);

        Product product = products.get(position);

        if (txtName != null && product != null) {
            txtName.setText(product.getName());
        }

        return convertView;
    }
}
