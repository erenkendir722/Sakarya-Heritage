package com.sakaryamiras.app.ui.routes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sakaryamiras.app.R;
import com.sakaryamiras.app.adapter.RouteAdapter;
import com.sakaryamiras.app.repository.RouteRepository;

public class RoutesListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private RouteAdapter adapter;

    private final RouteRepository routeRepo = new RouteRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.routes_list);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyView = findViewById(R.id.empty_view);

        adapter = new RouteAdapter(route -> {
            Intent intent = new Intent(this, RouteDetailActivity.class);
            intent.putExtra(RouteDetailActivity.EXTRA_ROUTE_ID, route.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadRoutes);
        loadRoutes();
    }

    private void loadRoutes() {
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
}
