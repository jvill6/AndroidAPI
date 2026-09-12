package com.example.myapidemo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        implements ProductAdapter.OnProductActionListener

    {

        private ListView listViewProducts;
        private ProgressBar progressBar;
        private Button btnAddProduct;

        private ProductAdapter adapter;

        private final List<Product> productList =
                new ArrayList<>();

        private ApiService apiService;

        @Override
        protected void onCreate (Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listViewProducts =
                findViewById(R.id.listViewProducts);

        progressBar =
                findViewById(R.id.progressBar);

        btnAddProduct =
                findViewById(R.id.btnAddProduct);

        apiService =
                RetrofitClient
                        .getRetrofitInstance()
                        .create(ApiService.class);

        adapter = new ProductAdapter(
                this,
                productList,
                this
        );

        listViewProducts.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v ->
                showProductDialog(null)
        );

        loadProducts();
    }

    // ==========================================
    // THIS IS WE READ OUR DATA FROM THE API
    // ==========================================

    private void loadProducts() {

        progressBar.setVisibility(View.VISIBLE);

        Call<List<Product>> call =
                apiService.getProducts();

        call.enqueue(new Callback<List<Product>>() {

            @Override
            public void onResponse(
                    Call<List<Product>> call,
                    Response<List<Product>> response) {

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()
                        && response.body() != null) {

                    productList.clear();

                    productList.addAll(
                            response.body()
                    );

                    adapter.notifyDataSetChanged();

                } else {

                    showApiError(response);
                }
            }

            @Override
            public void onFailure(
                    Call<List<Product>> call,
                    Throwable t) {

                progressBar.setVisibility(View.GONE);

                showNetworkError(t);
            }
        });
    }

    // ==========================================
    // PROMPT AND PROCESS WHEN WE CREATE AND SUBMIT A DATA TO API
    // ==========================================

    private void createProduct(Product product) {

        progressBar.setVisibility(View.VISIBLE);

        Call<Product> call =
                apiService.createProduct(product);

        call.enqueue(new Callback<Product>() {

            @Override
            public void onResponse(
                    Call<Product> call,
                    Response<Product> response) {

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Product created successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadProducts();

                } else {

                    showApiError(response);
                }
            }

            @Override
            public void onFailure(
                    Call<Product> call,
                    Throwable t) {

                progressBar.setVisibility(View.GONE);

                showNetworkError(t);
            }
        });
    }

    // ==========================================
    // PROMPT AND PROCESS WHEN YOU WANT TO MODIFY OR UPDATE A DATA
    // ==========================================

    private void updateProduct(Product product) {

        progressBar.setVisibility(View.VISIBLE);

        Call<Product> call =
                apiService.updateProduct(
                        product.getId(),
                        product
                );

        call.enqueue(new Callback<Product>() {

            @Override
            public void onResponse(
                    Call<Product> call,
                    Response<Product> response) {

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Product updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadProducts();

                } else {

                    showApiError(response);
                }
            }

            @Override
            public void onFailure(
                    Call<Product> call,
                    Throwable t) {

                progressBar.setVisibility(View.GONE);

                showNetworkError(t);
            }
        });
    }

    // ==========================================
    // PROMPT AND PROCESS WHEN REMOVING OR DELETING DATA
    // ==========================================

    @Override
    public void onDelete(Product product) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage(
                        "Delete " +
                                product.getName() +
                                "?"
                )
                .setPositiveButton(
                        "DELETE",
                        (dialog, which) ->
                                deleteProduct(product)
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .show();
    }

    private void deleteProduct(Product product) {

        progressBar.setVisibility(View.VISIBLE);

        Call<Void> call =
                apiService.deleteProduct(
                        product.getId()
                );

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response) {

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Product deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadProducts();

                } else {

                    showApiError(response);
                }
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t) {

                progressBar.setVisibility(View.GONE);

                showNetworkError(t);
            }
        });
    }

    // ==========================================
    // AFTER EDIT
    // ==========================================

    @Override
    public void onEdit(Product product) {

        showProductDialog(product);
    }

    // ==========================================
    // ADD / EDIT DIALOG
    // ==========================================

    private void showProductDialog(Product existingProduct) {

        View view = getLayoutInflater()
                .inflate(
                        R.layout.product_dialog,
                        null
                );

        EditText edtSku =
                view.findViewById(R.id.edtSku);

        EditText edtName =
                view.findViewById(R.id.edtName);

        EditText edtDescription =
                view.findViewById(R.id.edtDescription);

        EditText edtCategory =
                view.findViewById(R.id.edtCategory);

        EditText edtPrice =
                view.findViewById(R.id.edtPrice);

        EditText edtStock =
                view.findViewById(R.id.edtStock);

        EditText edtActive =
                view.findViewById(R.id.edtActive);

        boolean editing =
                existingProduct != null;

        // Populate fields during EDIT
        if (editing) {

            edtSku.setText(
                    existingProduct.getSku()
            );

            edtName.setText(
                    existingProduct.getName()
            );

            edtDescription.setText(
                    existingProduct.getDescription()
            );

            edtCategory.setText(
                    existingProduct.getCategory()
            );

            edtPrice.setText(
                    existingProduct.getPrice()
            );

            edtStock.setText(
                    String.valueOf(
                            existingProduct
                                    .getStock_quantity()
                    )
            );

            edtActive.setText(
                    String.valueOf(
                            existingProduct
                                    .getIs_active()
                    )
            );
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                editing
                                        ? "Edit Product"
                                        : "Add Product"
                        )
                        .setView(view)
                        .setPositiveButton(
                                editing
                                        ? "UPDATE"
                                        : "CREATE",
                                null
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                try {

                    String sku =
                            edtSku.getText()
                                    .toString()
                                    .trim();

                    String name =
                            edtName.getText()
                                    .toString()
                                    .trim();

                    String description =
                            edtDescription.getText()
                                    .toString()
                                    .trim();

                    String category =
                            edtCategory.getText()
                                    .toString()
                                    .trim();

                    String price =
                            edtPrice.getText()
                                    .toString()
                                    .trim();

                    int stock =
                            Integer.parseInt(
                                    edtStock.getText()
                                            .toString()
                                            .trim()
                            );

                    int active =
                            Integer.parseInt(
                                    edtActive.getText()
                                            .toString()
                                            .trim()
                            );

                    Product product =
                            new Product(
                                    sku,
                                    name,
                                    description,
                                    category,
                                    price,
                                    stock,
                                    active
                            );

                    if (editing) {

                        product.setId(
                                existingProduct.getId()
                        );

                        updateProduct(product);

                    } else {

                        createProduct(product);
                    }

                    dialog.dismiss();

                } catch (Exception e) {

                    Toast.makeText(
                            MainActivity.this,
                            "Please enter valid values",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });

        dialog.show();
    }

    // ==========================================
    // API ERROR
    // ==========================================

    private void showApiError(
            Response<?> response) {

        String errorBody = "";

        try {

            if (response.errorBody() != null) {

                errorBody =
                        response.errorBody().string();
            }

        } catch (Exception e) {

            errorBody = e.getMessage();
        }

        String message =
                "HTTP " +
                        response.code() +
                        "\n" +
                        response.message();

        if (!errorBody.isEmpty()) {

            message += "\n\n" + errorBody;
        }

        new AlertDialog.Builder(this)
                .setTitle("API Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // ==========================================
    // NETWORK ERROR
    // ==========================================

    private void showNetworkError(Throwable t) {

        new AlertDialog.Builder(this)
                .setTitle("Connection Error")
                .setMessage(
                        t.getClass()
                                .getSimpleName()
                                + "\n\n"
                                + t.getMessage()
                )
                .setPositiveButton("OK", null)
                .show();
    }
}