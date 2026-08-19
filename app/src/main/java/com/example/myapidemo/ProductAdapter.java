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
                            R.layout.product_item,
                            parent,
                            false
                    );
        }

        TextView txtName =
                convertView.findViewById(R.id.txtName);

        TextView txtSku =
                convertView.findViewById(R.id.txtSku);

        TextView txtCategory =
                convertView.findViewById(R.id.txtCategory);

        TextView txtPrice =
                convertView.findViewById(R.id.txtPrice);

        TextView txtStock =
                convertView.findViewById(R.id.txtStock);

        Button btnEdit =
                convertView.findViewById(R.id.btnEdit);

        Button btnDelete =
                convertView.findViewById(R.id.btnDelete);

        Product product = products.get(position);

        txtName.setText(product.getName());

        txtSku.setText(context.getString(R.string.sku_format, String.valueOf(product.getSku())));
        txtCategory.setText(context.getString(R.string.category_format, String.valueOf(product.getCategory())));
        txtPrice.setText(context.getString(R.string.price_format, String.valueOf(product.getPrice())));
        txtStock.setText(context.getString(R.string.stock_format, String.valueOf(product.getStock_quantity())));

        btnEdit.setOnClickListener(v ->
                listener.onEdit(product)
        );

        btnDelete.setOnClickListener(v ->
                listener.onDelete(product)
        );

        return convertView;
    }
}
