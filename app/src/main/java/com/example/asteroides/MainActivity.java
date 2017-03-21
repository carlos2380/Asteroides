package com.example.asteroides;

import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private GestureLibrary libreria;

    private Button btnPuntuaciones;
    private TextView textView;
    private MediaPlayer mp;
    public static AlmacenPuntuaciones almacen = new AlmacenPuntuacionesArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp = MediaPlayer.create(this, R.raw.audio);
        mp.start();

        btnPuntuaciones = (Button) findViewById(R.id.btnPuntuaciones);
        textView = (TextView) findViewById(R.id.textView);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.giro_con_zoom);

        textView.startAnimation(animation);
        almacen = new AlmacenPuntuacionesSQLite(this);
        /*new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish()btnSalir.setOnClickListener(;
            }
        });*/

        libreria= GestureLibraries.fromRawResource(this, R.raw.gestures);

        if(!libreria.load()) {

            finish();

        }

        GestureOverlayView gesturesView =

                (GestureOverlayView) findViewById(R.id.gestures);

        gesturesView.addOnGesturePerformedListener(this);
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
    }
    @Override protected void onStart() {
        super.onStart();
        mp.start();
        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onResume() {
        super.onResume();
        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onPause() {
        super.onPause();
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();

    }

    @Override protected void onStop() {
        super.onStop();
        mp.pause();
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onRestart() {
        super.onRestart();
        mp.start();
        Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        mp.pause();
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1234 && resultCode==RESULT_OK && data!=null) {
            int puntuacion = data.getExtras().getInt("puntuacion");
            String nombre = "Yo";
            // Mejor leerlo desde un Dialog o una nueva actividad AlertDialog.Builder
            almacen.guardarPuntuacion(puntuacion, nombre, System.currentTimeMillis());
            lanzarPuntuaciones(null);
        }
    }

    //Colocar el menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Si se clica una opcion del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.acercaDe:
                lanzarAcercaDe(null);
                break;
        }
        return true;
    }


    public void lanzarAcercaDe(View view) {
        Intent i = new Intent(this, AcercaDe.class);
        startActivity(i);
    }

    public void lanzarJuego(View view) {
        Intent i = new Intent(this, Juego.class);
        startActivityForResult(i, 1234);
    }
    public void lanzarPreferencias(View view) {
        Intent i = new Intent(this, Preferencias.class);
        startActivity(i);
    }

    public void lanzarPuntuaciones(View view) {
        Intent i = new Intent(this, Puntuaciones.class);
        startActivity(i);
    }

    //Visualizar preferencias
    public void mostrarPreferencias(){
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(this);
        String s = "música: "+ pref.getBoolean("musica",true)
                +", gráficos: " + pref.getString("graficos","?");
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    //Controlar Juegos
    public void onGesturePerformed(GestureOverlayView ov,Gesture gesture) {

        ArrayList<Prediction> predictions=libreria.recognize(gesture);

        if(predictions.size()>0){

            String comando = predictions.get(0).name;

            if(comando.equals("play")){

                lanzarJuego(null);

            } else if(comando.equals("configurar")){

                lanzarPreferencias(null);

            } else if(comando.equals("acerca_de")){

                lanzarAcercaDe(null);

            } else if(comando.equals("cancelar")){

                finish();

            }

        }

    }
}
