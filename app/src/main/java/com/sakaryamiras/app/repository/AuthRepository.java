package com.sakaryamiras.app.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    private static final String ADMINS_COLLECTION = "admins";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<AuthResult> signIn(@NonNull String email, @NonNull String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signUp(@NonNull String email, @NonNull String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public void signOut() {
        auth.signOut();
    }

    @Nullable
    public String currentEmail() {
        FirebaseUser u = currentUser();
        return u != null ? u.getEmail() : null;
    }

    public boolean isLoggedIn() {
        return currentUser() != null;
    }

    @Nullable
    public FirebaseUser currentUser() {
        return auth.getCurrentUser();
    }

    @Nullable
    public String currentUid() {
        FirebaseUser u = currentUser();
        return u != null ? u.getUid() : null;
    }

    public Task<Boolean> isAdmin(@NonNull String uid) {
        return db.collection(ADMINS_COLLECTION).document(uid).get()
                .continueWith(task -> task.getResult() != null && task.getResult().exists());
    }
}
