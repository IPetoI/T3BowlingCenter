package com.example.t3bowlingcenter.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.t3bowlingcenter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisztracioActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisztracioActivity.class.getName();
    private static final String PREF_KEY = Objects.requireNonNull(RegisztracioActivity.class.getPackage()).toString();
    private static final int SECRET_KEY = 9898;

    private EditText felhasznaloNevET;
    private EditText felhasznaloEmailET;
    private EditText felhasznaloTelefonET;
    private EditText felhasznaloJelszoET;
    private EditText felhasznaloJelszoUjraET;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regisztracio);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 9898) {
            finish();
        }

        felhasznaloNevET = findViewById(R.id.felhasznaloNevEditText);
        felhasznaloEmailET = findViewById(R.id.felhasznaloEmailEditText);
        felhasznaloTelefonET = findViewById(R.id.felhasznaloTelefonEditText);
        felhasznaloJelszoET = findViewById(R.id.felhasznaloJelszoEditText);
        felhasznaloJelszoUjraET = findViewById(R.id.felhasznaloJelszoIsmetEditText);

        SharedPreferences preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String email = preferences.getString("email","");
        String jelszo = preferences.getString("jelszo","");

        felhasznaloEmailET.setText(email);
        felhasznaloJelszoET.setText(jelszo);
        felhasznaloJelszoUjraET.setText(jelszo);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void vissza(View view) {
        finish();
    }

    private void belepes() {
        Intent intent = new Intent(this, KezdolapActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    public void regisztralok(View view) {
        String felhasznaloNev = felhasznaloNevET.getText().toString();
        String felhasznaloEmail = felhasznaloEmailET.getText().toString();
        String felhasznaloTelefon = felhasznaloTelefonET.getText().toString();
        String felhasznaloJelszo = felhasznaloJelszoET.getText().toString();
        String felhasznaloJelszoUjra = felhasznaloJelszoUjraET.getText().toString();

        if (felhasznaloNev.isEmpty()) {
            felhasznaloNevET.setError("Kérlek add meg a nevedet!");
        } else if (felhasznaloEmail.isEmpty()) {
            felhasznaloEmailET.setError("Kérlek add meg az e-mail címedet!");
        } else if (!emailEllenorzese(felhasznaloEmail)) {
            felhasznaloEmailET.setError("Kérlek add meg egy helyes e-mail címedet!");
        } else if (felhasznaloTelefon.isEmpty()) {
            felhasznaloTelefonET.setError("Kérlek add meg a telefonszámodat!");
        } else if (!felhasznaloTelefonszamEllenorzes(felhasznaloTelefon)) {
            felhasznaloTelefonET.setError("Kérlek add meg a helyes telefonszámodat!");
        } else if (felhasznaloJelszo.isEmpty()) {
            felhasznaloJelszoET.setError("Kérlek adj meg egy jelszót!");
        } else if (!felhasznaloJelszo.equals(felhasznaloJelszoUjra)) {
            felhasznaloJelszoUjraET.setError("Nem azonos a jelszópáros!");
        } else if (felhasznaloJelszo.length() <= 6 ) {
            felhasznaloJelszoET.setError("A jelszónak legalább 7 karakter hosszúnak kell lennie!");
        } else {
            mAuth.createUserWithEmailAndPassword(felhasznaloEmail,felhasznaloJelszo).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()) {
                    Log.i(LOG_TAG,"Sikeres Regisztráció!");

                    String felhasznaloId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    Map<String, Object> felhasznaloAdatok = new HashMap<>();

                    felhasznaloAdatok.put("nev", felhasznaloNev);
                    felhasznaloAdatok.put("telefonszam", felhasznaloTelefon);
                    felhasznaloAdatok.put("email", felhasznaloEmail);

                    mFirestore.collection("Felhasznalok").document(felhasznaloId).set(felhasznaloAdatok);
                    belepes();
                } else {
                    Log.e(LOG_TAG,"Sikerertelen Regisztráció!");
                    Toast.makeText(RegisztracioActivity.this,"Sikerertelen Regisztráció! Hiba oka: "+ Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean emailEllenorzese(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean felhasznaloTelefonszamEllenorzes(String tel) {
        return tel.matches("\\+36\\s(20|30|70)\\s\\d{3}\\s\\d{4}");
    }

    public void felhasznaloTelefonEditTextClickReg(View view) {
        EditText editText = (EditText) view;
        editText.setFocusableInTouchMode(true);
        editText.setFocusable(true);

        if (editText.getText().length() == 11) {
            String csere = "+36 ";
            felhasznaloTelefonET.setText(csere);
            felhasznaloTelefonET.setSelection(csere.length());
        }

        felhasznaloTelefonET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String szoveg = s.toString();

                if (szoveg.length() == 7 || szoveg.length() == 11 || szoveg.length() == 4 && before != 1) {
                    if (!szoveg.endsWith(" ")) {
                        szoveg = szoveg.substring(0, szoveg.length() - 1) + " " + szoveg.charAt(szoveg.length() - 1);
                        felhasznaloTelefonET.setText(szoveg);
                        felhasznaloTelefonET.setSelection(szoveg.length());
                    }
                } else if (szoveg.length() == 5) {
                    if (szoveg.charAt(szoveg.length() - 1) != '2' && szoveg.charAt(szoveg.length() - 1) != '3'
                            && szoveg.charAt(szoveg.length() - 1) != '7') {
                        String csere = "+36 ";
                        felhasznaloTelefonET.setText(csere);
                        felhasznaloTelefonET.setSelection(csere.length());
                    } else if (before!= 1) {
                        String firstDigit = szoveg.substring(4, 5);
                        if (firstDigit.equals("2") || firstDigit.equals("3") || firstDigit.equals("7")) {
                            szoveg += "0";
                            felhasznaloTelefonET.setText(szoveg);
                            felhasznaloTelefonET.setSelection(szoveg.length());
                        }
                    }
                } else if (szoveg.length() > 0 && szoveg.length() <= 4) {
                    String csere = "+36 ";
                    felhasznaloTelefonET.setText(csere);
                    felhasznaloTelefonET.setSelection(csere.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}