package tpanalytics.busroutecreator;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class DropboxActivity extends Activity implements OnClickListener {
    private DropboxAPI<AndroidAuthSession> dropbox;
    private final static String FILE_DIR = "/Rotas/";
//    private final static String FILE_DIR = "/data/data/tpanalytics.busroutecreator/files/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "kyfyuricrfxvfmu";
    private final static String ACCESS_SECRET = "k7ws2ywrbtwl81d";
    private boolean isLoggedIn;
    private Button logIn;
    private Button uploadFile;
    private Button listFiles;
    private Button removeFiles;
    private LinearLayout container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        logIn = (Button) findViewById(R.id.dropbox_login);
        logIn.setOnClickListener(this);
        uploadFile = (Button) findViewById(R.id.upload_file);
        uploadFile.setOnClickListener(this);
        listFiles = (Button) findViewById(R.id.list_files);
        listFiles.setOnClickListener(this);
        removeFiles = (Button) findViewById(R.id.remove);
        removeFiles.setOnClickListener(this);
        container = (LinearLayout) findViewById(R.id.container_files);

        loggedIn(false);
        AndroidAuthSession session;
        AppKeyPair pair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER, token);
        } else {
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER);
        }
        dropbox = new DropboxAPI<AndroidAuthSession>(session);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = dropbox.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();
                loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Erro de autenticação no Dropbox",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        uploadFile.setEnabled(isLogged);
        listFiles.setEnabled(isLogged);
        logIn.setText(isLogged ? "Logout" : "Login");
    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ArrayList<String> result = msg.getData().getStringArrayList("data");
            for (String fileName : result) {
                Log.i("ListFiles", fileName);
                TextView tv = new TextView(DropboxActivity.this);
                tv.setText(fileName);
                container.addView(tv);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dropbox_login:
                if (isLoggedIn) {
                    dropbox.getSession().unlink();
                    loggedIn(false);
                } else {
                    dropbox.getSession().startAuthentication(DropboxActivity.this);
                }

                break;
            case R.id.list_files:
                ListDropboxFiles list = new ListDropboxFiles(dropbox, FILE_DIR,
                        handler);
                list.execute();
                break;
            case R.id.upload_file:
                Toast.makeText(this, "Enviando arquivos para o dropbox!",
                        Toast.LENGTH_LONG).show();
                UploadFileToDropbox upload = new UploadFileToDropbox(this, dropbox,
                        FILE_DIR);
                upload.execute();
                break;
            case R.id.remove:

                try{
                    String filespath = this.getApplicationContext().getFilesDir().getPath();

                    File f = new File(filespath);
                    File file[] = f.listFiles();
                    System.out.println("Size: "+ file.length);
                    for (int i=0; i < file.length; i++) {
                        boolean deleted = file[i].delete();
                }
                    Toast.makeText(this, "Arquivos removidos com sucesso.",
                            Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed(){
        final Intent i = new Intent(DropboxActivity.this, MainActivity.class);
        startActivity(i);

        DropboxActivity.this.finish();
    }
}