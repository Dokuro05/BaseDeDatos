package com.example.basededatos;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText user;
    EditText pass;
    Button validar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = findViewById(R.id.editUsuario);
        pass = findViewById(R.id.editContrasena);
        validar = findViewById(R.id.btnValidar);

        validar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = user.getText().toString();
                String contrasena = pass.getText().toString();

                // Ejecutar AsyncTask para realizar la conexión en segundo plano
                new PostAsyncTask().execute(usuario, contrasena);
            }
        });
    }

    class PostAsyncTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {
            String usuario = params[0];
            String contrasena = params[1];

            Post post = new Post();
            return post.getServerData(usuario, contrasena, "http://192.168.16.15/palabras/tabla_usuarios.php");
        }

        @Override
        protected void onPostExecute(JSONArray datos) {
            // Procesar los datos después de la ejecución en segundo plano
            if (datos != null && datos.length() > 0) {
                try {
                    JSONObject json_data = datos.getJSONObject(0);
                    int numRegistrados = json_data.getInt("id");
                    if (numRegistrados > 0) {
                        Intent intent = new Intent(MainActivity.this, Ahorcado.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getBaseContext(),
                                        "Usuario incorrecto. ", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getBaseContext(),
                                "Datos nulos o vacíos. ", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    class Post {
        private static final int READ_TIMEOUT = 10000;
        private static final int CONNECTION_TIMEOUT = 15000;

        private JSONArray getJsonArray(String respuesta) {
            JSONArray jArray = null;
            try {
                jArray = new JSONArray(respuesta);
            } catch (Exception e) {
                Log.e("log_tag", "Error converting result " + e.toString());
            }
            return jArray;
        }

        private String conectaPost(String usuario, String contrasena, String URL) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("Usuario", usuario)
                        .appendQueryParameter("Contrasena", contrasena);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                int response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    return result.toString();
                } else {
                    return "unsuccessful";
                }

            } catch (IOException e1) {
                e1.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        public JSONArray getServerData(String usuario, String contrasena, String URL) {
            String respuesta = conectaPost(usuario, contrasena, URL);
            if (respuesta != null && !respuesta.trim().isEmpty()) {
                return getJsonArray(respuesta);
            } else {
                return null;
            }
        }
    }
}