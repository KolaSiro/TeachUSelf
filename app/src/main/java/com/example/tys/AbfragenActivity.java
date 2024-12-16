package com.example.tys;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.example.tys.MainActivity.WORDART_FILTER;

public class AbfragenActivity extends AppCompatActivity
{
    private Daten item = null;
    private Boolean bIsEnglish = true;
    private RadioButton rbEnglisch = null;
    private RadioButton rbDeutsch = null;
    private Button btnCheck = null;
    private Button btnHinweis = null;
    private Button btnFrage = null;
    private Button btnAntwortAnzeigen = null;
    private EditText edAntwort ;
    private TextView tvVersuche;
    private TextView tvHinweis1;
    private TextView tvRichtigFalsch;
    private Integer nVersuche = 0;
    private Spinner spWortArtFilter = null;
    DbConnection db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_abfragen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = DbConnection.getInstance(this);
        rbEnglisch = findViewById(R.id.rbEnglisch);
        rbDeutsch = findViewById(R.id.rbDeutsch);
        rbEnglisch.setClickable(true);
        rbDeutsch.setClickable(false);
        edAntwort =  findViewById(R.id.edAntwort);
        btnFrage = findViewById(R.id.btnFrage);
        btnFrage.setEnabled(false);
        btnCheck = findViewById(R.id.btnCheck);
        btnCheck.setEnabled(false);
        btnHinweis = findViewById(R.id.btnHinweisAnzeigen);
        btnHinweis.setEnabled(false);
        btnAntwortAnzeigen = findViewById(R.id.btnAntwortSofortAnzeigen);
        btnAntwortAnzeigen.setEnabled(false);
        tvVersuche = findViewById(R.id.tvAnzahVersuche);
        tvHinweis1 = findViewById(R.id.tvFrageHinweis);
        tvRichtigFalsch = findViewById(R.id.tvRichtigFalsch);
        tvRichtigFalsch.setText("");
        spWortArtFilter = findViewById(R.id.spFilterFrage);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String sFilter = sharedPreferences.getString(WORDART_FILTER, "");
        String []anzeigen = getResources().getStringArray(R.array.wordarten);
        int nIndex = 0;

        for (String s: anzeigen)
        {
            if ( s.equals(sFilter))
            {
                spWortArtFilter.setSelection(nIndex);
                break;
            }
            nIndex++;
        }

        spWortArtFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1, int position, long id)
            {
                String workRequestType = adapterView.getItemAtPosition(position).toString();

                Object item = adapterView.getItemAtPosition(position);

                if (item != null)
                {
                    Toast.makeText(AbfragenActivity.this, workRequestType, Toast.LENGTH_LONG).show();

                    // Anzeige aus Spinner lesen
                    final String[] anzeige = getResources().getStringArray(R.array.wordarten);
                    int nPosLookUp = adapterView.getSelectedItemPosition();
                    final String sWert = anzeige[nPosLookUp];

                    // Anzeige in Preferences schreiben
                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(WORDART_FILTER, sWert); // User Wahl in Preferences speichern
                    editor.commit();

                    onResume();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub
            }
        });
    }

    public void onSpacheGewaehlt(View view)
    {
        rbEnglisch.setClickable(false);
        rbDeutsch.setClickable(false);
        btnFrage.setEnabled(true);
    }

    public void onFrage(View view)
    {
        tvRichtigFalsch.setText("");
        btnFrage.setEnabled(false);
        btnHinweis.setEnabled(true);

        item = db.getOneDataSetFromWordArt(this);

        if (item == null)
        {
            Toast.makeText(this, "Woerterbuch ist leer", Toast.LENGTH_LONG).show();
            return;
        }

        tvHinweis1.setText("");

        nVersuche = 1;
        tvVersuche.setText( nVersuche + ". Versuch");

        TextInputEditText edFrage =  findViewById(R.id.textInputEditTextError);
        edAntwort.setText("");
        edAntwort.requestFocus();
        
        TextView tvPosition =  findViewById(R.id.tvPosition);

        tvPosition.setText("Karteiposition (1..5): " + item.getFragePos());

        if ( rbEnglisch.isChecked() )
        {
            edFrage.setText(item.getWort1() == null ? "" : item.getWort1() );
        }
        else
        {
            edFrage.setText(item.getWort2() == null ? "" : item.getWort2());
        }

        TextView tvWortArt = findViewById(R.id.tvWortArtAbfrage);
        tvWortArt.setText(item.getArt() != null ? item.getArt() : "");
        btnCheck.setEnabled(true);
        btnAntwortAnzeigen.setEnabled(true);
    }

    /**
     * Testet Frage und Antwort. Antwort wird gruen, wenn richtig sonst rot.
     * @param view
     */
    public void onCheck(View view)
    {
        String sAntwort = edAntwort.getText().toString();
        String sLoesung = "";

        if ( rbEnglisch.isChecked() )
        {
            sLoesung = item.getWort2();
        }
        else
        {
            sLoesung = item .getWort1();
        }

        long nReturn = 0;

        // Ist die Antwort korrekt?
        // indeed = in der Tat, allerdings, wirklich
        // wenn nur ein Wort richtig ist,ist es ok.
        String sAntw = sAntwort.trim();
        String sLoesungNonCapital = sLoesung.toLowerCase();
        if ( sLoesungNonCapital.indexOf(sAntw.toLowerCase()) >= 0 && !sAntw.equals(""))
        {
            tvRichtigFalsch.setText("RICHTIG");
            tvRichtigFalsch.setTextColor(Color.GREEN);
            btnCheck.setEnabled(false);
            btnAntwortAnzeigen.setEnabled(false);
            btnFrage.setEnabled(true);

            // Antwortauswertung fuer die spaetere Sortierung
            switch(nVersuche)
            {
                case 1: // Auf Anhieb richtig
                    item.setFragePos(5);
                    nReturn = db.update(this, item);
                    break;
                case 2: // beim 2. Versuch ohne Hilfe richtig
                    item.setFragePos(4);
                    nReturn = db.update(this, item);
                    break;
                case 3: // Tipp verwendet + richtig
                    item.setFragePos(3);
                    nReturn = db.update(this, item);
                    break;
                case 4: // Tipp verwendet + falsch
                    item.setFragePos(2);
                    nReturn = db.update(this, item);
                    break;
                default: break;
            }
        }
        else
        {
            tvRichtigFalsch.setText("FALSCH");
            tvRichtigFalsch.setTextColor(Color.RED);
            nVersuche += 1;
            tvVersuche.setText( nVersuche + ". Versuch");
            if ( nVersuche > 2)
            {
                btnHinweis.setEnabled(true);
            }
        }

        if ( nVersuche >= 5)
        {
            item.setFragePos(2);
            nReturn = db.update(this, item);
            btnCheck.setEnabled(false);
            btnFrage.setEnabled(false);
        }

        // Update testen
        if (nReturn < 1)
        {
            Toast.makeText(this, "Frageposition: " + item.getFragePos(), Toast.LENGTH_LONG).show();
        }
    }

    // Hinweis zur Frage einblenden
    public void onHinweisClicked(View view)
    {
        if ( rbEnglisch.isChecked() )
        {
            tvHinweis1.setText(item.getHint1() == null ? "" : item.getHint1());
        }
        else
        {
            tvHinweis1.setText(item.getHint2() == null ? "" : item.getHint2());
        }
        if (nVersuche <5)
        {
            nVersuche++;
            tvVersuche.setText( nVersuche + ". Versuch");
        }
        btnHinweis.setEnabled(false);
    }

    // Benutzer ist ungeduldig und will sofort die Antwort sehen
    public void onAntwortSofortAnzeigen(View view)
    {
        tvRichtigFalsch.setText("");
        TextView tvPosition = findViewById(R.id.tvPosition);
        tvPosition.setText(item.getFragePos().toString() == null ? "Karteiposition (1..5): " : "Karteiposition (1..5): " + item.getFragePos().toString());

        if (item.getFragePos() < 5)
        {
            item.setFragePos(item.getFragePos() + 1); // baldmoeglichst wieder fragen, bei 1 kaeme er sofort wieder
        }

        // Loesung, Hinweis und Position anzeigen zur Hilfe
        if ( rbEnglisch.isChecked() )
        {
            edAntwort.setText(item.getWort2());
            tvHinweis1.setText(item.getHint1() == null ? "" : item.getHint1());
        }
        else
        {
            edAntwort.setText(item.getWort2());
            tvHinweis1.setText(item.getHint2() == null ? "" : item.getHint2());
        }

        db.update(this, item);

        btnCheck.setEnabled(false);
        btnFrage.setEnabled(true);
        btnAntwortAnzeigen.setEnabled(false);
    }

}