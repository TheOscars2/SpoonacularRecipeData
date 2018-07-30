package foodapp.spoonacularscraping;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    String jsonString = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONArray recipeResults;

        MyDownloadTask task = (MyDownloadTask) new MyDownloadTask().execute();

        jsonString = task.jsonString;

        JSONObject myObj = null;
        try {
            myObj = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject json = new JSONObject();
        try {
            recipeResults = myObj.getJSONArray("recipes");
            for (int i = 0; i < recipeResults.length(); i++) {
                JSONObject recipe = (JSONObject) recipeResults.get(i);

                json.put("id", i);
                json.put("name", recipe.getString("title"));
                int totalTime = recipe.getInt("readyInMinutes");
                json.put("CookTimeHours", totalTime / 60);
                json.put("CookTimeMinutes", totalTime % 60);
                json.put("sourceName", recipe.getString("sourceName"));
                json.put("sourceLink", "sourceUrl");

                    /*
                        Get and format ingredients to our json style
                     */
                JSONArray ingredients = recipe.getJSONArray("extendedIngredients");
                JSONArray recipeIngredients = new JSONArray();
                for (int j = 0; j < ingredients.length(); j++) {
                    JSONObject ingredient = (JSONObject) ingredients.get(j);
                    JSONObject ingredientObject = new JSONObject();
                    ingredientObject.put("food", ingredient.getString("name"));
                    ingredientObject.put("quantity", ingredient.getDouble("amount") + ingredient.getString("unit"));
                    recipeIngredients.put(ingredientObject);
                }
                json.put("ingredients", recipeIngredients);

                    /*
                        Get and format instructions to our json style
                     */
                JSONArray instructions = new JSONArray();
                instructions.put(recipe.getString("instructions"));
                json.put("instructions", instructions);
            }

            TextView textView = findViewById(R.id.textView);
            textView.setText(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class MyDownloadTask extends AsyncTask {
        public String jsonString;

        @Override
        protected String doInBackground(Object[] objects) {
            HttpURLConnection connection = null;
            try {
                URL u = new URL("https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/random?number=1");
                connection = (HttpURLConnection) u.openConnection();
                connection.setRequestProperty("X-Mashape-Key", "vN4jBdFJ1Smsh760lIrNUKSUES2Up1Jp0cAjsnf7pLatlnoAWK");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("X-Mashape-Host", "spoonacular-recipe-food-nutrition-v1.p.mashape.com");
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.connect();

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());

                BufferedReader br = new BufferedReader(reader);
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    json.append(line + "\n");
                }
                this.jsonString = json.toString();
                br.close();

            } catch (MalformedURLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return jsonString;
        }

        protected void onPreExecute() {
            //display progress dialog.

        }



        protected void onPostExecute(Void result) {
            // dismiss progress dialog and update ui
        }
    }

    /**
     * curl -H "X-Mashape-Key: vN4jBdFJ1Smsh760lIrNUKSUES2Up1Jp0cAjsnf7pLatlnoAWK" -H "X-Mashape-Host: spoonacular-recipe-food-nutrition-v1.p.mashape.com" "https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/random?number=50" > recipes.json
     > recipes.json

     */
}
