package com.example.tys;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class DbConnection extends SQLiteOpenHelper
{
   private static final Object sLock = "";
   private static DbConnection sINSTANCE;

   private DbConnection(Context context)
   {
      super(context, "DatenDB", null, 1);
   }

   /**
    * Liefert den Datenbank Handle
    * @param context Context
    * @return Liefert die Verbindung
    */
   public static DbConnection getInstance(Context context)
   {
      if (sINSTANCE == null)
      {
         synchronized (sLock)
         {
            if (sINSTANCE == null)
            {
               sINSTANCE = new DbConnection(context);
            }
         }
      }
      return sINSTANCE;
   }

   @Override
   public void onCreate(SQLiteDatabase db)
   {
      // Tabelle wird in SqLite erzeugt
      db.execSQL(DatenTabelle.SQL_CREATE);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
   {
      // brauchen wir vorerst nicht
   }

   /**
    * Fuegt 1 Daten in die Tabelle TDaten und liefert
    * als return die ID des neu erzeugten Datensatzes
    * @param context Context
    * @param daten Klasse
    * @return the row ID of the newly inserted row, or -1 if an error occurred
    */
   public long insert(Context context, Daten daten) throws Exception
   {
      ContentValues values = new ContentValues();
      long nReturn = -1;
      try
      {
         values.put("wort1", daten.getWort1());
         values.put("wort2", daten.getWort2());
         values.put("wordArt", daten.getArt());
         values.put("hint1", daten.getHint1());
         values.put("hint2", daten.getHint2());
         values.put("fragePos", daten.getFragePos());

         DbConnection conn = getInstance(context);
         SQLiteDatabase db = conn.getWritableDatabase();
         nReturn = db.insert("TDaten", null, values);
         if (nReturn < 1)
            throw new Exception("Insert failed.");
      }
      catch (Exception ex)
      {
         throw new Exception(ex);
      }
      finally
      {
         return nReturn;
      }
   }

   /**
    * aendert einen bestehenden TDaten
    * @param context Kontext
    * @param daten Daten
    * @return Die Anzahl Datensaetze die betroffen waren.
    */
   public long update(Context context, Daten daten)
   {
      ContentValues values = new ContentValues();
      values.put("wort1", daten.getWort1());
      values.put("wort2", daten.getWort2());
      values.put("wordArt", daten.getArt());
      values.put("hint1", daten.getHint1());
      values.put("hint2", daten.getHint2());
      values.put("fragePos", daten.getFragePos());

      DbConnection conn = getInstance(context);
      SQLiteDatabase db = conn.getWritableDatabase();
      String sWhere = " id=" + daten.getId();
      return db.update("TDaten", values, sWhere, new String[]{});
   }

   /**
    * Löscht einen Datensatz aus der Tabelle, oder alle wenn als
    * id -1 uebergeben wird
    * @param context Context
    * @param id Primary Key
    * @return true, wenn erfolgreich gelöscht, sonst false
    */
   public boolean delete(Context context, int id)
   {
      SQLiteDatabase db = getInstance(context).getWritableDatabase();
      int nResult = 0;
      if ( id == -1)
      {
         nResult = db.delete("TDaten", "id != -1 ", null);
      }
      else
      {
         nResult = db.delete("TDaten", "id=" + id, null);
      }
      return nResult >= 1;
   }

   /**
    * Liefert die Anzahl der Datensaetze in der Datenbank
    * @param context
    * @return Anzahl der Datensaetze
    */
   public int getDatenAnzahl(Context context)
   {
      DbConnection conn = getInstance(context);
      SQLiteDatabase db = conn.getReadableDatabase();
      Cursor cur = db.rawQuery("SELECT * FROM TDaten ", null);
      return cur.getCount();
   }

   /**
    * Liefert anhand der ID die Daten
    * @param context Context
    * @param id Primary Key oder -1 fuer alle Daten
    * @return Liefert 0..n Daten
    */
   public ArrayList<Daten> getDaten(Context context, long id)
   {
      SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
      String sSortierungAnzeige = sharedPreferences.getString(MainActivity.ANZEIGE_SORTIERUNG, "");

      final String[] anzeigePositionen =  context.getResources().getStringArray(R.array.anzeigen_sortierung_anzeige);
      int nPos = 0;
      for(; nPos < anzeigePositionen.length; nPos++)
      {
         String s1 = sSortierungAnzeige;
         String s2 = anzeigePositionen[nPos];
         if (s1.equals(s2))
         {
            break;
         }
      }
      final String[] anzeigeWerte =  context.getResources().getStringArray(R.array.anzeigen_sortierung_werte);
      String sOrderBy = "";
      if (anzeigeWerte.length < 1)
      {
         Log.e("TYS", "Datensortierung order by nicht vorhanden");
      }
      else
      {
         if ( nPos < anzeigePositionen.length)
         {
            sOrderBy = anzeigeWerte[nPos];
         }
      }

      ArrayList<Daten> daten = new ArrayList<>();
      String sql;
      if (id == -1)
      {
         sql = "SELECT * FROM TDaten " + sOrderBy;
      }
      else
      {
         sql = "SELECT * FROM TDaten WHERE id = " + id ;
      }

      DbConnection conn = getInstance(context);
      SQLiteDatabase db = conn.getReadableDatabase();
      Cursor cur = db.rawQuery(sql, null);

      while (cur.moveToNext())
      {
         Daten dat = new Daten();
         dat.setId(cur.getInt(0));
         dat.setWort1(cur.getString(1));
         dat.setWort2(cur.getString(2));
         dat.setArt(cur.getString(3));
         dat.setHint1(cur.getString(4));
         dat.setHint2(cur.getString(5));
         dat.setFragePos(cur.getInt(6));
         daten.add(dat);
      }
      cur.close();
      return daten;
   }

   /**
    * Liefert 1 Datensatz anhand der Wortart: NOUN, VERB, etc.
    * @param context
    * @return 1 Datensatz
    */
   public Daten getOneDataSetFromWordArt(Context context)
   {
      SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
      String sWortArtFilterWert = sharedPreferences.getString(MainActivity.WORDART_FILTER, "");

      String sWhere = ";";

      if (sWortArtFilterWert.length() > 0)
      {
         sWhere = " WHERE wordart = '" + sWortArtFilterWert + "';";
      }
      String [] sWortArtArray = context.getResources().getStringArray(R.array.wordarten);

      if ( sWortArtArray[0].equalsIgnoreCase(sWortArtFilterWert))
      {
         sWhere = ";"; // = NOT_SPECIFIED, d.h. alle Daten
      }


      String sql = "SELECT * FROM TDaten " + sWhere;
      return getOneDataSet(context, sql);
   }

   public Daten getOneDataSetFromFragePosition(Context context)
   {
      //      SELECT foo FROM bar
      //      WHERE id >= (abs(random()) % (SELECT max(id) FROM bar))
      //      LIMIT 1;

      // String  sql = "SELECT * FROM TDaten WHERE id >= (abs(random()) %  (SELECT max(id)  FROM TDaten)) LIMIT 1";
      String sql = "SELECT * FROM TDaten order by fragePos;";

      return getOneDataSet(context, sql);
   }

   public Daten getOneDataSet(Context context, String sql)
   {
      DbConnection conn = getInstance(context);
      SQLiteDatabase db = conn.getReadableDatabase();
      Cursor cur = db.rawQuery(sql, null);

      int nRowCount = cur.getCount();
      if (nRowCount < 1)
      {
         return  new Daten("[no words available]", "[keine Wörter vorhanden]", "", "no words from wordart", "Keine Wörter in dieser Wortart", -1 );
      }
      Random rand = new Random();

      // Generate random integers in range nRowCount
      int nRndPosition = rand.nextInt(nRowCount);

      Daten dat = null;

      if ( nRndPosition >= 0 && cur.moveToPosition(nRndPosition) )
      {
         dat = new Daten();
         dat.setId(cur.getInt(0));
         dat.setWort1(cur.getString(1));
         dat.setWort2(cur.getString(2));
         dat.setArt(cur.getString(3));
         dat.setHint1(cur.getString(4));
         dat.setHint2(cur.getString(5));
         dat.setFragePos(cur.getInt(6));
      }
      else
      {
         while (cur.moveToNext())
         {
            dat = new Daten();
            dat.setId(cur.getInt(0));
            dat.setWort1(cur.getString(1));
            dat.setWort2(cur.getString(2));
            dat.setArt(cur.getString(3));
            dat.setHint1(cur.getString(4));
            dat.setHint2(cur.getString(5));
            dat.setFragePos(cur.getInt(6));
            break;
         }
      }

      cur.close();
      return dat;
   }
}
