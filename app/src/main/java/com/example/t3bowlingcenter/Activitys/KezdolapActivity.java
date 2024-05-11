package com.example.t3bowlingcenter.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.t3bowlingcenter.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class KezdolapActivity extends AppCompatActivity {

    private static final String LOG_TAG = KezdolapActivity.class.getName();
    private FirebaseUser felhasznalo = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kezdolap);

        if (felhasznalo != null) {
            if (felhasznalo.isAnonymous()) {
                Log.i(LOG_TAG, "Vendég felhasznaló.");
            } else {
                Log.i(LOG_TAG, "Regisztrált felhasznaló.");
            }
        } else {
            Log.i(LOG_TAG, "Nem regisztrált felhasznaló.");
            finish();
        }

        alsoNavHozzaadasa();
    }

    public void kijelentkezes(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loggedIn", false);
        editor.apply();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), BejelentkezesActivity.class));
        finish();
    }

    public void alsoNavHozzaadasa() {
        BottomNavigationView alsoNav = findViewById(R.id.alsoNav);
        alsoNav.setSelectedItemId(R.id.kezdolap);

        alsoNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.kezdolap) {
                Log.i(LOG_TAG, "kezdolap");
                return true;
            } else if (item.getItemId() == R.id.palyafoglalas) {
                Log.i(LOG_TAG, "palyafoglalas");
                startActivity(new Intent(getApplicationContext(), PalyafoglalasActivity.class));
                overridePendingTransition(R.anim.becsuszas_jobb, R.anim.becsuszas_bal);
                finish();
                return true;
            } else if (item.getItemId() == R.id.profil) {
                Log.i(LOG_TAG, "profil");
                if(felhasznalo.isAnonymous()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle(Html.fromHtml("<font color='#FFFFFF'>" +
                            "Felhasználói regisztráció szükséges!</font>"));
                    builder.setMessage(Html.fromHtml("<font color='#FFFFFF'>" +
                            "\nCsak regisztrált felhasználóknak van profiljuk. Kérlek regisztrálj az alkalmazásban.</font>"));
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = builder.create();

                    dialog.setOnShowListener(dialogInterface -> {
                        int gombSzine = getResources().getColor(R.color.feher);
                        Button uzenetGomb = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        uzenetGomb.setTextColor(gombSzine);
                    });

                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.uzenet_backg);
                    dialog.show();
                }else {
                    startActivity(new Intent(getApplicationContext(), ProfilActivity.class));
                    overridePendingTransition(R.anim.becsuszas_jobb, R.anim.becsuszas_bal);
                    finish();
                    return true;
                }
            } else if (item.getItemId() == R.id.galleria) {
                Log.i(LOG_TAG, "galleria");
                startActivity(new Intent(getApplicationContext(), GalleriaActivity.class));
                overridePendingTransition(R.anim.becsuszas_jobb, R.anim.becsuszas_bal);
                finish();
                return true;
            } else if (item.getItemId() == R.id.rolunk) {
                Log.i(LOG_TAG, "rolunk");
                startActivity(new Intent(getApplicationContext(), RolunkActivity.class));
                overridePendingTransition(R.anim.becsuszas_jobb, R.anim.becsuszas_bal);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}