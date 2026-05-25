package com.sakaryamiras.app.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.sakaryamiras.app.model.HeritageRoute;
import com.sakaryamiras.app.model.RouteStop;

import java.util.ArrayList;
import java.util.List;

public class RouteRepository {

    private static final String COLLECTION = "routes";
    private static final String STOPS_SUBCOLLECTION = "stops";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<List<HeritageRoute>> getAll() {
        return db.collection(COLLECTION)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .continueWith(this::toRouteList);
    }

    public Task<HeritageRoute> getById(@NonNull String id) {
        return db.collection(COLLECTION).document(id).get()
                .continueWith(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null || !doc.exists()) return null;
                    HeritageRoute route = doc.toObject(HeritageRoute.class);
                    if (route != null) route.setId(doc.getId());
                    return route;
                });
    }

    public Task<List<RouteStop>> getStops(@NonNull String routeId) {
        return db.collection(COLLECTION).document(routeId)
                .collection(STOPS_SUBCOLLECTION)
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .continueWith(this::toStopList);
    }

    public Task<Void> create(@NonNull HeritageRoute route,
                             @NonNull List<RouteStop> stops,
                             @NonNull String adminUid) {
        Timestamp now = Timestamp.now();
        route.setCreatedBy(adminUid);
        route.setCreatedAt(now);

        DocumentReference routeDoc = db.collection(COLLECTION).document();
        route.setId(routeDoc.getId());

        WriteBatch batch = db.batch();
        batch.set(routeDoc, route);
        for (int i = 0; i < stops.size(); i++) {
            RouteStop stop = stops.get(i);
            stop.setOrderIndex(i + 1);
            DocumentReference stopDoc = routeDoc.collection(STOPS_SUBCOLLECTION).document();
            stop.setId(stopDoc.getId());
            batch.set(stopDoc, stop);
        }
        return batch.commit();
    }

    public Task<Void> update(@NonNull HeritageRoute route, @NonNull List<RouteStop> stops) {
        if (route.getId() == null) {
            throw new IllegalArgumentException("Route id null");
        }
        DocumentReference routeDoc = db.collection(COLLECTION).document(route.getId());

        return routeDoc.collection(STOPS_SUBCOLLECTION).get().continueWithTask(task -> {
            WriteBatch batch = db.batch();
            batch.set(routeDoc, route);
            if (task.getResult() != null) {
                for (DocumentSnapshot existing : task.getResult().getDocuments()) {
                    batch.delete(existing.getReference());
                }
            }
            for (int i = 0; i < stops.size(); i++) {
                RouteStop stop = stops.get(i);
                stop.setOrderIndex(i + 1);
                DocumentReference stopDoc = routeDoc.collection(STOPS_SUBCOLLECTION).document();
                stop.setId(stopDoc.getId());
                batch.set(stopDoc, stop);
            }
            return batch.commit();
        });
    }

    public Task<Void> delete(@NonNull String routeId) {
        DocumentReference routeDoc = db.collection(COLLECTION).document(routeId);
        return routeDoc.collection(STOPS_SUBCOLLECTION).get().continueWithTask(task -> {
            WriteBatch batch = db.batch();
            if (task.getResult() != null) {
                for (DocumentSnapshot stop : task.getResult().getDocuments()) {
                    batch.delete(stop.getReference());
                }
            }
            batch.delete(routeDoc);
            return batch.commit();
        });
    }

    private List<HeritageRoute> toRouteList(@NonNull Task<QuerySnapshot> task) {
        List<HeritageRoute> list = new ArrayList<>();
        if (task.getResult() == null) return list;
        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            HeritageRoute r = doc.toObject(HeritageRoute.class);
            if (r != null) {
                r.setId(doc.getId());
                list.add(r);
            }
        }
        return list;
    }

    private List<RouteStop> toStopList(@NonNull Task<QuerySnapshot> task) {
        List<RouteStop> list = new ArrayList<>();
        if (task.getResult() == null) return list;
        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
            RouteStop s = doc.toObject(RouteStop.class);
            if (s != null) {
                s.setId(doc.getId());
                list.add(s);
            }
        }
        return list;
    }
}
