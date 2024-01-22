package com.example.basededatos;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MostrarPalabras extends Activity {

    private ArrayList<String> palabras;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.palabras);

        palabras = new ArrayList<>();
        listView = findViewById(R.id.listView);

        // Realizar la solicitud HTTP para obtener los datos de la base de datos
        new GetPalabrasTask().execute();
    }

    private class GetPalabrasTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String url = "http://192.168.1.142/palabras/listar_palabras.php"; // Reemplaza con la URL de tu servidor y tu script PHP
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
                    palabras.add(palabra);
                }

                // Actualizar el ListView
                actualizarListView();
            } else {
                // Manejar el caso en que la respuesta es nula
                Toast.makeText(this, "La respuesta del servidor es nula", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la respuesta JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarListView() {
        // Crear un ArrayAdapter con los datos y establecerlo en el ListView
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, palabras);
        listView.setAdapter(adapter);
    }

    public void salir(View view) {
        finish();
    }
}
