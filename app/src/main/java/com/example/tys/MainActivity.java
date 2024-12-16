package com.example.tys;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity
{
    //private static final int REQUEST_CAMERA_PERMISSION = 100;

    // Datenimport
    private static final int REQUEST_STORAGE_PERMISSION = 1;

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.spWortArt), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnImport = findViewById(R.id.btnImport);

        btnImport.setOnClickListener((v) ->
        {
            // Test ob zugriff auf Storage erlaubt ist
            checkAndRequestPermission();


            // Android oeffnet ein Fenster damit der User eine Datei auswaehlen kann.
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //intent.setType("text/csv"); // Filter für CSV-Dateien
            intent.setType("*/*"); // Filter für alles
            startActivityForResult(intent, REQUEST_STORAGE_PERMISSION);
        });

        // Testdaten erzeugen, wenn DB leer ist
        insertTestData(DbConnection.getInstance(this));

        /////////////////////////////////////////////////////////////////////////////////////////////
        // Code Beispiele fuer User-Preferences
        /////////////////////////////////////////////////////////////////////////////////////////////

        //SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        // Editor zum Schreiben von Werten erstellen
        //SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putString("wortart", "Noun"); // Wortart
        //editor.putInt("position", 1); // Karteiposition
        // editor.putBoolean("englisch", true); // Deutsch oder Englisch
//        final String[] werteArray =  getResources().getStringArray(R.array.anzeigen_sortierung_anzeige);
//        final String sID_ASC = werteArray[0];
//
//        editor.putString(ANZEIGE_SORTIERUNG, sID_ASC); // ASC id default
//        editor.commit(); // synchron
    }

    private void checkAndRequestPermission() {

        // Überprüfen, ob die Berechtigung bereits erteilt wurde
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            // Berechtigung wurde noch nicht erteilt, anfordern
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
        else
        {
            // Berechtigung ist bereits vorhanden
            Toast.makeText(this, "Storage-Berechtigung bereits erteilt", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Storage-Berechtigung erteilt", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_STORAGE_PERMISSION && resultCode == RESULT_OK)
        {
            // Uri hat auch den genauen Pfad der Datei
            Uri fileUri = data.getData();
            if (fileUri != null)
            {
                importCsvToDatabase(this, fileUri);
            }
        }
        else
        {
            Toast.makeText(this, "Storage-Berechtigung verweigert", Toast.LENGTH_SHORT).show();
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

    private void insertTestData(DbConnection db)
    {
        if ( db.getDaten(this, -1).size() > 0)
        {
            return; // Daten sind schon importiert
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



    /** Daten von CSV-File in DB importieren
     * @param inputStream Importstring im CSV-Format. Semicolon seperated
     * @return Datenstring
     */
    public String readTextFromInputStream(InputStream inputStream)
    {
        DbConnection db = DbConnection.getInstance(this);

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
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
                    // Bsp. 1;cool;kühl;ADJ;Themperature;Themperatur

                    String[] item = line.split(";");
                    // Test ob es zuviele Spalten hat
                    if (item.length > 6)
                    {
                        Log.e("TYS", "Import-Zeile hat zu viele Spalten. Max. 6");
                        continue;
                    }
                    String[] itemArray = {"", "", "", "", "", ""};

                    for (int i = 0; i < item.length; i++)
                    {
                        itemArray[i] = item[i] != null ? item[i] : "";
                    }

                    if ( itemArray[1].equals("") && itemArray[2].equals(""))
                    {
                        Toast.makeText(this, "eng+deut leer", Toast.LENGTH_SHORT).show();
                        continue;
                    }
                    Daten daten = new Daten(itemArray[1], itemArray[2], itemArray[3], itemArray[4], itemArray[5], 5);
                    try
                    {
                        db.insert(this, daten);
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
        return result.toString();
    }

    public void importCsvToDatabase(Context context, Uri csvFilePath)
    {
        try (InputStream inputStream = getContentResolver().openInputStream(csvFilePath))
        {
            // InputStream an lese Funktion uebergeben und darin in DB speichern
            String sText = readTextFromInputStream(inputStream);
            int i = 0; // debug only
        }
        catch (IOException ex)
        {
            Log.e("TYS", "Import-File: Inputstream fehlerhaft: " + ex.getMessage());
            Toast.makeText(this, "InputStream failed" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }
}