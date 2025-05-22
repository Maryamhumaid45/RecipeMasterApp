package ca.mohawk.foodrecipeapp.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import ca.mohawk.foodrecipeapp.LoginActivity;
import ca.mohawk.foodrecipeapp.R;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private TextView txt_name, txt_email, txt_phone;
    private ImageView photo_imageView;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userID;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Initialize views from your XML
        txt_name = root.findViewById(R.id.txt_name);
        txt_email = root.findViewById(R.id.txt_email);
        txt_phone = root.findViewById(R.id.txt_phone);
        photo_imageView = root.findViewById(R.id.photo_imageView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // User not logged in, redirect to login screen
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.putExtra("warning", "You need to Login First to unlock this feature!!");
            startActivity(intent);
        } else {
            userID = firebaseUser.getUid();

            // No email verification UI logic here, as your XML has no views for it

            // Load user data from Firestore and update UI
            DocumentReference docRef = firebaseFirestore.collection("users").document(userID);
            docRef.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.d("NotificationsFragment", "Error fetching user data: " + error.getMessage());
                    } else if (snapshot != null && snapshot.exists()) {
                        txt_name.setText(snapshot.getString("fullName"));
                        txt_email.setText(snapshot.getString("email"));
                        txt_phone.setText(snapshot.getString("phone"));
                    }
                }
            });
        }
        return root;
    }
}
