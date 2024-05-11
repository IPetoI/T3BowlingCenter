package com.example.t3bowlingcenter.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.t3bowlingcenter.DataModels.FelhasznaloFoglaltIdopontjai;
import com.example.t3bowlingcenter.Adapters.FoglaltIdopontokAdapter;
import com.example.t3bowlingcenter.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfilActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProfilActivity.class.getName();

    private TextView felhasznaloNeveTV;
    private TextView felhasznaloTelefonszamaTV;
    private TextView felhasznaloEmailjeTV;

    private int jelenlegiEv;
    private int jelenlegiHonap;
    private int jelenlegiNap;
    private int jelenlegiOra;

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private  FirebaseUser felhasznalo = FirebaseAuth.getInstance().getCurrentUser();

    private FoglaltIdopontokAdapter mAdapter;
    private ArrayList<FelhasznaloFoglaltIdopontjai> palyafoglalasok;
    private CollectionReference mIdopontok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        FirebaseUser felhasznalo = FirebaseAuth.getInstance().getCurrentUser();
        if (felhasznalo != null) {
            Log.i(LOG_TAG, "Regisztrált felhasznaló.");

            felhasznaloNeveTV = findViewById(R.id.profilNevErtek);
            felhasznaloEmailjeTV = findViewById(R.id.profilEmailErtek);
            felhasznaloTelefonszamaTV = findViewById(R.id.profilTelErtek);

            DocumentReference felhasznaloRef = mFirestore.collection("Felhasznalok").document(felhasznalo.getUid());
            felhasznaloRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nevAB = documentSnapshot.getString("nev");
                    String emailAB = documentSnapshot.getString("email");
                    String telefonszamAB = documentSnapshot.getString("telefonszam");

                    felhasznaloNeveTV.setText(nevAB);
                    felhasznaloEmailjeTV.setText(emailAB);
                    felhasznaloTelefonszamaTV.setText(telefonszamAB);
                } else {
                    Log.e(LOG_TAG, "Nem található a keresett felhasználó.");
                }
            }).addOnFailureListener(e -> Log.e(LOG_TAG,"Hiba a dokumentum létrehozása során.", e));
        } else {
            Log.e(LOG_TAG, "Nem regisztrált felhasznaló.");
            finish();
        }

        alsoNavHozzaadasa();

        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewProfil);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        palyafoglalasok = new ArrayList<>();

        mAdapter = new FoglaltIdopontokAdapter(this,palyafoglalasok,this);
        mRecyclerView.setAdapter(mAdapter);

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Budapest");
        Calendar calendar = Calendar.getInstance(timeZone);
        jelenlegiEv = calendar.get(Calendar.YEAR);
        jelenlegiHonap = calendar.get(Calendar.MONTH)+1;
        jelenlegiNap = calendar.get(Calendar.DAY_OF_MONTH);
        jelenlegiOra = calendar.get(Calendar.HOUR_OF_DAY);

        mFirestore = FirebaseFirestore.getInstance();
        mIdopontok = mFirestore.collection("Foglalasok");

        adatokLekerdezeseFirestorebol();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void adatokLekerdezeseFirestorebol() {

        palyafoglalasok.clear();

        String felhasznaloEmailcim = felhasznalo.getEmail();

        mIdopontok.orderBy("melyik_nap")
                .orderBy("melyik_ora")
                .whereEqualTo("felhasznalo_email", felhasznaloEmailcim)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String melyikOra = document.getString("melyik_ora");
                String melyikNap = document.getString("melyik_nap");
                String id = document.getString("foglalas_id");
                String ar = document.getString("ar");
                int felhasznaloHanyan = Objects.requireNonNull(document.getLong("hanyan_jonnek")).intValue();

                if (melyikOra != null && melyikNap != null && idopontVizsgalata(melyikOra,melyikNap)) {
                    palyafoglalasok.add(new FelhasznaloFoglaltIdopontjai(felhasznaloHanyan+" Személy", melyikNap, melyikOra,ar ,id));
                }
            }
            if (palyafoglalasok.size() == 0) {
                TextView profilNincsFoglalas = findViewById(R.id.profilNincsFoglalas);
                profilNincsFoglalas.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    public boolean idopontVizsgalata(String idopontOraPerc, String datum) {

        String[] oraPerc = idopontOraPerc.split(":");

        String[] evHonapNap = datum.split("\\.");

        if (jelenlegiEv < Integer.parseInt(evHonapNap[0])) {
            return true;
        } else if (jelenlegiEv == Integer.parseInt(evHonapNap[0]) && jelenlegiHonap < Integer.parseInt(evHonapNap[1])) {
            return true;
        } else if (jelenlegiEv == Integer.parseInt(evHonapNap[0]) && jelenlegiHonap == Integer.parseInt(evHonapNap[1]) && jelenlegiNap < Integer.parseInt(evHonapNap[2])) {
            return true;
        } else if (jelenlegiEv == Integer.parseInt(evHonapNap[0]) && jelenlegiHonap == Integer.parseInt(evHonapNap[1]) && jelenlegiNap == Integer.parseInt(evHonapNap[2]) &&
                Integer.parseInt(oraPerc[0]) > jelenlegiOra) {
            return true;
        }

        return false;
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
        alsoNav.setSelectedItemId(R.id.profil);

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
                return true;
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

    @SuppressLint("SetTextI18n")
    public void telSzerkesztese(View view) {
        String felhasznaloJelenlegiTel =  felhasznaloTelefonszamaTV.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilActivity.this);
        builder.setTitle(Html.fromHtml("<font color='#FFFFFF'>Telefonszám szerkesztése</font>"));
        builder.setMessage(Html.fromHtml("<font color='#FFFFFF'>Írd be az új telefonszámodat:</font>"));

        final EditText editTextujTel = new EditText(ProfilActivity.this);
        editTextujTel.setInputType(InputType.TYPE_CLASS_PHONE);
        editTextujTel.setTextColor(Color.WHITE);
        if (felhasznaloJelenlegiTel.length() == 3) {
            editTextujTel.setText("+36 ");
        } else {
            editTextujTel.setText(felhasznaloJelenlegiTel);
        }
        builder.setView(editTextujTel);

        builder.setPositiveButton(Html.fromHtml("<font color='#FFFFFF'>Mentés</font>"), (dialog, which) -> {

            if (felhasznaloTelefonszamEllenorzes(editTextujTel.getText().toString())) {
                String ujTel = editTextujTel.getText().toString();

                CollectionReference felhasznaloRef = mFirestore.collection("Felhasznalok");
                CollectionReference foglalasokRef = mFirestore.collection("Foglalasok");

                felhasznaloRef.whereEqualTo("telefonszam", felhasznaloJelenlegiTel)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String documentId = document.getId();
                                    felhasznaloRef.document(documentId).update("telefonszam", ujTel)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(ProfilActivity.this, "A telefonszám sikeresen módosítva lett.", Toast.LENGTH_SHORT).show();
                                                felhasznaloTelefonszamaTV.setText(ujTel);

                                                foglalasokRef.whereEqualTo("felhasznalo_telefonszam", felhasznaloJelenlegiTel)
                                                        .get()
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                AtomicBoolean sikeresFrissites = new AtomicBoolean(false);
                                                                for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                                                    String documentId1 = document1.getId();
                                                                    foglalasokRef.document(documentId1).update("felhasznalo_telefonszam", ujTel)
                                                                            .addOnSuccessListener(aVoid1 -> sikeresFrissites.set(true))
                                                                            .addOnFailureListener(e -> sikeresFrissites.set(false));
                                                                }
                                                                if (sikeresFrissites.get()) {
                                                                    Toast.makeText(ProfilActivity.this, "Foglalások frissítve.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(ProfilActivity.this, "A foglalások frissítése sikertelen.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                Toast.makeText(ProfilActivity.this, "Lekérdezési hiba.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(ProfilActivity.this, "A telefonszám módosítása sikertelen.", Toast.LENGTH_SHORT).show());
                                }
                            } else {
                                Toast.makeText(ProfilActivity.this, "Lekérdezési hiba.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(ProfilActivity.this, "Nem megfelelő telefonszámot adtál meg!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(Html.fromHtml("<font color='#FFFFFF'>Mégse</font>"), null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.uzenet_backg);
        dialog.show();

        editTextujTel.setOnClickListener(this::felhasznaloTelefonEditTextClick);
        editTextujTel.requestFocus();
    }

    public void emailSzerkesztese(View view) {

        String felhasznaloJelenlegiEmail =  felhasznaloEmailjeTV.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilActivity.this);
        builder.setTitle(Html.fromHtml("<font color='#FFFFFF'>E-mail szerkesztése</font>"));
        builder.setMessage(Html.fromHtml("<font color='#FFFFFF'>Írd be az új e-mailed:</font>"));

        final EditText editTextujEmail = new EditText(ProfilActivity.this);
        editTextujEmail.setTextColor(Color.WHITE);
        editTextujEmail.setText(felhasznaloJelenlegiEmail);
        builder.setView(editTextujEmail);

        builder.setPositiveButton(Html.fromHtml("<font color='#FFFFFF'>Mentés</font>"), (dialog, which) -> {

            if (emailEllenorzese(editTextujEmail.getText().toString())) {
                String ujEmail = editTextujEmail.getText().toString();

                CollectionReference felhasznaloRef = mFirestore.collection("Felhasznalok");
                CollectionReference foglalasokRef = mFirestore.collection("Foglalasok");

                felhasznaloRef.whereEqualTo("email", ujEmail)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    felhasznaloRef.whereEqualTo("email", felhasznaloJelenlegiEmail)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                                        String documentId = document.getId();
                                                        felhasznaloRef.document(documentId).update("email", ujEmail)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(ProfilActivity.this, "Az e-mail sikeresen módosítva lett.", Toast.LENGTH_SHORT).show();
                                                                    felhasznaloEmailjeTV.setText(ujEmail);

                                                                    foglalasokRef.whereEqualTo("felhasznalo_email", felhasznaloJelenlegiEmail)
                                                                            .get()
                                                                            .addOnCompleteListener(task11 -> {
                                                                                if (task11.isSuccessful()) {
                                                                                    AtomicBoolean sikeresFrissites = new AtomicBoolean(false);
                                                                                    for (QueryDocumentSnapshot document1 : task11.getResult()) {
                                                                                        String documentId1 = document1.getId();
                                                                                        foglalasokRef.document(documentId1).update("felhasznalo_email", ujEmail)
                                                                                                .addOnSuccessListener(aVoid1 -> sikeresFrissites.set(true))
                                                                                                .addOnFailureListener(e -> sikeresFrissites.set(false));
                                                                                    }
                                                                                    if (sikeresFrissites.get()) {
                                                                                        Toast.makeText(ProfilActivity.this, "Foglalások frissítve.", Toast.LENGTH_SHORT).show();
                                                                                    } else {
                                                                                        Toast.makeText(ProfilActivity.this, "A foglalások frissítése sikertelen.", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                } else {
                                                                                    Toast.makeText(ProfilActivity.this, "Lekérdezési hiba.", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                })
                                                                .addOnFailureListener(e -> Toast.makeText(ProfilActivity.this, "Az e-mail módosítása sikertelen.", Toast.LENGTH_SHORT).show());
                                                    }
                                                } else {
                                                    Toast.makeText(ProfilActivity.this, "Lekérdezési hiba.", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                } else {
                                    Toast.makeText(ProfilActivity.this, "Az adott e-mail cím már foglalt!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ProfilActivity.this, "Lekérdezési hiba az új e-mail címhez.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(ProfilActivity.this, "Nem megfelelő e-mail címet adtál meg!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#FFFFFF'>Mégse</font>"), null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.uzenet_backg);
        dialog.show();
    }

    private boolean emailEllenorzese(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean felhasznaloTelefonszamEllenorzes(String tel) {
        return tel.matches("\\+36\\s(20|30|70)\\s\\d{3}\\s\\d{4}");
    }

    public void felhasznaloTelefonEditTextClick(final View view) {
        final EditText editTextujTel = (EditText) view;

        editTextujTel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String szoveg = s.toString();

                if (szoveg.length() == 7 || szoveg.length() == 11 || szoveg.length() == 4 && before != 1) {
                    if (!szoveg.endsWith(" ")) {
                        szoveg = szoveg.substring(0, szoveg.length() - 1) + " " + szoveg.charAt(szoveg.length() - 1);
                        editTextujTel.setText(szoveg);
                        editTextujTel.setSelection(szoveg.length());
                    }
                } else if (szoveg.length() == 5) {
                    if (szoveg.charAt(szoveg.length() - 1) != '2' && szoveg.charAt(szoveg.length() - 1) != '3'
                            && szoveg.charAt(szoveg.length() - 1) != '7') {
                        String csere = "+36 ";
                        editTextujTel.setText(csere);
                        editTextujTel.setSelection(csere.length());
                    } else if (before!= 1) {
                        String firstDigit = szoveg.substring(4, 5);
                        if (firstDigit.equals("2") || firstDigit.equals("3") || firstDigit.equals("7")) {
                            szoveg += "0";
                            editTextujTel.setText(szoveg);
                            editTextujTel.setSelection(szoveg.length());
                        }
                    }
                } else if (szoveg.length() > 0 && szoveg.length() <= 4) {
                    String csere = "+36 ";
                    editTextujTel.setText(csere);
                    editTextujTel.setSelection(csere.length());
                } else if (szoveg.length() > 15) {
                    szoveg = szoveg.substring(0, szoveg.length() - 1);
                    editTextujTel.setText(szoveg);
                    editTextujTel.setSelection(szoveg.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}