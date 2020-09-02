package com.grosantoine.fr.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Fragment1 extends Fragment {

    ListView lst;
    ArrayList<String> titres;
    ArrayList<Bitmap> images;
    ArrayList<String> ids;

    int deletePos = 0;

    public static final String EXTRA_ID = "com.grosantoine.fr.myapplication.EXTRA_ID";

    CustomListView customListView;

    Boolean flag_loading = false;

    String lastItem = "0";

    TabLayout tabLayout;
    private final int[] titlesInt = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};

    // On instancie les ArrayList
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titres = new ArrayList<String>();
        images = new ArrayList<Bitmap>();
        ids = new ArrayList<String>();
    }


    // On place le layout dans le fragment
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment1_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // On instancie la listView et on lui attribue ses deux listeners
        lst = (ListView) getView().findViewById(R.id.listview);
        lst.setOnScrollListener(scrollListener);
        lst.setOnItemClickListener(clickListener);
        lst.setOnItemLongClickListener(longClickListener);

        // On charge les images et les données
        getJSON("http://ip/req_f.php?id=" + lastItem);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tabLayout = getActivity().findViewById(R.id.tabs);
    }

    // Le listener de clicks sur les images
    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            Intent intent = new Intent(getContext(), Desc.class);

            // On transmet à l'activité l'id de l'image
            intent.putExtra(EXTRA_ID, ids.get(position));

            // On charge la nouvelle activité
            startActivity(intent);
        }
    };

    // Le listener de long clicks sur les images
    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

            deletePos = position;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Suppresion");
            builder.setMessage("Est-ce sûr ?");
            builder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    // Suppresion de l'image de partout
                    deleteImage(ids.get(deletePos));

                    // Mise à jour des titres
                    updateTitles();
                }
            });
            builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.show();

            return true;
        }
    };

    // Le listener de scroll de la listview
    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            // Si l'item affiché est le dernier item de la listView
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {

                if (!flag_loading) {

                    // On empêche le spam de la méthode suivante
                    flag_loading = true;

                    // On appelle la méthode pour charger les nouvelles images
                    populateList("http://ip/req_f.php?id=" + lastItem);
                }
            }
        }
    };

    // Méthode qui met en page la listView
    private void loadIntoListView(Triple triple) {

        customListView = new CustomListView(getActivity(), triple.getFirst(), triple.getSecond(), triple.getThird());
        lst.setAdapter(customListView);
    }

    // Méthode qui télécharge et charge les images et les titres dans la listView
    private void getJSON(final String urlJSON) {

        class GetJSON extends AsyncTask<Void, Void, Triple> {

            @Override
            protected void onPostExecute(Triple triple) {
                super.onPostExecute(triple);

                // On charge les données dans la listView
                loadIntoListView(triple);
            }

            // Téléchargement des images et des titres
            @Override
            protected Triple doInBackground(Void... voids) {

                try {

                    // Création de l'url pour récupérer les titres et ids des 10 premières images
                    URL url = new URL(urlJSON);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    StringBuilder sb = new StringBuilder();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;

                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    json = sb.toString().trim();

                    // Création du tableau JSON
                    JSONArray jsonArray = new JSONArray(json);

                    String id = "";

                    // On parcourt chaque objet du tableau
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject obj = jsonArray.getJSONObject(i);

                        // On ajoute le titre à la liste
                        titres.add(obj.getString("titre"));
                        id = obj.getString("id");

                        // On ajoute l'id à la liste
                        ids.add(id);

                        // On complète l'id avec des 0
                        while (id.length() < 5) {
                            id = "0" + id;
                        }

                        // On récupère l'image correspondant à l'id
                        URL uri = new URL("http://ip/images/" + id + ".jpg");
                        HttpURLConnection con2 = (HttpURLConnection) uri.openConnection();

                        InputStream inputStream = con2.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // On ajoute l'image à la liste
                        images.add(bitmap);
                    }

                    // On sauvegarde le dernier id
                    lastItem = id;

                    // On regroupe les 3 listes en un seul objet
                    Triple triple = new Triple(titres, images, ids);

                    // On retourne les 3 listes
                    return triple;

                } catch (Exception e) {
                    return null;
                }
            }
        }

        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }

    // Méthode qui télécharge et charge les nouvelles images et titres dans la listView
    private void populateList(final String urlJSON) {

        class PopulateList extends AsyncTask<Void, Void, Void> {


            // Téléchargement des nouvelles images et titres
            @Override
            protected Void doInBackground(Void... voids) {

                try {

                    // Création de l'url pour récupérer les titres et les ids de 10 nouvelles images
                    URL url = new URL(urlJSON);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    StringBuilder sb = new StringBuilder();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;

                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    json = sb.toString().trim();

                    // Création du tableau JSON
                    JSONArray jsonArray = new JSONArray(json);

                    String id = "";

                    // On parcourt chaque objet du tableau
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject obj = jsonArray.getJSONObject(i);

                        // On ajoute le titre à la liste
                        titres.add(obj.getString("titre"));
                        id = obj.getString("id");

                        // On ajoute l'id à la liste
                        ids.add(id);

                        // On complète l'id avec des 0
                        while (id.length() < 5) {
                            id = "0" + id;
                        }

                        // On récupère l'image correspondant à l'id
                        URL uri = new URL("http://ip/images/" + id + ".jpg");
                        HttpURLConnection con2 = (HttpURLConnection) uri.openConnection();

                        InputStream inputStream = con2.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // On ajoute l'image à la liste
                        images.add(bitmap);
                    }
                    // On sauvegarde le dernier id
                    lastItem = id;

                    // On spécifie que le chargement des nouvelles images est fini
                    flag_loading = false;

                    // On spécifie que des données ont été rajoutées aux listes
                    customListView.notifyDataSetChanged();

                    return null;

                } catch (Exception e) {
                    return null;
                }
            }
        }

        PopulateList populateList = new PopulateList();
        populateList.execute();
    }

    // Méthode qui supprime une image de la base de données et de la listView
    private void deleteImage(final String id) {

        class DeleteImage extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                // On retire les informations de l'image supprimée de l'ArrayList
                titres.remove(deletePos);
                images.remove(deletePos);
                ids.remove(deletePos);

                // On update la ListView
                customListView.notifyDataSetChanged();
            }

            @Override
            protected Void doInBackground(Void... voids) {

                try {

                    String url = "http://ip/del_d.php?id=" + id;

                    // On charge l'url pour supprimer les données
                    URL uri = new URL(url);
                    Scanner scanner = new Scanner(uri.openStream());

                    return null;

                } catch (Exception e) {
                    return null;
                }
            }
        }

        DeleteImage deleteImage = new DeleteImage();
        deleteImage.execute();
    }

    // Méthode qui met à jour les titres des fragments
    private void updateTitles() {

        class UpdateTitles extends AsyncTask<Void, String, ArrayList<String>> {

            @Override
            protected void onPostExecute(ArrayList<String> titles) {
                super.onPostExecute(titles);

                for(int i = 0; i < 3; i++) {

                    // Mise à jour de chaque titre
                    if(!titles.get(i).equals("0")) {
                        tabLayout.getTabAt(i).setText(getContext().getResources().getString(titlesInt[i]) + " (" + titles.get(i) + ")");
                    }
                }
            }

            @Override
            protected ArrayList<String> doInBackground(Void... voids) {

                try {


                    ArrayList<String> titles = new ArrayList<String>();

                    // On récupère le nombre d'images non traitées
                    URL url = new URL("http://ip/req_nt_ch.php");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

                    String nbr;

                    nbr = bufferedReader.readLine();
                    titles.add(nbr);

                    // On récupère le nombre d'images traitées
                    url = new URL("http://ip/req_np_ch.php");
                    bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

                    nbr = bufferedReader.readLine();
                    titles.add(nbr);

                    // On récupère le nombre d'images publiées
                    url = new URL("http://ip/req_p_ch.php");
                    bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

                    nbr = bufferedReader.readLine();
                    titles.add(nbr);

                    return titles;

                } catch (Exception e) {
                    return null;
                }
            }
        }

        UpdateTitles updateTitles = new UpdateTitles();
        updateTitles.execute();
    }
}
