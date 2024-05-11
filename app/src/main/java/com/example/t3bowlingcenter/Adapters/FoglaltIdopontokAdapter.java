package com.example.t3bowlingcenter.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.t3bowlingcenter.Activitys.ProfilActivity;
import com.example.t3bowlingcenter.Activitys.RegisztracioActivity;
import com.example.t3bowlingcenter.DataModels.FelhasznaloFoglaltIdopontjai;
import com.example.t3bowlingcenter.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FoglaltIdopontokAdapter extends
        RecyclerView.Adapter<FoglaltIdopontokAdapter.ViewHolder> {
    private ArrayList<FelhasznaloFoglaltIdopontjai> elemek;
    private Context mContext;

    private int pozicio = -1;

    private static final String LOG_TAG = RegisztracioActivity.class.getName();

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private ProfilActivity mActivity;

    public FoglaltIdopontokAdapter(Context context, ArrayList<FelhasznaloFoglaltIdopontjai> elemek, ProfilActivity activity) {
        this.elemek = elemek;
        this.mContext = context;
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.foglaltidopontok_lista, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FelhasznaloFoglaltIdopontjai elem = elemek.get(position);

        holder.tablazatSzemelyekSzama.setText(elem.getSzemelyekSzama());
        holder.tablazatNap.setText(elem.getNap());
        holder.tablazatOra.setText(elem.getOra());
        holder.tablazatAr.setText(elem.getAr());

        if(holder.getAdapterPosition() > pozicio) {
            Animation animacio = AnimationUtils.loadAnimation(mContext, R.anim.sorok_csusztatas);
            holder.itemView.startAnimation(animacio);
            pozicio = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return elemek.size();
    }

    public void removeItem(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(Html.fromHtml("<font color='#FFFFFF'>Időpont törlése</font>"));
        builder.setMessage(Html.fromHtml("<font color='#FFFFFF'>Biztosan törölni szeretnéd az időpontot?</font>"));

        final String[] melyikOra = new String[1];
        final String[] melyikNap = new String[1];

        builder.setPositiveButton(Html.fromHtml("<font color='#FFFFFF'>Igen</font>"), (dialogInterface, i) -> {
            String docId = elemek.get(position).getFoglalasIdopontId();
            CollectionReference collectionRef = mFirestore.collection("Foglalasok");
            Query query = collectionRef.whereEqualTo("foglalas_id", docId);

            query.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            DocumentReference docRef = collectionRef.document(documentSnapshot.getId());

                            melyikOra[0] = documentSnapshot.getString("melyik_ora");
                            melyikNap[0] = documentSnapshot.getString("melyik_nap");

                            AlertDialog.Builder detailsDialogBuilder = new AlertDialog.Builder(mContext);
                            detailsDialogBuilder.setTitle(Html.fromHtml("<font color='#FFFFFF'>Időpont részletei</font>"));
                            detailsDialogBuilder.setMessage(Html.fromHtml("<font color='#FFFFFF'>Az időpont a következő adatokkal rendelkezik:\n\nNap: " + melyikNap[0] + "\nÓra: " + melyikOra[0] +"</font>"));
                            detailsDialogBuilder.setPositiveButton(Html.fromHtml("<font color='#FFFFFF'>Törlés</font>"), (dialogInterface1, i1) -> docRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(LOG_TAG, "Sikeresen törölted a foglalást!");
                                        elemek.remove(position);
                                        notifyItemRemoved(position);

                                        CollectionReference collectionRefHelyek = mFirestore.collection("Helyek");

                                        collectionRefHelyek.whereEqualTo("foglalas_datuma", melyikNap[0]).whereEqualTo("foglalas_ora", melyikOra[0]).get().addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        QuerySnapshot querySnapshot = task.getResult();

                                                        DocumentSnapshot existingDocument = querySnapshot.getDocuments().get(0);
                                                        int aktualisAlkalomSzam = existingDocument.getLong("alkalom_szama").intValue();
                                                        if (aktualisAlkalomSzam > 1) {
                                                            int ujAlkalomSzam = aktualisAlkalomSzam - 1;

                                                            existingDocument.getReference().update("alkalom_szama", ujAlkalomSzam);
                                                        } else if (aktualisAlkalomSzam == 1) {
                                                            existingDocument.getReference().delete().addOnSuccessListener(aVoid1 -> {
                                                                        if (queryDocumentSnapshots.size() == 1) {
                                                                            TextView profilNincsFoglalas = mActivity.findViewById(R.id.profilNincsFoglalas);
                                                                            profilNincsFoglalas.setVisibility(View.VISIBLE);
                                                                        }
                                                                        Log.d(LOG_TAG, "Sikeresen törölted a foglalást!");
                                                                    })
                                                                    .addOnFailureListener(e -> Log.d(LOG_TAG, "Sikertelenül törölted a foglalást!"));
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(e -> Log.d(LOG_TAG, "Hiba történt a lekérés során: " + e.getMessage()));
                                    })
                                    .addOnFailureListener(e -> Log.d(LOG_TAG, "Sikertelenül törölted a foglalást!")));
                            detailsDialogBuilder.setNegativeButton(Html.fromHtml("<font color='#FFFFFF'>Mégse</font>"), null);
                            AlertDialog detailsDialog = detailsDialogBuilder.create();
                            detailsDialog.getWindow().setBackgroundDrawableResource(R.drawable.uzenet_backg);
                            detailsDialog.show();
                        }
                    })
                    .addOnFailureListener(e -> Log.d(LOG_TAG, "Hiba történt a lekérés során: " + e.getMessage()));
        });

        builder.setNegativeButton(Html.fromHtml("<font color='#FFFFFF'>Mégse</font>"), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.uzenet_backg);
        alertDialog.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tablazatSzemelyekSzama;
        private TextView tablazatNap;
        private TextView tablazatOra;
        private TextView tablazatAr;

        private ImageButton idopontTorlese;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tablazatSzemelyekSzama = itemView.findViewById(R.id.tablazatSzemelyekSzama);
            tablazatNap = itemView.findViewById(R.id.tablazatNap);
            tablazatOra = itemView.findViewById(R.id.tablazatOra);
            tablazatAr = itemView.findViewById(R.id.tablazatAr);
            idopontTorlese = itemView.findViewById(R.id.idopontTorlesImageButton);

            idopontTorlese.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    removeItem(position);
                }
            });
        }
    }
}
