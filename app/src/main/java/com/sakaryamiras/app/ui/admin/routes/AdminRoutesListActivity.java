package com.sakaryamiras.app.ui.admin.routes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.adapter.RouteAdapter;
import com.sakaryamiras.app.model.HeritageRoute;
import com.sakaryamiras.app.repository.RouteRepository;

public class AdminRoutesListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private RouteAdapter adapter;

    private final RouteRepository routeRepo = new RouteRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.admin_routes);
        }

        recyclerView = findViewById(R.id.list);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyView = findViewById(R.id.empty_view);

        adapter = new RouteAdapter(this::openForm);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> openFormForNew());

        swipeRefresh.setOnRefreshListener(this::load);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        routeRepo.getAll()
                .addOnSuccessListener(routes -> {
                    swipeRefresh.setRefreshing(false);
                    if (routes.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setItems(routes);
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                });
    }

    private void openForm(HeritageRoute route) {
        new AlertDialog.Builder(this)
                .setTitle(route.getName())
                .setItems(new String[]{getString(R.string.edit), getString(R.string.delete)},
                        (d, which) -> {
                            if (which == 0) {
                                Intent intent = new Intent(this, AdminRouteFormActivity.class);
                                intent.putExtra(AdminRouteFormActivity.EXTRA_ROUTE_ID, route.getId());
                                startActivity(intent);
                            } else {
                                confirmDelete(route);
                            }
                        })
                .show();
    }

    private void openFormForNew() {
        startActivity(new Intent(this, AdminRouteFormActivity.class));
    }

    private void confirmDelete(HeritageRoute route) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (d, w) -> deleteRoute(route))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteRoute(HeritageRoute route) {
        routeRepo.delete(route.getId())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.form_save_success, Toast.LENGTH_SHORT).show();
                    load();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        getString(R.string.form_save_error, e.getMessage()),
                        Toast.LENGTH_LONG).show());
    }
}
