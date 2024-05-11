package com.example.t3bowlingcenter.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.t3bowlingcenter.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RolunkActivity extends AppCompatActivity {

    private static final String LOG_TAG = RolunkActivity.class.getName();

    private FirebaseUser felhasznalo = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rolunk);

        if (felhasznalo != null) {
            Log.i(LOG_TAG, "Regisztrált felhasznaló.");
        } else {
            Log.i(LOG_TAG, "Nem regisztrált felhasznaló.");
            finish();
        }

        alsoNavHozzaadasa();

        String emailCim = "t3bowlingc@gmail.com";
        String mobilSzam = "+36 30 123 4567";

        ImageView emailKep = findViewById(R.id.emailIcon);
        emailKep.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("E-mail cím", emailCim);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(RolunkActivity.this, "E-mail cím a vágolapon!", Toast.LENGTH_SHORT).show();
        });

        ImageView mobilKep = findViewById(R.id.mobilIcon);
        mobilKep.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Mobil szám", mobilSzam);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(RolunkActivity.this, "Mobil szám a vágolapon!", Toast.LENGTH_SHORT).show();
        });
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

    public void googleMapLink(View view) {
        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/place/46%C2%B014'38.8%22N+20%C2%B009'56.6%22E/" +
                "@46.2441067,20.1631411,573m/data=!3m2!1e3!4b1!4m4!3m3!8m2!3d46.244103!4d20.165716?authuser=0&entry=ttu");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void webLink(View view) {
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://galaxybowlingc.web.app/kezdolap"));
        startActivity(urlIntent);
    }

    public void alsoNavHozzaadasa() {
        BottomNavigationView alsoNav = findViewById(R.id.alsoNav);
        alsoNav.setSelectedItemId(R.id.rolunk);

        alsoNav.setOnItemSelectedListener( item -> {
            if (item.getItemId() == R.id.kezdolap) {
                Log.i(LOG_TAG, "kezdolap");
                startActivity(new Intent(getApplicationContext(), KezdolapActivity.class));
                overridePendingTransition(R.anim.becsuszas_jobb, R.anim.becsuszas_bal);
                finish();
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