package com.example.tys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import java.io.BufferedReader;
import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity
{
    private static final int CREATE_FILE_REQUEST_CODE = 1;
    private static final int OPEN_FILE_REQUEST_CODE = 2;
    private Uri fileUri;

    // Datenfilterung anhand von der Wortart
    public static final String WORDART_FILTER = "WORDART_FILTER";

    // Datensortierung
    public static final String ANZEIGE_SORTIERUNG = "ANZEIGE_SORTIERUNG";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button btnCreateCsv = findViewById(R.id.btnExportieren);
        Button btnOpenCsv = findViewById(R.id.btnImport);

        btnCreateCsv.setOnClickListener(v -> createCsvFile());
        btnOpenCsv.setOnClickListener(v -> openCsvFile());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spWortArt), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Testdaten erzeugen, wenn DB leer ist
        insertTestData(DbConnection.getInstance(this));

//        /////////////////////////////////////////////////////////////////////////////////////////////
//        // Code Beispiele fuer User-Preferences
//        /////////////////////////////////////////////////////////////////////////////////////////////
//
//        //SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
//        // Editor zum Schreiben von Werten erstellen
//        //SharedPreferences.Editor editor = sharedPreferences.edit();
//        //editor.putString("wortart", "Noun"); // Wortart
//        //editor.putInt("position", 1); // Karteiposition
//        // editor.putBoolean("englisch", true); // Deutsch oder Englisch
////        final String[] werteArray =  getResources().getStringArray(R.array.anzeigen_sortierung_anzeige);
////        final String sID_ASC = werteArray[0];
////
////        editor.putString(ANZEIGE_SORTIERUNG, sID_ASC); // ASC id default
////        editor.commit(); // synchron
    }

    // Methode zum Erstellen und Schreiben einer CSV-Datei
    private void createCsvFile()
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "_TUS_export.csv");
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    // Methode zum Öffnen und Lesen einer CSV-Datei
    private void openCsvFile()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();

            if (requestCode == CREATE_FILE_REQUEST_CODE)
            {
                writeCsvData(fileUri);
            }
            else if (requestCode == OPEN_FILE_REQUEST_CODE)
            {
                readCsvData(fileUri);
            }
        }
    }

    // CSV-Daten lesen
    private void readCsvData(Uri uri)
    {
        try
        {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            readTextFromInputStream(inputStream);
        }
        catch (Exception ex)
        {
            Log.e("TYS", "Fehler beim Lesen der Datei", ex);
            Toast.makeText(this, "Lesefehler: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // CSV-Daten schreiben
    private void writeCsvData(Uri uri)
    {
        DbConnection db = DbConnection.getInstance(this);
        ArrayList<Daten> daten = db.getDaten(this, -1);
        String sExport = "";
        int i = 0;
        for ( ;i < daten.size(); i++)
        {
            Daten item = daten.get(i);
            sExport += item.getId() + ";" + item.getWort1() + ";" + item.getWort2() + ";"
                    + item.getArt() + ";" + item.getHint1() + ";" + item.getHint2() + ";"
                    + item.getFragePos() + "\n";
        }

        String csvContent =  sExport;

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri))
        {
            if (outputStream != null)
            {
                outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
                Toast.makeText(this, "CSV-Datei mit " + i + " Daten erstellt.", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            Log.e("MainActivity", "Fehler beim Schreiben der Datei", e);
            Toast.makeText(this, "Fehler beim Schreiben der Datei", Toast.LENGTH_SHORT).show();
        }
    }

    public void onWoerterErfassen(View view)
    {
        final Intent intent = new Intent(this, ErfassenActivity.class);
        startActivity(intent);
    }

    public void onAbfragen(View view)
    {
        final Intent intent = new Intent(this, AbfragenActivity.class);
        startActivity(intent);
    }

    public void onAllesAnzeigen(View view)
    {
        final Intent intent = new Intent(this, AnzeigenActivity.class);
        startActivity(intent);
    }

    /**
     * Fuegt standard Daten ein, wenn die Datenbank leer ist
     * @param db Datenbank Handle
     * 2024.12.18 Datenbank check verbessert.
     */
    private void insertTestData(DbConnection db)
    {
        if ( db.getDatenAnzahl(this) > 0)
        {
            // Daten sind schon importiert
            // Wir wollen die Kundendaten NICHT ueberschreiben
            return;
        }

        try
        {
            // Wort Arten
            final String NOT_SPECIFIED = getResources().getStringArray(R.array.wordarten)[0];
            final String NOUN = getResources().getStringArray(R.array.wordarten)[1]; // Hauptwort
            final String VERB = getResources().getStringArray(R.array.wordarten)[2]; // Verb
            final String ADJECTIVE = getResources().getStringArray(R.array.wordarten)[3]; // Adjektiv
            final String ADVERB = getResources().getStringArray(R.array.wordarten)[4]; // Adverb
            final String IDIOM = getResources().getStringArray(R.array.wordarten)[5]; // Redewendung
            final String SENTENCE = getResources().getStringArray(R.array.wordarten)[6]; // ganzer oder Teilsatz

            // Daten fuer Import, damit die DB am Anfang nicht leer ist.
            db.insert(this, new Daten("Hello", "Hallo", NOT_SPECIFIED, "greeting", "Begrüssung", 3));
            db.insert(this, new Daten("red", "rot", ADJECTIVE, "color", "Farbe", 2));
            db.insert(this, new Daten("green", "grün", ADJECTIVE, "color", "Farbe", 1));
            db.insert(this, new Daten("blue", "blau", ADJECTIVE, "color", "Farbe", 5));
            db.insert(this, new Daten("table", "Tisch", NOUN, "furniture", "Möbel", 2));
            db.insert(this, new Daten("chair", "Stuhl", NOUN, "furniture", "Möbel", 4));
            db.insert(this, new Daten("lamp", "Lampe", NOUN, "furniture", "Möbel", 2));
            db.insert(this, new Daten("carpet", "Teppich", NOUN, "furniture", "Möbel", 5));
            db.insert(this, new Daten("car", "Auto", NOUN, "Vehicle on the street", "Fahrzeug", 3));
            db.insert(this, new Daten("bus", "Bus", NOUN, "Vehicle on the street", "Fahrzeug", 4));
            db.insert(this, new Daten("eat", "essen", VERB, "food", "Nahrung", 1));
            db.insert(this, new Daten("meat", "Fleisch", NOUN, "food", "Nahrung", 3));
            db.insert(this, new Daten("milk", "Milch", NOUN, "food", "Nahrung", 2));
            db.insert(this, new Daten("water", "Wasser", NOUN, "food", "Nahrung", 4));
            db.insert(this, new Daten("bread", "Brot", NOUN, "food", "Nahrung", 2));
            db.insert(this, new Daten("bird", "Vogel", NOUN, "animal", "Tier", 3));
            db.insert(this, new Daten("dog", "Hund", NOUN, "animal", "Tier", 2));
            db.insert(this, new Daten("swift", "schnell", ADJECTIVE, "", "", 1));
            db.insert(this, new Daten("striker", "Stürmer", NOUN, "player", "Spieler", 1));
            db.insert(this, new Daten("Bite the bullet", "In den sauren Apfel beissen", IDIOM, "Idiom", "Redewendung", 2));
            db.insert(this, new Daten("Cutting corners", "Am falschen Ende sparen", IDIOM, "Idiom", "Redewendung", 2));
            db.insert(this, new Daten("I am ill", "Ich bin krank", SENTENCE, "", "Gesundheitszustand", 2));
            db.insert(this, new Daten("now", "jetzt" , ADVERB, "", "Zeitpunkt", 2));
            db.insert(this, new Daten("yesterday", "gestern" , ADVERB, "", "Zeitpunkt", 2));
            db.insert(this, new Daten("often", "oft" , ADVERB, "", "Wiederholung", 2));
            db.insert(this, new Daten("dayly", "täglich" , ADVERB, "", "Zeitpunkt", 2));
        }
        catch( Exception ex)
        {
            Toast.makeText(this, ex.getMessage() + " Testdaten failed", Toast.LENGTH_LONG).show();
        }
    }

    public void onAllesNeuMischen(View view)
    {
        DbConnection db = DbConnection.getInstance(this);
        ArrayList<Daten> daten = db.getDaten(this, -1);
        int nAnzahlFragen = daten.size();

        for(int i = 0; i < nAnzahlFragen; i++)
        {
            Random r = new Random(System.currentTimeMillis());
            int low = 1; // inclusive
            int high = 6; // exclusive
            int rndZahl = r.nextInt(high-low) + low;

            daten.get(i).setFragePos(rndZahl);
            db.update(this, daten.get(i));
        }
        Button btnAllesNeuMischen = findViewById(R.id.btnNeuMischen);
        btnAllesNeuMischen.setEnabled(false);
    }

    /** Daten von CSV-File in DB importieren.
     * 16.12.24 Counter for User added
     * @param inputStream Importstring im CSV-Format. Semicolon seperated
     * @return Datenstring
     */
    public String readTextFromInputStream(InputStream inputStream)
    {
        DbConnection db = DbConnection.getInstance(this);
        int nCounter = 0;

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {

            // Test ob der Datensatz schon lokal vorhanden ist
            ArrayList<Daten> datenDic = db.getDaten(this, -1);
            int nSize = datenDic.size();

            Boolean bFirstLine = true;
            String line;

            while ((line = reader.readLine()) != null)
            {
                // CR/LF anhaengen am Schluss der Zeile
                result.append(line).append("\n");

                if (bFirstLine)
                {
                    // die 1. Zeile ist die Spaltenbeschreibung die wir nicht benoetigen
                    bFirstLine = false;
                }
                else
                {
                    // ID;wort1;wort2;wordart;hinweis1;hinweis2;position
                    // Bsp. 1;cool;kühl;ADJ;Themperature;Themperatur

                    String[] item = line.split(";");
                    // Test ob es zuviele Spalten hat
                    if (item.length > 7)
                    {
                        Log.e("TYS", "Import-Zeile hat zu viele Spalten. Max. 7");
                        continue;
                    }
                    String[] itemArray = {"", "", "", "", "", "", ""};

                    for (int i = 0; i < item.length; i++)
                    {
                        itemArray[i] = item[i] != null ? item[i] : "";
                    }

                    if ( itemArray[1].equals("") && itemArray[2].equals(""))
                    {
                        Toast.makeText(this, "eng+deut leer", Toast.LENGTH_SHORT).show();
                        continue;
                    }


                    boolean bAlreadyInDictionary = false;

                    for(int j = 0; j < nSize; j++)
                    {
                        if ( itemArray[1].equals(datenDic.get(j).getWort1()))
                        {
                            // Datensatz schon vorhanden also ignorieren
                            bAlreadyInDictionary = true;
                        }
                    }

                    if (bAlreadyInDictionary)
                    {
                        continue;
                    }

                    // Test ob Wordart leer ist, was zwar erlaubt waere.
                    String sWordArtText = itemArray[3];
                    if ( sWordArtText.equals(""))
                    {
                        sWordArtText = "NOT_SPECIFIED";
                    }
                    Daten daten = new Daten(-1, itemArray[1], itemArray[2], sWordArtText, itemArray[4], itemArray[5], 5);
                    try
                    {
                        db.insert(this, daten);
                        nCounter++;
                    }
                    catch (Exception ex)
                    {
                        Log.e("TYS", "SQL-Insert failed: " + ex.getMessage());
                        Toast.makeText(this, "SQL-Insert failed" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        catch (IOException ex)
        {
            Log.e("TYS", "InputStreamReader failed: " + ex.getMessage());
            Toast.makeText(this, "Error-Datenimport: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
        Toast.makeText(this, nCounter +  " Daten importiert." , Toast.LENGTH_LONG).show();
        return result.toString();
    }
}