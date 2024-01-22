package com.example.basededatos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class SQLite extends Activity {

    Button botonInsertar, botonListar, botonBorrar, botonModificar;
    EditText id, palabra, descripcion;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sqlite);

        botonInsertar=(Button)findViewById(R.id.botonAñadir);
        botonListar=(Button) findViewById(R.id.botonListar);
        botonBorrar=(Button) findViewById(R.id.botonBorrar);
        botonModificar=(Button) findViewById(R.id.botonModificar);

        id=(EditText) findViewById(R.id.textoid);
        palabra=(EditText) findViewById(R.id.textopalabra);
        descripcion=(EditText) findViewById(R.id.textodescripcion);

        String idText=id.getText().toString();


        botonInsertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String palabraText = palabra.getText().toString();
                String descripcionText = descripcion.getText().toString();

                if (!palabraText.isEmpty() && !descripcionText.isEmpty()) {
                    anadirPalabra(palabraText, descripcionText, "http://192.168.1.142/palabras/anadir_palabra.php");
                } else {
                    Toast.makeText(getApplicationContext(), "Faltan datos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        botonBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idText = id.getText().toString();

                if (!idText.isEmpty()) {
                    borrarPalabra(idText, "http://192.168.1.142/palabras/borrar_palabra.php");
                } else {
                    Toast.makeText(getApplicationContext(), "Falta el ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        botonModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idText = id.getText().toString();
                String palabraText = palabra.getText().toString();
                String descripcionText = descripcion.getText().toString();

                if (!idText.isEmpty() && !palabraText.isEmpty() && !descripcionText.isEmpty()) {
                    modificarPalabra(palabraText, descripcionText, idText, "http://192.168.1.142/palabras/modificar_palabra.php");
                } else {
                    Toast.makeText(getApplicationContext(), "Faltan datos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        botonListar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SQLite.this, MostrarPalabras.class);
                startActivity(intent);
            }
        });

    }

    // Método para añadir palabra
    private void anadirPalabra(String palabra, String descripcion, String url) {
        PostAsyncTask task = new PostAsyncTask(getApplicationContext(), url);
        task.execute(palabra, descripcion);
    }

    private void borrarPalabra(String id, String url) {
        PostAsyncTask task = new PostAsyncTask(getApplicationContext(), url);
        task.execute(id);
    }

    private void modificarPalabra(String palabra, String descripcion, String id, String url) {
        PostAsyncTask task = new PostAsyncTask(getApplicationContext(), url);
        task.execute(id, palabra, descripcion);
    }

}

class PostAsyncTask extends AsyncTask<String, Void, JSONArray> {

    private Context mContext;
    private String mUrl; // Nueva variable para almacenar la URL

    public PostAsyncTask(Context context, String url) {
        mContext = context;
        mUrl = url;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        Post post = new Post();
        return post.getServerData(params, mUrl);
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        // Procesar los datos después de la ejecución en segundo plano
        if (jsonArray != null && jsonArray.length() > 0) {
            try {
                // Supongamos que tu respuesta siempre tiene un solo objeto JSON
                JSONObject jsonResult = jsonArray.getJSONObject(0);
                String message = jsonResult.getString("message");

                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "Datos nulos o vacíos.", Toast.LENGTH_SHORT).show();
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

    private String conectaPost(String URL, String... params) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder();

            // Agregar parámetros según el botón pulsado
            switch (URL) {
                case "http://192.168.1.142/palabras/anadir_palabra.php":
                    builder.appendQueryParameter("palabra", params[0]);
                    builder.appendQueryParameter("descripcion", params[1]);
                    break;
                case "http://192.168.1.142/palabras/borrar_palabra.php":
                    builder.appendQueryParameter("id", params[0]);
                    break;
                case "http://192.168.1.142/palabras/modificar_palabra.php":
                    builder.appendQueryParameter("id", params[0]);
                    builder.appendQueryParameter("palabra", params[1]);
                    builder.appendQueryParameter("descripcion", params[2]);
                    break;
            }

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
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public JSONArray getServerData(String[] params, String URL) {
        String respuesta = conectaPost(URL, params);
        if (respuesta != null && !respuesta.trim().isEmpty()) {
            Log.d("Post", "Respuesta: " + respuesta);

            // Intenta crear un JSONArray
            try {
                return new JSONArray(respuesta);
            } catch (JSONException e) {
                // Si no se puede crear un JSONArray, intenta crear un JSONObject
                try {
                    // Supongamos que tu respuesta es un objeto JSON
                    return new JSONArray("[" + respuesta + "]");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        } else {
            return null;
        }
    }
}