package com.example.tys;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Daten erfassen Dialog
 */
public class ErfassenActivity extends AppCompatActivity
{
    private boolean bNurAendern = false;
    private int nId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_erfassen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edEnglischerfassen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            bNurAendern = true;
            nId = extras.getInt(AnzeigenActivity.WORT_ID);

            EditText edEngl = findViewById(R.id.edEnglischerfassen);
            edEngl.setText(extras.getCharSequence(AnzeigenActivity.WORT_ENGL));
            edEngl.requestFocus();

            EditText edDeut = findViewById(R.id.edDeutschErfassen);
            edDeut.setText(extras.getCharSequence(AnzeigenActivity.WORT_DEUTSCH));

            // Wortart auslesen und Spinner setzen
            Spinner spWortArt = findViewById(R.id.spWordArt);
            String sWortArt = extras.getCharSequence(AnzeigenActivity.WORT_ART).toString();
            String [] wortArtenArray = getResources().getStringArray(R.array.wordarten); // Liste mit Wortarten

            for(int i = 0; i < wortArtenArray.length; i++)
            {
                if (sWortArt.equalsIgnoreCase(wortArtenArray[i]) )
                {
                    spWortArt.setSelection(i);
                    break;
                }
            }

            EditText edHin1 = findViewById(R.id.edHinweis1);
            edHin1.setText(extras.getCharSequence(AnzeigenActivity.HINWEIS_1));

            EditText edHin2 = findViewById(R.id.edHinweis2);
            edHin2.setText(extras.getCharSequence(AnzeigenActivity.HINWEIS_2));

            Button btnSpeichernWeiter = findViewById(R.id.btnSpeicherNext);
            btnSpeichernWeiter.setEnabled(false);

            Button btnSpeicherFertig = findViewById(R.id.btnSpeichernFertig);
            btnSpeicherFertig.setText("Änderungen speichern");
            btnSpeicherFertig.setBackgroundColor(Color.GREEN);
            btnSpeicherFertig.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        EditText edEngl = findViewById(R.id.edEnglischerfassen);
        edEngl.requestFocus();
    }

    public void onSpeichernWeiter(View view)
    {
        Speichern(view);

        // Reset
        EditText edEngl = findViewById(R.id.edEnglischerfassen);
        EditText edDeut = findViewById(R.id.edDeutschErfassen);
        edEngl.setText("");
        edDeut.setText("");
        EditText edHin1 = findViewById(R.id.edHinweis1);
        EditText edHin2 = findViewById(R.id.edHinweis2);
        edHin1.setText("");
        edHin2.setText("");
        edEngl.requestFocus();
    }

    public void onSpeichernFertig(View view)
    {
        Speichern(view);
        finish();
    }

    private void Speichern(View view)
    {
        EditText edEngl = findViewById(R.id.edEnglischerfassen);
        EditText edDeut = findViewById(R.id.edDeutschErfassen);
        String sValueEngl = edEngl.getText().toString();
        String sValueDeutsch = edDeut.getText().toString();
        EditText edHin1 = findViewById(R.id.edHinweis1);
        EditText edHin2 = findViewById(R.id.edHinweis2);
        String sHin1 = edHin1.getText().toString();
        String sHin2 = edHin2.getText().toString();


        final Spinner spWortArt = findViewById(R.id.spWordArt );
        final int pos = spWortArt.getSelectedItemPosition();
        final String[] werteArray =  getResources().getStringArray(R.array.wordarten);
        final String sWortArt = werteArray[pos];

        DbConnection db = DbConnection.getInstance(this);

        try
        {
            if (bNurAendern)
            {
                Daten daten = new Daten(sValueEngl, sValueDeutsch, sWortArt, sHin1, sHin2, 5 );
                daten.setId(nId);
                long nCount = db.update(this, daten);
                if (nCount == 1)
                {
                    Toast.makeText(this, "Daten geaendert.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "Daten NICHT geaendert.", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
            long primaryKey = db.insert(this,
                    new Daten(sValueEngl, sValueDeutsch,  sWortArt == "empty" ? "" : sWortArt , sHin1, sHin2, 5));
                Toast.makeText(this, "Erfasst: ID=" + primaryKey + " " + sValueEngl + " = " + sValueDeutsch, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage() + " Fehler beim Speichern", Toast.LENGTH_LONG).show();
        }
    }

    public void onLoeschen(View view)
    {
        DbConnection db = DbConnection.getInstance(this);
        if (db.delete(this, nId) )
        {
            Toast.makeText(this, "geloescht", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            Toast.makeText(this, "NICHT geloescht", Toast.LENGTH_SHORT).show();
        }
    }
}