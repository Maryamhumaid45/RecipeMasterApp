package ca.mohawk.foodrecipeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DetailActivity extends AppCompatActivity {

    TextView foodDescription, recipeName, recipePrice;
    ImageView like_imageView, dislike_imagView;
    Button btnUpdate, btnDelete;

    String recipeItemName, recipeItemPrice, recipeDescription;
    String key = "";
    String compareKey = "";
    String recipeKey = "";
    String imageUrl = "";
    String favoriteKey = "";
    String userID;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        recipeName = findViewById(R.id.txtRecipeName);
        recipePrice = findViewById(R.id.txtPrice);
        foodDescription = findViewById(R.id.txtDescription);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        like_imageView = findViewById(R.id.like_imageView);
        dislike_imagView = findViewById(R.id.dislike_imagView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("warning", "You need to Login First to unlock this feature!!");
            startActivity(intent);
        } else {
            userID = firebaseAuth.getCurrentUser().getUid();

            Bundle mBundle = getIntent().getExtras();

            if (mBundle != null) {
                recipeItemName = mBundle.getString("RecipeName");
                recipeName.setText(recipeItemName);
                recipeItemPrice = mBundle.getString("price");
                recipePrice.setText(recipeItemPrice);
                recipeDescription = mBundle.getString("Description");
                foodDescription.setText(recipeDescription);
                key = mBundle.getString("keyValue");
                recipeKey = mBundle.getString("recipeKeyValue");
                imageUrl = mBundle.getString("Image");
            }

            Log.d("TAG DetailActivity", "Recipe Key " + recipeKey);

            final DocumentReference favoriteDocumentReference = firebaseFirestore.collection("users").document(userID);
            favoriteDocumentReference.collection("Favorites").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            FoodData foodData = documentSnapshot.toObject(FoodData.class);
                            if (key.equals(foodData.getKey())) {
                                like_imageView.setVisibility(View.VISIBLE);
                                dislike_imagView.setVisibility(View.GONE);
                                favoriteKey = documentSnapshot.getId();
                                Log.d("TAG", "favoriteKey : " + favoriteKey);
                            }
                        }
                    });

            if (recipeKey != null) {
                DocumentReference documentReference = firebaseFirestore.collection("users").document(userID).collection("Recipes").document(recipeKey);
                documentReference.addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Log.d("TAG", "onEvent:" + error.getMessage());
                    } else if (value != null) {
                        compareKey = value.getString("key");
                        if (key.equals(compareKey)) {
                            btnUpdate.setVisibility(View.VISIBLE);
                            btnDelete.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }
    }

    public void btnDeleteRecipe(View view) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipe");

        reference.child(key).removeValue()
                .addOnSuccessListener(aVoid -> {
                    DocumentReference recipeDocRef = firebaseFirestore.collection("users")
                            .document(userID).collection("Recipes").document(recipeKey);

                    recipeDocRef.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                firebaseFirestore.collection("users").document(userID)
                                        .collection("Favorites").whereEqualTo("key", key)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                                doc.getReference().delete();
                                            }
                                            Snackbar.make(findViewById(R.id.txtRecipeName), "Recipe Deleted from all collections", BaseTransientBottomBar.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("TAG", "Failed to delete from Favorites: " + e.getMessage());
                                            Snackbar.make(findViewById(R.id.txtRecipeName), "Recipe deleted but failed to remove from Favorites", BaseTransientBottomBar.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(findViewById(R.id.txtRecipeName), "Failed to Delete Recipe: " + e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(findViewById(R.id.txtRecipeName), "Failed to Delete Recipe: " + e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
                });
    }

    public void btnUpdateRecipe(View view) {
        updateFavoriteRecipe(); // <<<<<<<<<<<<<<<<<<< Update favorites if needed

        startActivity(new Intent(getApplicationContext(), UpdateRecipeActivity.class)
                .putExtra("recipeNameKey", recipeName.getText().toString())
                .putExtra("descriptionKey", foodDescription.getText().toString())
                .putExtra("priceKey", recipePrice.getText().toString())
                .putExtra("oldImageUrl", imageUrl)
                .putExtra("key", key)
                .putExtra("recipeKey", recipeKey));
    }

    private void updateFavoriteRecipe() {
        if (!favoriteKey.isEmpty()) {
            DocumentReference favRef = firebaseFirestore.collection("users").document(userID)
                    .collection("Favorites").document(favoriteKey);

            favRef.update(
                            "recipeName", recipeName.getText().toString(),
                            "description", foodDescription.getText().toString(),
                            "price", recipePrice.getText().toString(),
                            "image", imageUrl
                    ).addOnSuccessListener(aVoid -> Log.d("TAG", "Favorite updated"))
                    .addOnFailureListener(e -> Log.e("TAG", "Failed to update favorite: " + e.getMessage()));
        }
    }

    public void btnLike(View view) {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userID).collection("Favorites").document(favoriteKey);
        documentReference.delete()
                .addOnSuccessListener(aVoid -> Log.d("TAG", "Recipe removed from Favorites"))
                .addOnFailureListener(e -> Snackbar.make(findViewById(R.id.txtRecipeName), "Failed to Delete Favorite" + e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show());

        dislike_imagView.setVisibility(View.VISIBLE);
        like_imageView.setVisibility(View.GONE);
    }

    public void btnDislike(View view) {
        favoriteKey = firebaseFirestore.collection("users").document(userID).collection("Favorites").document().getId();
        FoodData fireStoreFoodData = new FoodData(recipeItemName, recipeDescription, recipeItemPrice, imageUrl, key, favoriteKey);

        DocumentReference documentReference = firebaseFirestore.collection("users").document(userID).collection("Favorites").document(favoriteKey);
        documentReference.set(fireStoreFoodData)
                .addOnSuccessListener(aVoid -> Log.d("TAG", "Recipe added to Favorites"))
                .addOnFailureListener(e -> Snackbar.make(view, "Failed to Upload Favorite" + e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show());

        like_imageView.setVisibility(View.VISIBLE);
        dislike_imagView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
