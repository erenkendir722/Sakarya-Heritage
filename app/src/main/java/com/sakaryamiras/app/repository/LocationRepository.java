package com.sakaryamiras.app.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sakaryamiras.app.model.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationRepository {

    private static final String COLLECTION = "locations";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<List<Location>> getAll() {
        return db.collection(COLLECTION)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .continueWith(this::toList);
    }

    public Task<List<Location>> getByEra(@NonNull String eraId) {
        return db.collection(COLLECTION)
                .whereEqualTo("eraId", eraId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .continueWith(this::toList);
    }

    public Task<List<Location>> getByCategory(@NonNull String categoryId) {
        return db.collection(COLLECTION)
                .whereEqualTo("categoryId", categoryId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .continueWith(this::toList);
    }

    public Task<Location> getById(@NonNull String id) {
        return db.collection(COLLECTION).document(id).get()
                .continueWith(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null || !doc.exists()) return null;
                    Location loc = doc.toObject(Location.class);
                    if (loc != null) loc.setId(doc.getId());
                    return loc;
                });
    }

    public Task<List<Location>> getByIds(@NonNull List<String> ids) {
        if (ids.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forResult(new ArrayList<>());
        }
        return db.collection(COLLECTION)
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
                .get()
                .continueWith(this::toList);
    }

    public Task<Void> create(@NonNull Location location, @NonNull String adminUid) {
        Timestamp now = Timestamp.now();
        location.setCreatedBy(adminUid);
        location.setCreatedAt(now);
        location.setUpdatedAt(now);
        DocumentReference doc = db.collection(COLLECTION).document();
        location.setId(doc.getId());
        return doc.set(location);
    }

    public Task<Void> update(@NonNull Location location) {
        if (location.getId() == null) {
            throw new IllegalArgumentException("Location id null — update için id zorunlu");
        }
        location.setUpdatedAt(Timestamp.now());
        return db.collection(COLLECTION).document(location.getId()).set(location);
    }

    public Task<Void> delete(@NonNull String id) {
        return db.collection(COLLECTION).document(id).delete();
    }

    private List<Location> toList(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
        List<Location> list = new ArrayList<>();
        if (task.getResult() == null) return list;
        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            Location loc = doc.toObject(Location.class);
            if (loc != null) {
                loc.setId(doc.getId());
                list.add(loc);
            }
        }
        return list;
    }
}
