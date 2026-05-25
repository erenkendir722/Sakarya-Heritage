package com.sakaryamiras.app.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sakaryamiras.app.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private static final String COLLECTION = "categories";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<List<Category>> getAll() {
        return db.collection(COLLECTION)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .continueWith(this::toList);
    }

    public Task<Category> getById(@NonNull String id) {
        return db.collection(COLLECTION).document(id).get()
                .continueWith(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null || !doc.exists()) return null;
                    Category cat = doc.toObject(Category.class);
                    if (cat != null) cat.setId(doc.getId());
                    return cat;
                });
    }

    public Task<Void> create(@NonNull Category category) {
        com.google.firebase.firestore.DocumentReference doc = db.collection(COLLECTION).document();
        category.setId(doc.getId());
        return doc.set(category);
    }

    public Task<Void> update(@NonNull Category category) {
        if (category.getId() == null) {
            throw new IllegalArgumentException("Category id null");
        }
        return db.collection(COLLECTION).document(category.getId()).set(category);
    }

    public Task<Void> delete(@NonNull String id) {
        return db.collection(COLLECTION).document(id).delete();
    }

    private List<Category> toList(@NonNull Task<QuerySnapshot> task) {
        List<Category> list = new ArrayList<>();
        if (task.getResult() == null) return list;
        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            Category c = doc.toObject(Category.class);
            if (c != null) {
                c.setId(doc.getId());
                list.add(c);
            }
        }
        return list;
    }
}
