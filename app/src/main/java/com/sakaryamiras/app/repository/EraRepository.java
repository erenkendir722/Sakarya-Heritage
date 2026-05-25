package com.sakaryamiras.app.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sakaryamiras.app.model.Era;

import java.util.ArrayList;
import java.util.List;

public class EraRepository {

    private static final String COLLECTION = "eras";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<List<Era>> getAll() {
        return db.collection(COLLECTION)
                .orderBy("startYear", Query.Direction.ASCENDING)
                .get()
                .continueWith(this::toList);
    }

    public Task<Era> getById(@NonNull String id) {
        return db.collection(COLLECTION).document(id).get()
                .continueWith(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null || !doc.exists()) return null;
                    Era era = doc.toObject(Era.class);
                    if (era != null) era.setId(doc.getId());
                    return era;
                });
    }

    private List<Era> toList(@NonNull Task<QuerySnapshot> task) {
        List<Era> list = new ArrayList<>();
        if (task.getResult() == null) return list;
        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            Era era = doc.toObject(Era.class);
            if (era != null) {
                era.setId(doc.getId());
                list.add(era);
            }
        }
        return list;
    }
}
