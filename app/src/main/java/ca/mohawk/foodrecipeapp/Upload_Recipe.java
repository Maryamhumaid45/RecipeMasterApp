package ca.mohawk.foodrecipeapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;

public class Upload_Recipe extends AppCompatActivity {

    EditText txt_name, txt_description, txt_price;
    String userID;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload__recipe);

        txt_name = findViewById(R.id.txt_recipe_name);
        txt_description = findViewById(R.id.text_description);
        txt_price = findViewById(R.id.text_price);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
    }

    public void btnUploadRecipe(View view) {
        String recipeName = txt_name.getText().toString();
        String recipeDesc = txt_description.getText().toString();
        String recipePrice = txt_price.getText().toString();

        if (recipeName.isEmpty()) {
            Snackbar.make(view, "Please enter recipe name", Snackbar.LENGTH_LONG).show();
        } else if (recipeDesc.isEmpty()) {
            Snackbar.make(view, "Please enter recipe description", Snackbar.LENGTH_LONG).show();
        } else if (recipePrice.isEmpty()) {
            Snackbar.make(view, "Please enter recipe price", Snackbar.LENGTH_LONG).show();
        } else {
            uploadRecipe(recipeName, recipeDesc, recipePrice, view);
        }
    }

    private void uploadRecipe(String name, String description, String price, View view) {
        String currentTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        String imageUrl = ""; // No image used
        FoodData foodData = new FoodData(name, description, price, imageUrl);
        FirebaseDatabase.getInstance().getReference("Recipe").child(currentTime).setValue(foodData);

        String recipeID = firebaseFirestore.collection("users").document(userID).collection("Recipes").document().getId();
        FoodData firestoreData = new FoodData(name, description, price, imageUrl, currentTime, recipeID);

        DocumentReference docRef = firebaseFirestore.collection("users").document(userID)
                .collection("Recipes").document(recipeID);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading recipe...");
        progressDialog.show();

        docRef.set(firestoreData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Snackbar.make(view, "Recipe uploaded successfully", Snackbar.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Snackbar.make(view, "Upload failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }
}
