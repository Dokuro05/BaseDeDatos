package com.example.basededatos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class Partida{

    String palabra;
    int vidas;

    int aleatorio;

    StringBuilder pista;

    public Partida() {
        pista=new StringBuilder("");
    }

    public StringBuilder getPista() {
        return pista;
    }

    public void setPista(StringBuilder pista) {
        this.pista = pista;
    }

    public String getPalabra() {
        return palabra;
    }

    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }

    public int getVidas() {
        return vidas;
    }

    public void setVidas(int vidas) {
        this.vidas = vidas;
    }

    public StringBuilder nuevaPalabra(ArrayList<String> palabras){
        vidas=5;
        pista.setLength(0);
        aleatorio=(int) (Math.random()*palabras.size());
        palabra=palabras.get(aleatorio);
        for(int j=0;j<palabra.length();j++){
            pista.append("_");
        }
        return pista;
    }

    public boolean comprobarLetra(boolean encontrado, String letra){

        for(int i=0;i<palabra.length();i++){
            String coincidencia=Character.toString(palabra.charAt(i));
            if(letra.equals(coincidencia)){
                encontrado=true;
                pista.setCharAt(i,letra.charAt(0));
            }
        }
        return encontrado;
    }

}

public class Ahorcado extends AppCompatActivity{

    ArrayList<String> palabras;
    //{"memoria", "oceano", "montaña", "carro"};
    ArrayList<String> definiciones;
    String palabraGenerada="";
    StringBuilder pista;
    int contador;
    private ListView listView;
    Partida partida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ahorcado);

        setSupportActionBar(findViewById(R.id.toolbar));

        palabras=new ArrayList<>();
        definiciones=new ArrayList<>();

        new GetPalabrasTask().execute();

        partida=new Partida();

    }

    @Override public boolean onCreateOptionsMenu(Menu mimenu) {
        getMenuInflater().inflate(R.menu.menu, mimenu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem opcion_menu){
        int id=opcion_menu.getItemId();
        if(id==R.id.verpalabra){

            if(palabraGenerada!=""){
                Toast.makeText(this, palabraGenerada, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "No hay ninguna palabra", Toast.LENGTH_SHORT).show();
            }

        }
        if(id==R.id.SQL){
            Intent intent = new Intent(Ahorcado.this, SQLite.class);
            startActivity(intent);
        }
        if(id==R.id.salir){
            Intent intent=new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(opcion_menu);
    }

    public void generarPalabra(View view){

        TextView mostrar = (TextView) findViewById(R.id.palabra);
        mostrar.setText(" ");
        pista=partida.nuevaPalabra(palabras);
        palabraGenerada=partida.getPalabra();

        mostrar.setText(pista);
        contador=partida.getVidas();
        TextView vidas=(TextView) findViewById(R.id.intentos);
        vidas.setText("Intentos: " + contador);
    }

    public void adivinarLetra(View view){
        boolean encontrado=false;
        EditText letraIntroducida=(EditText) findViewById(R.id.letra);
        String letra= letraIntroducida.getText().toString();

        encontrado=partida.comprobarLetra(encontrado, letra);
        pista=partida.getPista();

        TextView mostrar = (TextView) findViewById(R.id.palabra);
        mostrar.setText(pista);

        if (encontrado==false){
            contador--;
            TextView vidas=(TextView) findViewById(R.id.intentos);
            vidas.setText("Intentos: " + contador);
        }
        encontrado=false;
        if(contador==0){
            derrota(view);
        }

        String comprobar= String.valueOf(pista);
        if(!comprobar.contains("_")){
            victoria(view);
            mostrar.setText(definiciones.get(partida.aleatorio));
        }
    }

    public AlertDialog derrota(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Derrota")
                .setMessage("¡Has perdido!")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        builder.show();
        return builder.create();

    }

    public AlertDialog victoria(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Victoria")
                .setMessage("¡Has ganado!")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        builder.show();
        return builder.create();

    }

    public AlertDialog agregarPalabra(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.alertdialog, null);
        builder.setView(dialogView)
                .setTitle("Añade una palabra")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText palabraResultado = dialogView.findViewById(R.id.palabranueva);
                        String palabranueva = palabraResultado.getText().toString();
                        EditText definicionResultado = dialogView.findViewById(R.id.definicionnueva);
                        String definicionnueva = palabraResultado.getText().toString();
                        palabras.add(palabranueva);
                        Palabra nueva=new Palabra(palabranueva, definicionnueva);
                    }
                });

        builder.show();
        return builder.create();
    }

    private class GetPalabrasTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String url = "http://192.168.16.15/palabras/listar_palabras.php"; // Reemplaza con la URL de tu servidor y tu script PHP
            try {
                return obtenerDatos(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            // Procesar la respuesta JSON y actualizar el ListView
            procesarRespuestaJSON(response);
        }
    }

    private String obtenerDatos(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            // Obtener la respuesta del servidor
            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();

        } finally {
            // Cerrar la conexión y el BufferedReader
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void procesarRespuestaJSON(String response) {
        try {
            if (response != null) {
                // Convertir la respuesta JSON en un objeto JSON
                JSONObject json = new JSONObject(response);

                // Obtener el array "palabras" del objeto JSON
                JSONArray palabrasArray = json.getJSONArray("palabras");

                // Iterar a través del array y agregar las palabras al ArrayList
                for (int i = 0; i < palabrasArray.length(); i++) {
                    JSONObject palabraObj = palabrasArray.getJSONObject(i);
                    String palabra = palabraObj.getString("palabra");
                    String definicion = palabraObj.getString("definicion");
                    palabras.add(palabra);
                    definiciones.add(definicion);
                }
            } else {
                // Manejar el caso en que la respuesta es nula
                Toast.makeText(this, "La respuesta del servidor es nula", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la respuesta JSON", Toast.LENGTH_SHORT).show();
        }
    }

}
