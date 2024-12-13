package com.example.tys;

import java.io.Serializable;

/**
 * Ein Datensatz
 */
public class Daten implements Serializable
{
   private int id;
   private String wort1;
   private String wort2;
   private String art;
   private String hint1;
   private String hint2;
   private Integer fragePos;

   public Daten()
   {
   }

   public Daten(String sWort1, String sWort2, String sArt, String sHinweis1, String sHinweis2, Integer nFragePos)
   {
      this.wort1 = sWort1;
      this.wort2 = sWort2;
      this.art = sArt;
      this.hint1 = sHinweis1;
      this.hint2 = sHinweis2;
      this.fragePos = nFragePos;
   }

   public Daten(int id, String sWort1, String sWort2, String sArt, String sHinweis1, String sHinweis2, Integer nFragePos)
   {
      this.id = id;
      this.wort1 = sWort1;
      this.wort2 = sWort2;
      this.art = sArt;
      this.hint1 = sHinweis1;
      this.hint2 = sHinweis2;
      this.fragePos = nFragePos;
   }
   /**
    * Liefert den Primary Key
    * @return Key
    */
   public int getId()
   {
      return id;
   }

   /**
    * Setzt den Primary Key
    * @param id
    */
   public void setId(int id)
   {
      this.id = id;
   }

   public String getWort1()
   {
      return wort1;
   }

   public String getWort2()
   {
      return wort2;
   }

   public String getArt()
   {
      return art;
   }


   public void setWort1(String sWort1)
   {
      this.wort1 = sWort1;
   }

   public void setWort2(String sWort2)
   {
      this.wort2 = sWort2;
   }

   public void setArt(String sArt)
   {
      this.art = sArt;
   }

   public String getHint1()
   {
      return hint1;
   }

   public void setHint1(String sHint1)
   {
      this.hint1 = sHint1;
   }

   public String getHint2()
   {
      return hint2;
   }

   public void setHint2(String sHint2)
   {
      this.hint2 = sHint2;
   }

   public Integer getFragePos()
   {
      return fragePos;
   }

   public void setFragePos(Integer fragePos)
   {
      this.fragePos = fragePos;
   }


   /**
    *  Ueberschreibt toString() der Basisklasse und liefert den Namen und den Primary Key
    * @return
    */
//   @Override
//   public String toString()
//   {
//      return this.id + " " + this.wort1;
//   }
}