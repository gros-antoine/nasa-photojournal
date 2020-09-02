package com.grosantoine.fr.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Desc3 extends AppCompatActivity {

    EditText title;
    EditText target;
    EditText satellite;
    EditText mission;
    EditText spacecraft;
    EditText instrument;
    EditText text;
    EditText ratio;

    ImageView image;

    String id = "";

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.layout_pub);

        title = findViewById(R.id.title);
        target = findViewById(R.id.ettarget);
        satellite = findViewById(R.id.etsatellite);
        mission = findViewById(R.id.etmission);
        spacecraft = findViewById(R.id.etspacecraft);
        instrument = findViewById(R.id.etinstrument);
        text = findViewById(R.id.ettext);
        ratio = findViewById(R.id.etratio);

        image = findViewById(R.id.image);
        image.setClipToOutline(true);
        image.setOnClickListener(imgOnClickListener);

        // On récupère l'id de l'image sur laquelle on a cliqué
        Intent intent = getIntent();
        id = intent.getStringExtra(Fragment1.EXTRA_ID);

        // On charge les données associées à l'image
        getJSON(id);
    }

    // Le listener de click de l'image
    private View.OnClickListener imgOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            String idCor = id;

            while(idCor.length() < 5) {
                idCor = "0" + idCor;
            }

            String urlString = "https://photojournal.jpl.nasa.gov/catalog/PIA" + idCor;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.android.chrome");

            try {
                startActivity(intent);
            } catch(Exception e) {

                intent.setPackage(null);
                startActivity(intent);
            }
        }
    };

    // Méthode qui envoie les nouvelles données de l'image à la BDD
    private void postJSON(final String url) {

        class PostJson extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                // On retourne sur l'activité principale, ce qui la recharge pour enlever
                // l'image qu'on vient de modifier
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

            @Override
            protected Void doInBackground(Void... voids) {

                try {

                    // On charge l'url pour envoyer les données
                    URL uri = new URL(url);
                    Scanner scanner = new Scanner(uri.openStream());

                    return null;

                } catch(Exception e) {
                    return null;
                }
            }
        }

        PostJson postJson = new PostJson();
        postJson.execute();
    }

    // Méthode qui récupère les données sur l'image
    private void getJSON(final String id) {

        class GetJSON extends AsyncTask<Void, Void, Pair<Bitmap, JSONObject>> {

            float ratioInt = 0f;

            @Override
            protected void onPostExecute(Pair<Bitmap, JSONObject> pair) {
                super.onPostExecute(pair);

                // On affecte les données à l'ImageView et aux EditText
                image.setImageBitmap(pair.first);

                try {

                    JSONObject jsonObject = pair.second;

                    String jsonTitle = jsonObject.getString("titre");
                    title.setText(jsonTitle);

                    String jsonTarget = jsonObject.getString("target");
                    target.setText(jsonTarget);

                    String jsonSatellite = jsonObject.getString("satelliteOf");
                    satellite.setText(jsonSatellite);

                    String jsonMission = jsonObject.getString("mission");
                    mission.setText(jsonMission);

                    String jsonSpacecraft = jsonObject.getString("spacecraft");
                    spacecraft.setText(jsonSpacecraft);

                    String jsonInstrument = jsonObject.getString("instrument");
                    instrument.setText(jsonInstrument);

                    String jsonText = jsonObject.getString("text");
                    text.setText(jsonText);

                    ratio.setText(df.format(ratioInt));

                } catch(Exception e) {
                }
            }

            @Override
            protected Pair<Bitmap, JSONObject> doInBackground(Void... voids) {

                try {

                    String iid = id;

                    // On compléte l'id avec des 0
                    while(iid.length() < 5) {
                        iid = "0" + iid;
                    }

                    // Création de l'url pour récupérer l'image
                    URL uri = new URL("http:///images/" + iid + ".jpg");
                    HttpURLConnection con2 = (HttpURLConnection) uri.openConnection();

                    InputStream inputStream = con2.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // On redimensionne l'image et l'ImageView
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    // On calcule le ratio et on l'affecte
                    ratioInt = (float) w / h;

                    float px = 400 * getApplicationContext().getResources().getDisplayMetrics().density;
                    float coeff = 0;

                    if(w > h) {
                        coeff = px/w;
                    }

                    else {
                        coeff = px/h;
                    }

                    int new_w = (int) coeff*w;
                    int new_h = (int) coeff*h;

                    ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                    layoutParams.height = new_h;
                    layoutParams.width = new_w;

                    Bitmap resized = Bitmap.createScaledBitmap(bitmap, new_w, new_h, false);

                    // Création de l'url pour récupérer les données
                    URL url = new URL("http:///req_d.php?id=" + iid);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    StringBuilder sb = new StringBuilder();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;

                    while((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    json = sb.toString().trim();

                    // Création du tableau JSON
                    JSONArray jsonArray = new JSONArray(json);

                    // On récupère le premier objet du tableau
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    return Pair.create(resized, jsonObject);

                } catch (Exception e) {
                    return null;
                }
            }
        }

        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }
}
