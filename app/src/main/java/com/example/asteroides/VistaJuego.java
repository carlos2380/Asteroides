package com.example.asteroides;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.Vector;

/**
 * Created by carlos on 24/06/2016.
 */
public class VistaJuego extends View implements SensorEventListener{


    // //// MULTIMEDIA //////
    SoundPool soundPool;
    int idDisparo, idExplosion;
    // //// THREAD Y TIEMPO //////
    // Thread encargado de procesar el juego
    private ThreadJuego thread = new ThreadJuego();
    // Cada cuanto queremos procesar cambios (ms)
    private static int PERIODO_PROCESO = 50;
    // Cuando se realizó el último proceso
    private long ultimoProceso = 0;
    // //// NAVE //////
    private Grafico nave;// Gráfico de la nave
    private int giroNave; // Incremento de dirección
    private float aceleracionNave; // aumento de velocidad
    private static final int MAX_VELOCIDAD_NAVE = 20;
    // Incremento estándar de giro y aceleración
    private static final int PASO_GIRO_NAVE = 5;
    private static final float PASO_ACELERACION_NAVE = 0.5f;
    // //// ASTEROIDES //////
    private Vector<Grafico> asteroides; // Vector con los Asteroides
    private int numAsteroides= 5; // Número inicial de asteroides
    private int numFragmentos= 3; // Fragmentos en que se divide

    //// ON TOUCH EVENT /////
    private float mX = 0;
    private float mY = 0;
    private boolean disparo = false;
    //------------
    private SharedPreferences pref;
    private Drawable drawableAsteroide[]= new Drawable[3];
    // //// MISIL //////
    private Grafico misil;
    private static int PASO_VELOCIDAD_MISIL = 12;
    private boolean misilActivo = false;
    private int tiempoMisil;
    private Context context;

    private int puntuacion = 0;

    private Activity padre;


    public VistaJuego(Context context, AttributeSet attrs)  {
        super(context, attrs);
        Drawable drawableNave, drawableMisil;

        this.context = context;
        //SOUND INIT
        soundPool = new SoundPool( 5, AudioManager.STREAM_MUSIC , 0);
        idDisparo = soundPool.load(context, R.raw.disparo, 0);
        idExplosion = soundPool.load(context, R.raw.explosion, 0);
        //---------------------
        pref = context.getSharedPreferences("com.example.asteroides_preferences", Context.MODE_PRIVATE);

        if (pref.getString("graficos", "1").equals("0")) {
            //Comprobamos version de android
            desactivarAceleracionHardware();


            //ASTEROIDES
            Path pathAsteroide = new Path();
            pathAsteroide.moveTo((float) 0.3, (float) 0.0);
            pathAsteroide.lineTo((float) 0.6, (float) 0.0);
            pathAsteroide.lineTo((float) 0.6, (float) 0.3);
            pathAsteroide.lineTo((float) 0.8, (float) 0.2);
            pathAsteroide.lineTo((float) 1.0, (float) 0.4);
            pathAsteroide.lineTo((float) 0.8, (float) 0.6);
            pathAsteroide.lineTo((float) 0.9, (float) 0.9);
            pathAsteroide.lineTo((float) 0.8, (float) 1.0);
            pathAsteroide.lineTo((float) 0.4, (float) 1.0);
            pathAsteroide.lineTo((float) 0.0, (float) 0.6);
            pathAsteroide.lineTo((float) 0.0, (float) 0.2);
            pathAsteroide.lineTo((float) 0.3, (float) 0.0);
            for (int i=0; i<3; i++) {
                ShapeDrawable dAsteroide = new ShapeDrawable(new PathShape(
                        pathAsteroide, 1, 1));
                dAsteroide.getPaint().setColor(Color.WHITE);
                dAsteroide.getPaint().setStyle(Paint.Style.STROKE);
                dAsteroide.setIntrinsicWidth(50 - i * 14);
                dAsteroide.setIntrinsicHeight(50 - i * 14);
                drawableAsteroide[i] = dAsteroide;
            }
            setBackgroundColor(Color.BLACK);

            //NAVE
            Path pathNave = new Path();
            pathNave.moveTo((float) 0.0, (float) 0.0);
            pathNave.lineTo((float) 1, (float) 0.5);
            pathNave.lineTo((float) 0, (float) 1);
            pathNave.lineTo((float) 0, (float) 0);
            ShapeDrawable dNave = new ShapeDrawable(new PathShape(pathNave, 1, 1));
            dNave.getPaint().setColor(Color.WHITE);
            dNave.getPaint().setStyle(Paint.Style.STROKE);
            dNave.setIntrinsicWidth(50);
            dNave.setIntrinsicHeight(50);
            drawableNave = dNave;
            setBackgroundColor(Color.BLACK);


            //Misil
            ShapeDrawable dMisil = new ShapeDrawable(new RectShape());
            dMisil.getPaint().setColor(Color.WHITE);
            dMisil.getPaint().setStyle(Paint.Style.STROKE);
            dMisil.setIntrinsicWidth(15);
            dMisil.setIntrinsicHeight(3);
            drawableMisil = dMisil;
        } else {
            drawableAsteroide[0] = context.getResources().
                    getDrawable(R.drawable.asteroide1);
            drawableAsteroide[1] = context.getResources().
                    getDrawable(R.drawable.asteroide2);
            drawableAsteroide[2] = context.getResources().
                    getDrawable(R.drawable.asteroide3);
            drawableNave = context.getResources().getDrawable(R.drawable.nave);
            drawableMisil = context.getResources().getDrawable(R.drawable.misil1);
        }




        nave = new Grafico(this, drawableNave);
        misil = new Grafico(this, drawableMisil);
        asteroides = new Vector();
        for (int i = 0; i < numAsteroides; i++) {
            Grafico asteroide = new Grafico(this, drawableAsteroide[0]);
            asteroide.setIncY(Math.random() * 4 - 2);
            asteroide.setIncX(Math.random() * 4 - 2);
            asteroide.setAngulo((int) (Math.random() * 360));
            asteroide.setRotacion((int) (Math.random() * 8 - 4));
            asteroides.add(asteroide);
        }


    }
    @Override protected void onSizeChanged(int ancho, int alto,
                                           int ancho_anter, int alto_anter) {
        super.onSizeChanged(ancho, alto, ancho_anter, alto_anter);
        // Una vez que conocemos nuestro ancho y alto.
        nave.setPosX(ancho/2);
        nave.setPosY(alto/2);
        for (Grafico asteroide: asteroides) {
            //Para que los asteroides no coincidan con el centro
            do{
                asteroide.setPosX(Math.random()*(ancho-asteroide.getAncho()));
                asteroide.setPosY(Math.random()*(alto-asteroide.getAlto()));
            } while(asteroide.distancia(nave) < (ancho+alto)/5);
        }
        ultimoProceso = System.currentTimeMillis();
        thread.start();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB )
    private void desactivarAceleracionHardware(){
        if (android.os.Build.VERSION.SDK_INT>=11) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        nave.dibujaGrafico(canvas);
        for (Grafico asteroide: asteroides) {
            asteroide.dibujaGrafico(canvas);
        }
        if(misilActivo) {
            misil.dibujaGrafico(canvas);
        }
    }

    public void setPadre(Activity padre) {
        this.padre = padre;
    }

    private void salir() {
        Bundle bundle = new Bundle();
        bundle.putInt("puntuacion", puntuacion);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        padre.setResult(Activity.RESULT_OK, intent);
        padre.finish();
    }



    protected void actualizaFisica(){
        long ahora= System.currentTimeMillis();
        if(ultimoProceso + PERIODO_PROCESO > ahora) {
            return;
        }
        // Para una ejecución en tiempo real calculamos retardo
        double retardo = (ahora - ultimoProceso) / PERIODO_PROCESO;
        ultimoProceso = ahora; // Para la próxima vez
        // Actualizamos velocidad y dirección de la nave a partir de
        // giroNave y aceleracionNave (según la entrada del jugador)
        nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo));
        double nIncX = nave.getIncX() + aceleracionNave *
                Math.cos(Math.toRadians(nave.getAngulo())) * retardo;
        double nIncY = nave.getIncY() + aceleracionNave *
                Math.sin(Math.toRadians(nave.getAngulo())) * retardo;
        // Actualizamos si el módulo de la velocidad no excede el máximo
        if (Math.hypot(nIncX,nIncY) <= MAX_VELOCIDAD_NAVE){
            nave.setIncX(nIncX);
            nave.setIncY(nIncY);
        }
        // Actualizamos posiciones X e Y
        nave.incrementaPos(retardo);
        for (Grafico asteroide : asteroides) {
             asteroide.incrementaPos(retardo);
        }

        // Actualizamos posición de misil
        if (misilActivo) {
            misil.incrementaPos(retardo);
            tiempoMisil-=retardo;
            if (tiempoMisil < 0) {
                misilActivo = false;
            } else {
                for (int i = 0; i < asteroides.size(); i++)
                    if (misil.verificaColision(asteroides.elementAt(i))) {
                        destruyeAsteroide(i);
                        break;
                    }
            }
        }
        for (Grafico asteroide : asteroides) {
            if (asteroide.verificaColision(nave)) {
                salir();
            }
        }
    }

    private void destruyeAsteroide(int i) {
        int tam;
        if(asteroides.get(i).getDrawable()!=drawableAsteroide[2]) {
            if (asteroides.get(i).getDrawable() == drawableAsteroide[1]) {
                tam = 2;
            } else {
                tam = 1;
            }
            for (int n = 0; n < numFragmentos; ++n) {
                Grafico asteroide = new Grafico(this, drawableAsteroide[tam]);
                asteroide.setPosX(asteroides.get(i).getPosX());
                asteroide.setPosY(asteroides.get(i).getPosY());
                asteroide.setIncX(Math.random() * 7 - 2 - tam);
                asteroide.setIncY(Math.random() * 7 - 2 - tam);
                asteroide.setAngulo((int) (Math.random() * 8 - 4));
                asteroides.add(asteroide);
            }
            asteroides.remove(i);
            misilActivo = false;
            soundPool.play(idExplosion, 1, 1, 0, 0, 1);
            puntuacion += 1000;
        }
        if (asteroides.isEmpty()) {
            salir();
        }
    }

    private void activaMisil() {

        misil.setPosX(nave.getPosX());
        misil.setPosY(nave.getPosY());
        misil.setAngulo(nave.getAngulo());
        misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo())) *
                PASO_VELOCIDAD_MISIL);
        misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo())) *
                PASO_VELOCIDAD_MISIL);
        tiempoMisil = (int) Math.min(this.getWidth() / Math.abs( misil.getIncX()), this.getHeight() / Math.abs(misil.getIncY())) - 2;
        misilActivo = true;
        /*
        El método play() permite reproducir una pista. Hay que indicarle el identificador de pista;
        el volumen para el canal izquierdo y derecho (0.0 a 1.0);
        La prioridad; El número de repeticiones (-1= siempre, 0=solo una vez, 1=repetir una vez, …  )
        y el ratio de reproducción, con el que podremos modificar la velocidad o pitch (1.0 reproducción normal, rango: 0.5 a 2.0)
         */
        soundPool.play(idDisparo, 1, 1, 1, 0, 1);
    }




    //-------------------------------------------------------------
    //-------------------------------------------------------------
    //Operaciones de TECLADO
    @Override
    public boolean onKeyDown(int codigoTecla, KeyEvent evento) {
        super.onKeyDown(codigoTecla, evento);
        // Suponemos que vamos a procesar la pulsación
        boolean procesada = true;
        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                aceleracionNave = +PASO_ACELERACION_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                giroNave = -PASO_GIRO_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                giroNave = +PASO_GIRO_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                activaMisil();
                break;
            default:
                // Si estamos aquí, no hay pulsación que nos interese
                procesada = false;
                break;
        }
        return procesada;
    }

    @Override
    public boolean onKeyUp(int codigoTecla, KeyEvent evento) {
        super.onKeyUp(codigoTecla, evento);
        // Suponemos que vamos a procesar la pulsación
        boolean procesada = true;
        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                aceleracionNave = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                giroNave = 0;
                break;
            default:
                // Si estamos aquí, no hay pulsación que nos interese
                procesada = false;
                break;
        }
        return procesada;
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------


    @Override
    public boolean onTouchEvent (MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                disparo = true;
                break;
            case MotionEvent.ACTION_MOVE:

                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if(dy <6 && dx>6) {
                    giroNave = Math.round((x - mX));
                    disparo = false;
                }else  if(dx <6 && dy>6) {
                    aceleracionNave = Math.round((mY -y)/25);
                    if(aceleracionNave < 0) aceleracionNave = 0;
                    disparo = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                giroNave = 0;
                aceleracionNave = 0;
                if(disparo){
                    activaMisil();
                }
                break;
        }
        mX = x;
        mY = y;
        return true;
    }

    //---------------------------------
    //--- SENSORES -----
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    private boolean hayValorInicial = false;
    private float valorInicialY;
    private float valorInicialZ;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(pref.getString("control", "2").equals("1")){
            float valorY = event.values[1];
            float valorZ = event.values[2];
            if (!hayValorInicial){
                valorInicialY = valorY;
                valorInicialZ = valorZ;
                hayValorInicial = true;
            }
            giroNave=(int) (valorY-valorInicialY)/3 ;
        }
    }

    private SensorManager mSensorManager;
    public void activarSensores(){
        //Sensores
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> listSensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (!listSensors.isEmpty()) {
            Sensor orientationSensor = listSensors.get(0);
            mSensorManager.registerListener((SensorEventListener) this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void desactivarSensores(){
        mSensorManager.unregisterListener(this);
    }
    class ThreadJuego extends Thread {
        private boolean pausa,corriendo;

        public synchronized void pausar() {
            pausa = true;
        }

        public synchronized void reanudar() {
            pausa = false;
            notify();
        }

        public void detener() {
            corriendo = false;
            if (pausa) reanudar();
        }

        @Override public void run() {
            corriendo = true;
            while (corriendo) {
                actualizaFisica();
                synchronized (this) {
                    while (pausa)
                        try {
                            wait();
                        } catch (Exception e) {
                        }
                }
            }
        }

    }
    public ThreadJuego getThread() {
        return thread;
    }
}

