package com.example.fake_twitter_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private static final String LOG_TAG = SignUpActivity.class.getName();
    private static final int SECRET_KEY = 2;

    private SharedPreferences preferences;
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();

    private FirebaseAuth mAuth;
    private CollectionReference usersCollection;
    private FirebaseFirestore mFirestore;

    EditText emailET;
    EditText usernameET;
    EditText passwordET;
    EditText passwordConfirmET;
    EditText phoneET;
    RadioGroup genderRG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secret_key != 1) {
            finish();
        }

        emailET = findViewById(R.id.editTextSignUpEmail);
        passwordET = findViewById(R.id.editTextSignUpPassword);
        passwordConfirmET = findViewById(R.id.editTextSignUpPasswordConfirm);
        usernameET = findViewById(R.id.editTextSignUpUsername);
        phoneET = findViewById(R.id.editTextPhone);
        genderRG = findViewById(R.id.radioGroupGender);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String savedEmail = preferences.getString("email", "");
        String savedPassword = preferences.getString("password", "");

        emailET.setText(savedEmail);
        passwordET.setText(savedPassword);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        usersCollection = mFirestore.collection("Users");

    }

    public void cancel(View view) {
        finish();
    }


    private void startTwitter(){
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    public void sign_up(View view) {

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordConfirm = passwordConfirmET.getText().toString();
        String username = usernameET.getText().toString();
        String phone = phoneET.getText().toString();

        int checkedID = genderRG.getCheckedRadioButtonId();
        RadioButton radioButton = genderRG.findViewById(checkedID);
        String gender = radioButton.getText().toString();


        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this,"Passwords do not mach!",Toast.LENGTH_LONG).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.i(LOG_TAG,"User created successfully!");
                        usersCollection.document(mAuth.getUid()).set(new User(mAuth.getUid(),username,phone,gender));
                        startTwitter();
                    }else {
                        Log.e(LOG_TAG,"User creation failed!");
                    }
                }
            });


             /*
            Log.i(LOG_TAG,"Email: "+email);
            Log.i(LOG_TAG,"Username: "+username);
            Log.i(LOG_TAG,"Password: "+password);
            Log.i(LOG_TAG,"PasswordConfirm: "+passwordConfirm);
            Log.i(LOG_TAG,"Phone Number: "+phone);
            Log.i(LOG_TAG,"Gender: "+gender);
             */
        }

    }
}