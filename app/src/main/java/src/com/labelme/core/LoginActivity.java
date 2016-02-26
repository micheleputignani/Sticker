package src.com.labelme.core;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import src.com.labelme.R;
import src.com.labelme.helper.JSONParser;
import src.com.labelme.helper.SessionManager;


/**
 * Created by Mirko Putignani on 25/01/2016.
 */
public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    //dati login
    public static final String EMAIL_AUTOLOGIN = "admin@hotmail.com";
    public static final String PASSWORD_AUTOLOGIN = "password";

    private Button btnLogin;
    private TextView btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;

    private static String url_login = "http://androidlabelme.altervista.org/login.php";

    //JSON node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UID = "id";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_NAME = "name";
    private static final String TAG_ARRAY = "user";


    // sessione manager
    private SessionManager session;

    JSONParser jsonParser = new JSONParser();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // session manager
        session = new SessionManager(getApplicationContext());
        session.setLogin(false); //Todo: da eliminare per login automatico
        // check if user is already logged in or not
        if (session.isLoggedIn()) {
            // user is already logged in. Take him to main activity
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLinkToRegister = (TextView) findViewById(R.id.link_signup);

        //DATI TEMPORANEI PER VELOCIZZARE IL LOGIN
        inputEmail.setText(EMAIL_AUTOLOGIN);
        inputPassword.setText(PASSWORD_AUTOLOGIN);

        //Login button Click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        //Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRegister = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(toRegister);
                finish();
            }
        });
    }

    private void loginUser() {
        if (validate()) {
            new getUser().execute();
            return;
        }
    }

    private boolean validate() {
        boolean valid = true;
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("enter a valid email address");
            valid = false;
        } else {
            inputEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            inputPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            inputPassword.setError(null);
        }
        return valid;
    }

    class getUser extends AsyncTask<String, String, String> {
        // prende email e password inseriti dall'utente
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Loggin in...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));
            // getting JSON Object
            // POST method
            JSONObject json = jsonParser.makeHttpRequest(url_login, "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // user successfully logged in
                    // create login session
                    session.setLogin(true);

                    JSONArray jsonArray = json.getJSONArray(TAG_ARRAY);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    // store the user in session manager
                    int id = jsonObject.getInt(TAG_UID);
                    String email = jsonObject.getString(TAG_EMAIL);
                    String name = jsonObject.getString(TAG_NAME);
                    // store user info
                    session.setUser_id(id);
                    session.setUser_email(email);
                    session.setUser_name(name);

                    //intent alla Home page
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    // closing this screen
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //i dati inseriti non sono validi per il login
                            Toast.makeText(LoginActivity.this, "Please enter again your data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after loggin in
            pDialog.dismiss();
        }
    }
}




