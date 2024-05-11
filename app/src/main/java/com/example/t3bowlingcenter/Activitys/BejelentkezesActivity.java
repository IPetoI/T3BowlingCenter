package com.example.t3bowlingcenter.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.t3bowlingcenter.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BejelentkezesActivity extends AppCompatActivity {

    private static final String LOG_TAG = BejelentkezesActivity.class.getName();
    private static final String PREF_KEY = Objects.requireNonNull(BejelentkezesActivity.class.getPackage()).toString();
    private static final int RC_SIGN_IT = 5432;
    private static final int SECRET_KEY = 9898;

    private EditText jelszoET;
    private EditText emailET;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bejelentkezes_main);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean autoBejelentkezes = sharedPreferences.getBoolean("loggedIn", false);

        if (autoBejelentkezes) {
            Intent intent = new Intent(this, KezdolapActivity.class);
            startActivity(intent);
            finish();
        }

        emailET = findViewById(R.id.bejelentkezesEditTextEmail);
        jelszoET = findViewById(R.id.bejelentkezesEditTextJelszo);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getString(R.string.default_web_client_id)).
                requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IT) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount fiok = task.getResult(ApiException.class);
                Log.i(LOG_TAG, "FirebaseAuthWithGoogle: " + fiok.getId());
                firebaseAuthWithGoogle(fiok.getIdToken());
            } catch (ApiException e) {
                Log.e(LOG_TAG, "Sikertelen belépés a Google fiókba!", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser felhasznalo = mAuth.getCurrentUser();
                if (felhasznalo != null) {
                    String felhasznaloNeve = felhasznalo.getDisplayName();
                    String felhasznaloEmail = felhasznalo.getEmail();
                    String felhasznaloTel = felhasznalo.getPhoneNumber();
                    String felhasznaloId = felhasznalo.getUid();

                    Map<String, Object> felhasznaloAdatok = new HashMap<>();

                    felhasznaloAdatok.put("nev", felhasznaloNeve);
                    if (felhasznaloTel != null) {
                        felhasznaloAdatok.put("telefonszam", felhasznaloTel);
                    } else {
                        felhasznaloAdatok.put("telefonszam", "+36");
                    }
                    felhasznaloAdatok.put("email", felhasznaloEmail);

                    mFirestore.collection("Felhasznalok").document(felhasznaloId).set(felhasznaloAdatok)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.i(LOG_TAG, "Felhasználó adatainak mentése sikeres.");
                                    belepes();
                                } else {
                                    Log.e(LOG_TAG, "Hiba a felhasznaló adatai mentése közben: " + Objects.requireNonNull(task1.getException()).getMessage());
                                    Toast.makeText(BejelentkezesActivity.this, "Hiba a felhasznaló adatai mentése közben.", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            } else {
                Log.e(LOG_TAG, "Sikerertelen Bejelentkezés Google fiókkal!");
                Toast.makeText(BejelentkezesActivity.this, "Sikerertelen Bejelentkezés Google fiókkal! Hiba oka: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void bejelentkezes(View view) {

        String email = emailET.getText().toString();
        String jelszo = jelszoET.getText().toString();

        if (email.isEmpty()) {
            emailET.setError("Kérlek add meg az e-mail címedet!");
            return;
        }

        if (jelszo.isEmpty()) {
            jelszoET.setError("Kérlek add meg a jelszavadat!");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, jelszo).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.i(LOG_TAG, "Sikeres Bejelentkezés!");
                belepes();
            } else {
                Log.e(LOG_TAG, "Sikerertelen Bejelentkezés!");
                Toast.makeText(BejelentkezesActivity.this, "Sikerertelen Bejelentkezés! A jelszó vagy az email nem megfelelő!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void bejelentkezesVendeg(View view) {
        mAuth.signInAnonymously().addOnSuccessListener(this, authResult -> {
        }).addOnFailureListener(this, e -> { });

        mAuth.signOut();

        mAuth.signInAnonymously().addOnSuccessListener(this, authResult -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.isAnonymous()) {
                Log.i(LOG_TAG, "Sikeres Vendég Bejelentkezés!");
                belepes();
            } else {
                Log.e(LOG_TAG, "Nem sikerült megszerezni az anonim felhasználói adatokat!");
                Toast.makeText(BejelentkezesActivity.this, "Sikertelen Vendég Bejelentkezés!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(this, e -> {
            Log.e(LOG_TAG, "Sikertelen Vendég Bejelentkezés!");
            Toast.makeText(BejelentkezesActivity.this, "Sikertelen Vendég Bejelentkezés! Hiba oka: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    public void bejelentkezesGoogle(View view) {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IT);
    }

    private void belepes() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loggedIn", true);
        editor.apply();
        Intent intent = new Intent(this, KezdolapActivity.class);
        startActivity(intent);
    }

    public void regisztracio(View view) {
        Intent intent = new Intent(this, RegisztracioActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("email", emailET.getText().toString());
        editor.putString("jelszo", jelszoET.getText().toString());
        editor.apply();
    }
}