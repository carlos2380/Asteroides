package com.example.asteroides;

import java.util.Vector;

/**
 * Created by carlos on 24/06/2016.
 */
public interface AlmacenPuntuaciones {
    public void guardarPuntuacion(int puntos,String nombre,long fecha);
    public Vector listaPuntuaciones(int cantidad);
}
