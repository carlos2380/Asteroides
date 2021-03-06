package com.example.asteroides;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Vector;

/**
 * Created by carlos on 28/06/2016.
 */
public class AlmacenPuntuacionesSQLite extends SQLiteOpenHelper implements AlmacenPuntuaciones{
    //Métodos de SQLiteOpenHelper
    public AlmacenPuntuacionesSQLite(Context context) {
        super(context, "puntuaciones", null, 1);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE puntuaciones ("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "puntos INTEGER, nombre TEXT, fecha LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En caso de una nueva versión habría que actualizar las tablas
    }

    //Métodos de AlmacenPuntuaciones
    public void guardarPuntuacion(int puntos, String nombre, long fecha) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO puntuaciones VALUES ( null, "+
                puntos+", '"+nombre+"', "+fecha+")");
        db.close();
    }

    public Vector listaPuntuaciones(int cantidad) {
        Vector result = new Vector();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT puntos, nombre FROM " +
                "puntuaciones ORDER BY puntos DESC LIMIT " +cantidad, null);

        /* METODO QUERY
        String[] CAMPOS = {"puntos", "nombre"};
        Cursor cursor=db.query("puntuaciones", CAMPOS, null, null,
       null, null, "puntos DESC", Integer.toString(cantidad));*/
        while (cursor.moveToNext()){
            result.add(cursor.getInt(0)+" " +cursor.getString(1));
        }
        cursor.close();
        db.close();
        return result;
    }
}
