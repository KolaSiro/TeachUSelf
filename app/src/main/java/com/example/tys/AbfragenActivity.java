package com.example.tys;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    private CheckBox cbHinweisEin = null;
    private EditText edAntwort ;
    private TextView tvVersuche;
    private TextView tvHinweis1;
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
        cbHinweisEin = findViewById(R.id.cbHinweisEin);

        edAntwort =  findViewById(R.id.edAntwort);
        edAntwort.setTextColor(Color.BLACK);
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
        btnFrage.setEnabled(false);

        item = db.getOneDataSetFromWordArt(this);

        if (item == null)
        {
            Toast.makeText(this, "Woerterbuch ist leer", Toast.LENGTH_LONG).show();
            return;
        }

        tvHinweis1.setText("");

        nVersuche = 1;
        tvVersuche.setText( nVersuche + ". Versuch");

        EditText edFrage =  findViewById(R.id.edFrageWort);
        edFrage.setBackgroundColor(Color.argb(255, 50, 50, 255));
        edFrage.setTextColor(Color.WHITE);
        edAntwort.setBackgroundColor(Color.YELLOW);
        edAntwort.setTextColor(Color.BLACK);
        edAntwort.setText("");
        edAntwort.requestFocus();
        
        TextView tvPosition =  findViewById(R.id.tvPosition);

        tvPosition.setText("Kartei (1..5): " + item.getFragePos());

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
        if (sLoesung.equalsIgnoreCase(sAntwort.trim()))
        {
            edAntwort.setBackgroundColor(Color.GREEN);
            btnCheck.setEnabled(false);
            if (cbHinweisEin.isChecked() == false)
            {
                btnHinweis.setEnabled(false);
            }
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
            edAntwort.setBackgroundColor(Color.RED);
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
            if (cbHinweisEin.isChecked() == false)
            {
                btnHinweis.setEnabled(false);
            }
            btnFrage.setEnabled(false);
        }

        // Update testen
        if (nReturn < 1)
        {
            Toast.makeText(this, "Update Position failed", Toast.LENGTH_LONG).show();
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
    }

    // Benutzer ist ungeduldig und will sofort die Antwort sehen
    public void onAntwortSofortAnzeigen(View view)
    {
        TextView tvPosition = findViewById(R.id.tvPosition);
        tvPosition.setText(item.getFragePos().toString() == null ? "Kartei (1..5): " : "Kartei (1..5): " + item.getFragePos().toString());

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
        if (cbHinweisEin.isChecked())
        {
            btnHinweis.setEnabled(true);
        }
        btnFrage.setEnabled(true);
        btnAntwortAnzeigen.setEnabled(false);
    }

    public void onHinweisEinschalten(View view)
    {
            btnHinweis.setEnabled( cbHinweisEin.isChecked() ? true : false);
    }
}