package com.example.tys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.example.tys.MainActivity.ANZEIGE_SORTIERUNG;

public class AnzeigenActivity extends AppCompatActivity
{
    public static final String WORT_ENGL = "wortenglisch";
    public static final String WORT_DEUTSCH = "wortdeutsch";
    public static final String HINWEIS_1 = "hinweis1";
    public static final String HINWEIS_2 = "hinweis2";
    public static final String WORT_ART = "wortart";
    public static final String FRAGE_POSITION = "karteiposition";
    public static final String WORT_ID = "wortid";

    private ListView listView;
    private Spinner spSortierungSpinner;
    private ArrayList<Daten> daten = new ArrayList<>();
    private Daten item = null;

    @Override
    public void onResume(){
        super.onResume();
        showDaten();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anzeigen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvFilter), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.lvAllesAnzeigen);

        // Event-Handler
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int iPos, long id)
            {
                item = (Daten) listView.getItemAtPosition(iPos);
                Toast.makeText(getApplicationContext(), "Pos: " + iPos + " " + item.getWort1() + " = " + item.getWort2()  + " id="  + id, Toast.LENGTH_SHORT).show();
            }
        });

        // Event-Handler
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int iPos, long id)
            {
                item = (Daten) listView.getItemAtPosition(iPos);
                Intent intent = new Intent(getApplicationContext(), ErfassenActivity.class);
                // Die Werte von der neuen Activity uebergeben
                intent.putExtra(WORT_ENGL, item.getWort1());
                intent.putExtra(WORT_DEUTSCH, item.getWort2());
                intent.putExtra(HINWEIS_1, item.getHint1());
                intent.putExtra(HINWEIS_2, item.getHint2());
                intent.putExtra(WORT_ART, item.getArt());
                intent.putExtra(FRAGE_POSITION, item.getFragePos());
                intent.putExtra(WORT_ID, item.getId());

                startActivity(intent);
                return false;
            }
        });

        spSortierungSpinner = findViewById(R.id.spAnzeige);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String sSortierung = sharedPreferences.getString(ANZEIGE_SORTIERUNG, "");
        String []anzeigen = getResources().getStringArray(R.array.anzeigen_sortierung_anzeige);
        int nIndex = 0;

        for (String s: anzeigen)
        {
            if ( s.equals(sSortierung))
            {
                spSortierungSpinner.setSelection(nIndex);
                break;
            }
            nIndex++;
        }

        // Event - Handler
        spSortierungSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id)
            {
                Object item = adapterView.getItemAtPosition(position);

                if (item != null)
                {
                    // Anzeige aus Spinner lesen
                    final String[] anzeige =  getResources().getStringArray(R.array.anzeigen_sortierung_anzeige);
                    int nPosLookUp = adapterView.getSelectedItemPosition();
                    final String sWert = anzeige[nPosLookUp];

                    // Anzeige in Preferences schreiben
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ANZEIGE_SORTIERUNG, sWert); // User Wahl in Preferences speichern
                    editor.commit();

                    onResume();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * Holt die Daten aus der Datenbank und zeigt sie in der
     * ListView an.
     */
    private void showDaten()
    {
        daten.clear();
        DbConnection conn = DbConnection.getInstance(this);
        daten = conn.getDaten(this, -1);

        ArrayAdapter<Daten> adapter = new ArrayAdapter<Daten> (this, android.R.layout.simple_list_item_2, android.R.id.text1, daten) {

            @Override
            public View getView(int position,
                                View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                // text1 und text2 ist intern im Android FW bereits erstellt.
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(daten.get(position).getWort1() + " : " + daten.get(position).getHint1() + " Pos: " + daten.get(position).getFragePos());
                text2.setText(daten.get(position).getWort2() + " : " + daten.get(position).getHint2() + " Art: " + daten.get(position).getArt());

                return view;
            }

        };

        listView.setAdapter(adapter);
    }
}