package com.sakaryamiras.app.ui.admin.eras;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.adapter.EraAdapter;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.repository.EraRepository;

public class AdminErasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private EraAdapter adapter;

    private final EraRepository eraRepo = new EraRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.admin_eras);
        }

        recyclerView = findViewById(R.id.list);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyView = findViewById(R.id.empty_view);

        adapter = new EraAdapter(new EraAdapter.OnEraActionListener() {
            @Override
            public void onEdit(@NonNull Era era) {
                showDialog(era);
            }

            @Override
            public void onDelete(@NonNull Era era) {
                confirmDelete(era);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showDialog(null));

        swipeRefresh.setOnRefreshListener(this::load);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        eraRepo.getAll()
                .addOnSuccessListener(items -> {
                    swipeRefresh.setRefreshing(false);
                    if (items.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setItems(items);
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                });
    }

    private void showDialog(@Nullable Era existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_era_form, null);
        TextInputEditText nameInput = view.findViewById(R.id.dialog_era_name);
        TextInputEditText nameEnInput = view.findViewById(R.id.dialog_era_name_en);
        TextInputEditText startInput = view.findViewById(R.id.dialog_era_start);
        TextInputEditText endInput = view.findViewById(R.id.dialog_era_end);
        TextInputEditText colorInput = view.findViewById(R.id.dialog_era_color);

        if (existing != null) {
            nameInput.setText(existing.getName());
            nameEnInput.setText(existing.getNameEn());
            if (existing.getStartYear() != null) {
                startInput.setText(String.valueOf(existing.getStartYear()));
            }
            if (existing.getEndYear() != null) {
                endInput.setText(String.valueOf(existing.getEndYear()));
            }
            colorInput.setText(existing.getColorHex());
        }

        new AlertDialog.Builder(this)
                .setTitle(existing != null ? R.string.edit : R.string.add)
                .setView(view)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = text(nameInput);
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, R.string.form_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Era era = existing != null ? existing : new Era();
                    era.setName(name);
                    era.setNameEn(text(nameEnInput));
                    String startStr = text(startInput);
                    era.setStartYear(TextUtils.isEmpty(startStr) ? null : Integer.parseInt(startStr));
                    String endStr = text(endInput);
                    era.setEndYear(TextUtils.isEmpty(endStr) ? null : Integer.parseInt(endStr));
                    era.setColorHex(text(colorInput));
                    Task<Void> task = existing != null
                            ? eraRepo.update(era)
                            : eraRepo.create(era);
                    task.addOnSuccessListener(unused -> {
                        Toast.makeText(this, R.string.form_save_success, Toast.LENGTH_SHORT).show();
                        load();
                    }).addOnFailureListener(e -> Toast.makeText(this,
                            getString(R.string.form_save_error, e.getMessage()),
                            Toast.LENGTH_LONG).show());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(Era era) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    eraRepo.delete(era.getId())
                            .addOnSuccessListener(unused -> load())
                            .addOnFailureListener(e -> Toast.makeText(this,
                                    getString(R.string.form_save_error, e.getMessage()),
                                    Toast.LENGTH_LONG).show());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String text(@NonNull TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
}
