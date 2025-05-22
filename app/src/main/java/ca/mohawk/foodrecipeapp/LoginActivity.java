package ca.mohawk.foodrecipeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText txt_login, txt_password;
    ProgressBar loginProgressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        txt_login = findViewById(R.id.txt_login);
        txt_password = findViewById(R.id.txt_password);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        Intent intent = getIntent();
        intent.getExtras();

        if(intent.hasExtra("warning"))
        {
            Snackbar. make(findViewById(R.id.txt_login),intent.getStringExtra("warning"),BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    public void btnLogin(final View view) {
        String email = txt_login.getText().toString().trim();
        String password = txt_password.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            txt_login.setError("Email is Required.");
            return;
        }

        if (TextUtils.isEmpty(password)){
            txt_password.setError("Password is Required.");
            return;
        }

        if (password.length() < 6){
            txt_password.setError("Password Must be >= 6 Characters");
            return;
        }

        loginProgressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    finish();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                else {
                    Snackbar.make(view,"Error ! " + task.getException().getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
                    loginProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void btnRegister(View view) {
        startActivity(new Intent(this,RegisterActivity.class));
    }



    public void btnGuestLogin(View view) {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
}