package com.example.t3bowlingcenter.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.t3bowlingcenter.DataModels.Idopontok;
import com.example.t3bowlingcenter.Adapters.IdopontokAdapter;
import com.example.t3bowlingcenter.DataModels.PalyafoglalasAdatai;
import com.example.t3bowlingcenter.NotificationHandler;
import com.example.t3bowlingcenter.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PalyafoglalasActivity extends AppCompatActivity {

    private static final String LOG_TAG = PalyafoglalasActivity.class.getName();

    private TextView napIdopont;
    private TextView hanyanJonnek;
    private TextView hanyanJonnekCim;
    private TextView palyafoglalasUzenetek;
    private TextView mennyibeKerulTextView;

    private EditText felhasznaloNeve;
    private EditText felhasznaloTelefonszama;
    private EditText felhasznaloEmailje;

    private int jelenlegiEv;
    private String jelenlegiHonap;
    private int jelenlegiNap;
    private int jelenlegiOra;
    private int vizsgalandoOra;
    private int kivalasztottNapI;
    private int kivalasztottNap;
    private String kivalasztottHonap;
    private int kivalasztottEv;
    private String melyikIdopont;
    private int ar = 0;

    private NotificationHandler mNotificationHandler;

    private ArrayList<Idopontok> idopontok= new ArrayList<>();
    private final String[] idopontSzam = new String[] {"10:00", "11:00", "12:00", "13:00", "14:00",
            "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};

    private static String melyikOra = "";

    private IdopontokAdapter mAdapter;

    private int hanyanJonnekErtek = 2;

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private CollectionReference mFoglalas;
    private CollectionReference mHelyek;
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();

    private String jelenlegiFelhasznaloId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    private FirebaseUser felhasznalo = FirebaseAuth.getInstance().getCurrentUser();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palyafoglalas);

        felhasznaloNeve = findViewById(R.id.palyafoglalasNevEditText);
        felhasznaloEmailje = findViewById(R.id.palyafoglalasEmailEditText);
        felhasznaloTelefonszama = findViewById(R.id.palyafoglalasTelEditText);
        palyafoglalasUzenetek = findViewById(R.id.palyafoglalasUzenetek);
        mennyibeKerulTextView = findViewById(R.id.mennyibeKerulTextView);
        TextView regisztralvaOlcsobb = findViewById(R.id.regisztralvaOlcsobb);

        if (felhasznalo != null) {
            if (felhasznalo.isAnonymous()) {
                Log.d(LOG_TAG, "Vendég felhasznaló.");
                regisztralvaOlcsobb.setVisibility(View.VISIBLE);
                palyafoglalasUzenetek.setVisibility(View.GONE);
            } else {
                Log.d(LOG_TAG, "Regisztrált felhasznaló.");
                regisztralvaOlcsobb.setVisibility(View.GONE);
                palyafoglalasUzenetek.setVisibility(View.GONE);

                DocumentReference felhasznaloRef = mFirestore.collection("Felhasznalok").document(jelenlegiFelhasznaloId);
                felhasznaloRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nevAB = documentSnapshot.getString("nev");
                        String emailAB = documentSnapshot.getString("email");
                        String telefonszamAB = documentSnapshot.getString("telefonszam");

                        felhasznaloNeve.setText(nevAB);
                        felhasznaloEmailje.setText(emailAB);
                        felhasznaloTelefonszama.setText(telefonszamAB);
                    } else {
                        Log.e(LOG_TAG, "Nem található a keresett felhasználó.");
                    }
                }).addOnFailureListener(e -> Log.e(LOG_TAG,"Hiba a dokumentum létrehozása során.", e));
            }
        } else {
            Log.d(LOG_TAG, "Nem regisztrált felhasznaló.");
            finish();
        }
        mFoglalas= mFirestore.collection("Foglalasok");
        mHelyek= mFirestore.collection("Helyek");

        alsoNavHozzaadasa();

        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewPalyafoglalas);

        mAdapter = new IdopontokAdapter(getData());
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Budapest");
        Calendar calendar = Calendar.getInstance(timeZone);
        jelenlegiEv = calendar.get(Calendar.YEAR);
        jelenlegiHonap = String.valueOf(calendar.get(Calendar.MONTH)+1);
        jelenlegiNap = calendar.get(Calendar.DAY_OF_MONTH);
        kivalasztottNapI = calendar.get(Calendar.DAY_OF_WEEK);
        jelenlegiOra = calendar.get(Calendar.HOUR_OF_DAY);

        kivalasztottEv = calendar.get(Calendar.YEAR);
        kivalasztottHonap = String.valueOf(calendar.get(Calendar.MONTH)+1);
        kivalasztottNap = calendar.get(Calendar.DAY_OF_MONTH);

        adatbazisTisztitas();

        napIdopont = findViewById(R.id.palyafoglalasNapTextView);

        if (Integer.parseInt(jelenlegiHonap) < 10) {
            jelenlegiHonap = "0"+jelenlegiHonap;
            kivalasztottHonap = "0"+kivalasztottHonap;
        }

        napIdopont.setText(jelenlegiEv+"."+(jelenlegiHonap)+"."+jelenlegiNap+".");
        melyikIdopont = jelenlegiEv+"."+(jelenlegiHonap)+"."+jelenlegiNap+".";

        idopontokBetoltese();

        hanyanJonnek = findViewById(R.id.hanyanJottokTextView);
        hanyanJonnekCim = findViewById(R.id.hanyanJottokTextViewCim);

        mNotificationHandler = new NotificationHandler(this);
    }

    public void adatbazisTisztitas() {

        Calendar maxCalendar = Calendar.getInstance();
        maxCalendar.set(jelenlegiEv, Integer.parseInt(jelenlegiHonap), jelenlegiNap);
        long max = maxCalendar.getTimeInMillis();

        Task<QuerySnapshot> helyekTask = mHelyek.get();
        Task<QuerySnapshot> foglalasTask = mFoglalas.get();

        Tasks.whenAll(helyekTask, foglalasTask).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot helyekQuerySnapshot = helyekTask.getResult();
                QuerySnapshot foglalasQuerySnapshot = foglalasTask.getResult();

                for (QueryDocumentSnapshot document : helyekQuerySnapshot) {
                    String datum = document.getString("foglalas_datuma");

                    String[] datumST = datum != null ? datum.split("\\.") : new String[0];

                    int foglalasEv = Integer.parseInt(datumST[0]);
                    int foglalasHonap = Integer.parseInt(datumST[1]);
                    int foglalasNap = Integer.parseInt(datumST[2]);

                    Calendar foglalasCalendar = Calendar.getInstance();
                    foglalasCalendar.set(foglalasEv, foglalasHonap, foglalasNap);
                    long foglalas = foglalasCalendar.getTimeInMillis();

                    if (foglalas <= max) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.i(LOG_TAG, "Dokumentum törlése sikeres."))
                                .addOnFailureListener(e -> Log.e(LOG_TAG, "Dokumentum törlése sikertelen.", e));
                    }
                }
                for (QueryDocumentSnapshot document : foglalasQuerySnapshot) {
                    String datum = document.getString("melyik_nap");

                    String[] datumST = datum != null ? datum.split("\\.") : new String[0];

                    int foglalasEv = Integer.parseInt(datumST[0]);
                    int foglalasHonap = Integer.parseInt(datumST[1]);
                    int foglalasNap = Integer.parseInt(datumST[2]);

                    Calendar foglalasCalendar = Calendar.getInstance();
                    foglalasCalendar.set(foglalasEv, foglalasHonap, foglalasNap);
                    long foglalas = foglalasCalendar.getTimeInMillis();

                    if (foglalas <= max) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.i(LOG_TAG, "Dokumentum törlése sikeres."))
                                .addOnFailureListener(e -> Log.e(LOG_TAG, "Dokumentum törlése sikertelen.", e));
                    }
                }

            } else {
                Log.e(LOG_TAG, "Firebase lekérdezés sikertelen", task.getException());
            }
        });
    }

    private ArrayList<Idopontok> getData() {
        for (String idopont : idopontSzam) {
            idopontok.add(new Idopontok(idopont, false));
        }
        return idopontok;
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

    public void napKivalasztas(View view) {

        @SuppressLint("SetTextI18n")
        DatePickerDialog dialog = new DatePickerDialog(this, R.style.CustomDatePickerDialog, (view1, ev, honap, nap) -> {

            Calendar kivalasztottCalendar = Calendar.getInstance();
            kivalasztottCalendar.set(ev, honap, nap);

            kivalasztottNapI = kivalasztottCalendar.get(Calendar.DAY_OF_WEEK);
            kivalasztottEv = kivalasztottCalendar.get(Calendar.YEAR);
            kivalasztottHonap = String.valueOf(kivalasztottCalendar.get(Calendar.MONTH)+1);
            kivalasztottNap = kivalasztottCalendar.get(Calendar.DAY_OF_MONTH);

            if (Integer.parseInt(kivalasztottHonap) < 10) {
                kivalasztottHonap = "0" + kivalasztottHonap;
            }

            napIdopont.setText(ev+"."+kivalasztottHonap+"."+nap+".");

            melyikIdopont = ev+"."+kivalasztottHonap+"."+nap+".";

            if(kivalasztottNapI == 1) {
                Log.i(LOG_TAG,"Vasarnap van.");
            }else if(kivalasztottNapI == 2) {
                Log.i(LOG_TAG,"Hetfő van.");
            }else if(kivalasztottNapI == 3) {
                Log.i(LOG_TAG,"Kedd van.");
            }else if(kivalasztottNapI == 4) {
                Log.i(LOG_TAG,"Szerda van.");
            }else if(kivalasztottNapI == 5) {
                Log.i(LOG_TAG,"Csutortok van.");
            }else if(kivalasztottNapI == 6) {
                Log.i(LOG_TAG,"Pentek van.");
            }else if(kivalasztottNapI == 7) {
                Log.i(LOG_TAG,"Szombat van.");
            }
            idopontokBetoltese();

        },jelenlegiEv,Integer.parseInt(jelenlegiHonap)-1,jelenlegiNap);

        Calendar maxCalendar = Calendar.getInstance();
        maxCalendar.set(jelenlegiEv, Integer.parseInt(jelenlegiHonap)+6, jelenlegiNap);
        long max = maxCalendar.getTimeInMillis();

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.getDatePicker().setMaxDate(max);

        dialog.show();
    }

    public static void melyikOraban(String idopont) {
        melyikOra = idopont;
    }

    public void setAr(int ara) {
        ar = ara;

        if (!felhasznalo.isAnonymous()) {
            ar = (int) Math.round(ar - (ar * 0.05));
        }
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    public void idopontokBetoltese() {
        mAdapter.setKijelolesekKiszedese(true);
        melyikOra = "";

        idopontok.clear();

        if (kivalasztottEv == jelenlegiEv && kivalasztottHonap.equals(jelenlegiHonap) && kivalasztottNap == jelenlegiNap) {
            for (int i=0; i < idopontSzam.length; i++) {

                if (kivalasztottNapI == 2 || kivalasztottNapI == 3 || kivalasztottNapI == 4 || kivalasztottNapI == 5 || kivalasztottNapI == 6) {
                    if (i >= 4 && i <10) {
                        if (idopontMegnezese(idopontSzam[i])) {
                            idopontok.add(new Idopontok(idopontSzam[i],false));
                        }
                    }
                    if (kivalasztottNapI == 2 || kivalasztottNapI == 3 || kivalasztottNapI == 4 || kivalasztottNapI == 5) {
                        setAr(4000);
                        mennyibeKerulTextView.setText(ar+" Ft");
                    } else {
                        setAr(5000);
                        mennyibeKerulTextView.setText(ar+" Ft");
                    }
                }else if(kivalasztottNapI == 7) {
                    if (i >= 4 && i <14) {
                        if (idopontMegnezese(idopontSzam[i])) {
                            idopontok.add(new Idopontok(idopontSzam[i],false));
                        }
                    }
                    setAr(5000);
                    mennyibeKerulTextView.setText(ar+" Ft");
                }else if(kivalasztottNapI == 1) {
                    if (i <10) {
                        if (idopontMegnezese(idopontSzam[i])) {
                            idopontok.add(new Idopontok(idopontSzam[i],false));
                        }
                    }
                    setAr(5000);
                    mennyibeKerulTextView.setText(ar+" Ft");
                }
            }
        } else {
            for (int i=0; i < idopontSzam.length; i++) {

                if (kivalasztottNapI == 2 || kivalasztottNapI == 3 || kivalasztottNapI == 4 || kivalasztottNapI == 5 || kivalasztottNapI == 6) {
                    if (i >= 4 && i <10) {
                        idopontok.add(new Idopontok(idopontSzam[i],false));
                    }
                    if (kivalasztottNapI == 2 || kivalasztottNapI == 3 || kivalasztottNapI == 4 || kivalasztottNapI == 5) {
                        setAr(4000);
                        mennyibeKerulTextView.setText(ar+" Ft");
                    } else {
                        setAr(5000);
                        mennyibeKerulTextView.setText(ar+" Ft");
                    }
                }else if(kivalasztottNapI == 7) {
                    if (i >= 4 && i <14) {
                        idopontok.add(new Idopontok(idopontSzam[i],false));
                    }
                    setAr(5000);
                    mennyibeKerulTextView.setText(ar+" Ft");
                }else if(kivalasztottNapI == 1) {
                    if (i <10) {
                        idopontok.add(new Idopontok(idopontSzam[i],false));
                    }
                    setAr(5000);
                    mennyibeKerulTextView.setText(ar+" Ft");
                }
            }
        }
        AtomicInteger completedTasks = new AtomicInteger();
        int totalTasks = idopontok.size();
        TextView palyafogNincsFoglalas = findViewById(R.id.palyafoglalasNincsHelyTextView);

        for (Idopontok idopont : idopontok) {
            mHelyek.whereEqualTo("foglalas_datuma", melyikIdopont)
                    .whereEqualTo("foglalas_ora",  idopont.getIdopontok()).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Long mennyi = document.getLong("alkalom_szama");
                                String ora = document.getString("foglalas_ora");

                                if (ora != null && ora.equals(idopont.getIdopontok()) && mennyi > 5) {
                                    idopontok.remove(idopont);
                                }
                            }
                        } else {
                            Log.e(LOG_TAG, "Firebase lekérdezés sikertelen.", task.getException());
                        }
                        completedTasks.getAndIncrement();
                        if (completedTasks.get() == totalTasks) {
                            if (idopontok.size() == 0) {
                                palyafogNincsFoglalas.setVisibility(View.VISIBLE);
                            } else {
                                palyafogNincsFoglalas.setVisibility(View.GONE);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    });
        }
        if (idopontok.size() == 0) {
            palyafogNincsFoglalas.setVisibility(View.VISIBLE);
        } else {
            palyafogNincsFoglalas.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @SuppressLint("SetTextI18n")
    public void kevesebbenJonnek(View view) {
        if (hanyanJonnekErtek > 1) {
            hanyanJonnekErtek--;
            hanyanJonnek.setText(String.valueOf(hanyanJonnekErtek));

            if (hanyanJonnekErtek == 1) {
                hanyanJonnekCim.setText("Egyedül?");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void tobbenJonnek(View view) {
        if (hanyanJonnekErtek < 6) {
            hanyanJonnekErtek++;
            hanyanJonnek.setText(String.valueOf(hanyanJonnekErtek));

            if (hanyanJonnekErtek > 1) {
                hanyanJonnekCim.setText("Hányan jöttök?");
            }
        }
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    public void palyaLeoglalasa(View view) {

        if (melyikOra.equals("")) {
            palyafoglalasUzenetek.setVisibility(View.VISIBLE);
            palyafoglalasUzenetek.setText("Kérlek válassz időpontot!");
        } else if (felhasznaloNeve.getText().toString().isEmpty()) {
            felhasznaloNeve.setError("Kérlek add meg a neved!");
        } else if (felhasznaloTelefonszama.getText().toString().isEmpty() || felhasznaloTelefonszama.getText().toString().length() == 4) {
            felhasznaloTelefonszama.setError("Kérlek add meg a telefonszámodat!");
        } else if (felhasznaloEmailje.getText().toString().isEmpty()) {
            felhasznaloEmailje.setError("Kérlek add meg a e-mail címed!");
        } else if (!felhasznaloTelefonszamEllenorzes(felhasznaloTelefonszama.getText().toString())) {
            felhasznaloTelefonszama.setError("Kérlek add meg a helyes telefonszámodat!");
        } else if (!emailEllenorzese(felhasznaloEmailje.getText().toString())) {
            felhasznaloEmailje.setError("Kérlek add meg egy valós e-mail címedet!");
        } else {
            mFoglalas.orderBy("melyik_nap").get().addOnSuccessListener(queryDocumentSnapshots -> {
                foglalasLetrehozasa();

                mAdapter.notifyDataSetChanged();
            });

            palyafoglalasUzenetek.setVisibility(View.VISIBLE);
            palyafoglalasUzenetek.setText("Sikeresen lefoglaltál egy pályát!");
        }
    }

    public void foglalasLetrehozasa() {

        String documentId = UUID.randomUUID().toString();

        idopontMegnezese(melyikOra);

        if (!melyikOra.isEmpty()) {

            mFoglalas.add(new PalyafoglalasAdatai(felhasznaloNeve.getText().toString(),felhasznaloEmailje.getText().toString()
                    ,felhasznaloTelefonszama.getText().toString(), hanyanJonnekErtek,melyikIdopont,melyikOra,ar+" Ft", documentId));

            mHelyek.whereEqualTo("foglalas_datuma", melyikIdopont).whereEqualTo("foglalas_ora", melyikOra).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();

                    if (querySnapshot.isEmpty()) {
                        Map<String, Object> foglalasData = new HashMap<>();
                        foglalasData.put("foglalas_datuma", melyikIdopont);
                        foglalasData.put("foglalas_ora", melyikOra);
                        foglalasData.put("alkalom_szama", 1);

                        mHelyek.add(foglalasData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.i(LOG_TAG,"A dokumentum sikeresen létrejött.");
                                    mAdapter.setKijelolesekKiszedese(true);
                                    melyikOra = "";
                                })
                                .addOnFailureListener(e -> Log.e(LOG_TAG,"Hiba a dokumentum létrehozása során.", e));
                    } else {
                        DocumentSnapshot existingDocument = querySnapshot.getDocuments().get(0);
                        int aktualisAlkalomSzam = Objects.requireNonNull(existingDocument.getLong("alkalom_szama")).intValue();

                        if (aktualisAlkalomSzam < 6) {
                            int ujAlkalomSzam = aktualisAlkalomSzam + 1;

                            existingDocument.getReference().update("alkalom_szama", ujAlkalomSzam)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.i(LOG_TAG, "A dokumentum módosítása sikeres.");
                                        idopontokBetoltese();
                                        mAdapter.setKijelolesekKiszedese(true);
                                        melyikOra = "";
                                    })
                                    .addOnFailureListener(e -> Log.e(LOG_TAG, "Hiba a dokumentum módosítása során."));
                        } else {
                            Log.i(LOG_TAG,"Nincs szabad hely "+ existingDocument.getLong("foglalas_ora") +"-kor.");
                        }
                    }
                } else {
                    Log.e(LOG_TAG,"Hiba a lekérdezés során.");
                }
            });

            int megfeleloHonap;
            if (Integer.parseInt(kivalasztottHonap) <10) {
                megfeleloHonap = Integer.parseInt(String.valueOf(kivalasztottHonap.charAt(1)))-1;
            } else {
                megfeleloHonap = Integer.parseInt(kivalasztottHonap)-1;
            }

            mNotificationHandler.sendNotification("Pálya lefoglalva "+hanyanJonnekErtek+" személynek. \nIdőpont: "+melyikIdopont+"  "+melyikOra+" óra. \nÁr: "+ar+" Ft.");

            new Handler().postDelayed(() -> palyafoglalasUzenetek.setVisibility(View.GONE), 6000);
        }
    }

    public boolean idopontMegnezese(String idopontOraPerc) {
        String[] oraPerc = idopontOraPerc.split(":");
        vizsgalandoOra = Integer.parseInt(oraPerc[0]);

        return jelenlegiOra < vizsgalandoOra;
    }

    private boolean emailEllenorzese(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean felhasznaloTelefonszamEllenorzes(String tel) {

        return tel.matches("\\+36\\s(20|30|70)\\s\\d{3}\\s\\d{4}");
    }

    public void felhasznaloTelefonEditTextClickFog(View view) {
        EditText editText = (EditText) view;
        editText.setFocusableInTouchMode(true);
        editText.setFocusable(true);

        if (editText.getText().length() == 11 || editText.getText().length() == 3) {
            String csere = "+36 ";
            felhasznaloTelefonszama.setText(csere);
            felhasznaloTelefonszama.setSelection(csere.length());
        }

        felhasznaloTelefonszama.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String szoveg = s.toString();

                Log.d(LOG_TAG, start+" "+ before);

                if (szoveg.length() == 7 || szoveg.length() == 11 || szoveg.length() == 4 && before != 1) {
                    if (!szoveg.endsWith(" ")) {
                        szoveg = szoveg.substring(0, szoveg.length() - 1) + " " + szoveg.charAt(szoveg.length() - 1);
                        felhasznaloTelefonszama.setText(szoveg);
                        felhasznaloTelefonszama.setSelection(szoveg.length());
                    }
                } else if (szoveg.length() == 5) {
                    if (szoveg.charAt(szoveg.length() - 1) != '2' && szoveg.charAt(szoveg.length() - 1) != '3'
                            && szoveg.charAt(szoveg.length() - 1) != '7') {
                        String csere = "+36 ";
                        felhasznaloTelefonszama.setText(csere);
                        felhasznaloTelefonszama.setSelection(csere.length());
                    } else if (before!= 1) {
                        String firstDigit = szoveg.substring(4, 5);
                        if (firstDigit.equals("2") || firstDigit.equals("3") || firstDigit.equals("7")) {
                            szoveg += "0";
                            felhasznaloTelefonszama.setText(szoveg);
                            felhasznaloTelefonszama.setSelection(szoveg.length());
                        }
                    }
                } else if (szoveg.length() > 0 && szoveg.length() <= 4) {
                    String csere = "+36 ";
                    felhasznaloTelefonszama.setText(csere);
                    felhasznaloTelefonszama.setSelection(csere.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void alsoNavHozzaadasa() {
        BottomNavigationView alsoNav = findViewById(R.id.alsoNav);
        alsoNav.setSelectedItemId(R.id.palyafoglalas);

        alsoNav.setOnItemSelectedListener( item -> {
            if (item.getItemId() == R.id.kezdolap) {
                Log.i(LOG_TAG, "kezdolap");
                startActivity(new Intent(getApplicationContext(), KezdolapActivity.class));
                overridePendingTransition(R.anim.becsuszas_jobb, R.anim.becsuszas_bal);
                finish();
                return true;
            } else if (item.getItemId() == R.id.palyafoglalas) {
                Log.i(LOG_TAG, "palyafoglalas");
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
}