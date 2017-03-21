package com.example.asteroides;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Juego extends AppCompatActivity {
    private VistaJuego vistaJuego;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.juego);
        vistaJuego = (VistaJuego) findViewById(R.id.VistaJuego);
        vistaJuego.activarSensores();
        vistaJuego.setPadre(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        vistaJuego.getThread().pausar();
        vistaJuego.desactivarSensores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vistaJuego.getThread().reanudar();
        vistaJuego.activarSensores();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vistaJuego.getThread().detener();
        vistaJuego.desactivarSensores();
    }
}
