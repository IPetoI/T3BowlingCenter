package com.example.t3bowlingcenter.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.t3bowlingcenter.DataModels.Kepek;
import com.example.t3bowlingcenter.Adapters.KepekAdapter;
import com.example.t3bowlingcenter.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class GalleriaActivity extends AppCompatActivity {

    private static final String LOG_TAG = GalleriaActivity.class.getName();

    private ArrayList<Kepek> kepek;
    private KepekAdapter mAdapter;

    private FirebaseUser felhasznalo = FirebaseAuth.getInstance().getCurrentUser();
    private CollectionReference mKepek;

    private int queryLimit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galleria);

        if (felhasznalo != null) {
            Log.i(LOG_TAG, "Regisztrált felhasznaló. "+felhasznalo);
        } else {
            Log.i(LOG_TAG, "Nem regisztrált felhasznaló.");
            finish();
        }

        alsoNavHozzaadasa();

        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewGalleria);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        kepek = new ArrayList<>();

        mAdapter = new KepekAdapter(this, kepek);
        mRecyclerView.setAdapter(mAdapter);

        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mKepek = mFirestore.collection("Kepek");
        adatokLekerese();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);
    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 10;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;
            }
            adatokLekerese();
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private void adatokLekerese() {
        kepek.clear();

        mKepek.orderBy("hanyadik").limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Kepek kep = document.toObject(Kepek.class);
                kepek.add(kep);
            }
            if (kepek.size() == 0) {
                kepekBetoltese();
                adatokLekerese();
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    public void kepekBetoltese() {
        String[] kepSzam = getResources().getStringArray(R.array.hanyadikKep);
        TypedArray kep = getResources().obtainTypedArray(R.array.kepElerese);

        for (int i=0; i < kepSzam.length; i++) {
            mKepek.add(new Kepek(kepSzam[i], kep.getResourceId(i,0)));
        }
        kep.recycle();
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
        alsoNav.setSelectedItemId(R.id.galleria);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }
}