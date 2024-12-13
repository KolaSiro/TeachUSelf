package com.example.tys;

/**
 * Schema-Klasse um eine Tabelle: 'TDaten' in SQL-Lite zu erzeugen.
 */
public class DatenTabelle
{
   public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS TDaten ( " +
           " id INTEGER PRIMARY KEY , wort1 TEXT, wort2 TEXT, wordArt TEXT, hint1 TEXT, hint2 TEXT,  fragePos INTEGER ) ";
}
