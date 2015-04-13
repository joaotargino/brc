package tpanalytics.busroutecreator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends Activity implements OnClickListener {
    private final static String FILE_DIR = "/Rotas/";


    LocalBroadcastManager localBroadcastManager;
    private Handler customHandler = new Handler();
    private LocationManager locationManager;
    private String provider;
    private MyLocationListener mylistener;
    private Criteria criteria;

    private Location localAtual;

    List<Location> rota = new ArrayList<Location>();
    List<String> paradas = new ArrayList<String>();
    List paradasInexistentes = new ArrayList<>();

    Button viagemStart;
    Button addParada;
    Button paradaInexistente;
    Button salvar;
    Button dropbox;

    private TextView GPSAccuraccy;
    private TextView percursoSize;
    private TextView velocidadeAtual;

    private EditText nomeRota;

    private double velocidadeAtualVal;

    private boolean iniciaViagem = false;
    private String horaInicioViagem = "0";
    private String entrada = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viagemStart = (Button) findViewById(R.id.viagemStart);
        addParada = (Button) findViewById(R.id.addParada);
        paradaInexistente = (Button) findViewById(R.id.paradaInexistente);
        salvar = (Button) findViewById(R.id.salvar);
        dropbox = (Button) findViewById(R.id.dropbox);

        velocidadeAtual = (TextView) findViewById(R.id.velocidadeAtual);
        percursoSize = (TextView) findViewById(R.id.tamanhoPercurso);


        nomeRota = (EditText) findViewById(R.id.nomeRota);


        viagemStart.setOnClickListener(this);
        addParada.setOnClickListener(this);
        paradaInexistente.setOnClickListener(this);
        salvar.setOnClickListener(this);
        dropbox.setOnClickListener(this);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // default

        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        provider = locationManager.getBestProvider(criteria, false);

        // the last known location of this provider
        Location location = locationManager.getLastKnownLocation(provider);

        mylistener = new MyLocationListener();

        if (location != null) {
            mylistener.onLocationChanged(location);
        } else {
            // leads to the settings because there is no last known location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // location updates: at least 1 meter and 1secs change
        locationManager.requestLocationUpdates(provider, 10000, 1.0F, mylistener);

        IntentFilter j = new IntentFilter(
                "tpanalytics.busroutecreator.CURRENT_POSITION");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int valor = intent.getExtras().getInt("CURRENT_POSITION");

                    }
                }, j);

    }



    public void onClick(View target) {
        Calendar c = Calendar.getInstance();
        int hora = c.get(Calendar.HOUR);
        int minuto = c.get(Calendar.MINUTE);
        int segundo = c.get(Calendar.SECOND);

        String hr = String.format("%02d", hora);
        String min = String.format("%02d", minuto);
        String sec = String.format("%02d", segundo);

        final String horaDoDia = hr + ":" + min + ":" + sec;
        final String local = localAtual.getLatitude() + "," + localAtual.getLongitude();

        if(target == dropbox){
            final Intent i = new Intent(MainActivity.this, DropboxActivity.class);
            startActivity(i);

            MainActivity.this.finish();
        }
        else if(target == viagemStart){
            iniciaViagem = true;
            horaInicioViagem = hr + ":" + min + ":" + sec;
            viagemStart.setText("Viagem iniciada");
            viagemStart.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
        }

        else if(target == addParada){


            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Confirmar dados da parada");
            String mensagem = "Hora: " + horaDoDia + "\n" + "Rota: " + nomeRota.getText().toString() + "\n" + "Ponto de refêrencia: "  ;

            alert.setMessage(mensagem);

            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alert.setView(input); // uncomment this line

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String refs = input.getText().toString();
                    entrada = horaDoDia+","+local+","+refs;

                    paradas.add(entrada);
                }
            });

            alert.setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });

            alert.show();

        }
        else if(target == paradaInexistente){

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Confirmar dados da parada inexistente");
            String mensagem = "Rota: " + nomeRota.getText().toString() + "\n" ;

            alert.setMessage(mensagem);


            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alert.setView(input); // uncomment this line


            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String refs = input.getText().toString();
                    paradasInexistentes.add(local + "," + refs);
                }
            });

            alert.setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });

            alert.show();
        }
        else if(target == salvar){
            Toast.makeText(this, "Salvando a viagem, calma.",
                    Toast.LENGTH_SHORT).show();
//            FILE_DIR -> getApplicationContext().getFilesDir().getPath() = /data/data/tpanalytics.busroutecreator/files
//            System.out.println(getApplicationContext().getFilesDir().getPath());
            iniciaViagem = false;
            viagemStart.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            viagemStart.setText("Iniciar Viagem");

            if(nomeRota.getText().toString().isEmpty())
                nomeRota.setText("Rota");
            //escrever arquivo
            String filenameParadas = nomeRota.getText().toString() + "-" + horaInicioViagem + "-paradas.txt";
            File fileParadas = new File(getApplicationContext().getFilesDir().getPath(), filenameParadas);
            FileOutputStream outputStreamParadas;

            String filenameInexistentes = nomeRota.getText().toString() + "-" + horaInicioViagem + "-inexistentes.txt";
            File fileInexistentes = new File(getApplicationContext().getFilesDir().getPath(), filenameInexistentes);
            FileOutputStream outputStreamInexistentes;

            String filenameRota = nomeRota.getText().toString() + "-" + horaInicioViagem + "-rota.txt";
            File fileRota = new File(getApplicationContext().getFilesDir().getPath(), filenameRota);
            FileOutputStream outputStreamRota;

            String entradaParadas = "hora,latitude,longitude,referencia" + "\n";
            for(int i = 0; i < paradas.size(); i++){
                entradaParadas += paradas.get(i) + "\n";
            }

            String entradaInexistentes = "latitude,longitude,referencia" + "\n";
            for(int i = 0; i < paradasInexistentes.size(); i++){
                entradaInexistentes += paradasInexistentes.get(i) + "\n";
            }

            String entradaRota = "latitude,longitude,velocidade,altitude" + "\n";
            for(int i = 0; i < rota.size(); i++){
                entradaRota += rota.get(i).getLatitude() +"," +rota.get(i).getLongitude()
                        +"," + String.valueOf((double) (rota.get(i).getSpeed() * 3600) / 1000) +"," +rota.get(i).getAltitude() + "\n";
            }


            try {
                outputStreamParadas = openFileOutput(filenameParadas, MODE_APPEND | MODE_WORLD_READABLE);
                outputStreamParadas.write(entradaParadas.getBytes());
                outputStreamParadas.close();

                outputStreamInexistentes = openFileOutput(filenameInexistentes, MODE_APPEND | MODE_WORLD_READABLE);
                outputStreamInexistentes.write(entradaInexistentes.getBytes());
                outputStreamInexistentes.close();

                outputStreamRota = openFileOutput(filenameRota, MODE_APPEND | MODE_WORLD_READABLE);
                outputStreamRota.write(entradaRota.getBytes());
                outputStreamRota.close();

                Toast.makeText(this, "Viagem salva com sucesso.",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }



//location e distancia
    public double calculaDistancia(List<Location> distancias) {
        double distanceInMeters = 0;
        for (int i = 0; i < distancias.size() - 1; i++) {
            double lat1 = distancias.get(i).getLatitude();
            double lat2 = distancias.get(i + 1).getLatitude();
            double lng1 = distancias.get(i).getLongitude();
            double lng2 = distancias.get(i + 1).getLongitude();

            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            distanceInMeters += Math.round(6371000 * c);

        }
        return distanceInMeters;
    }


    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            // Initialize the location fields
            if(iniciaViagem) {
//                String local = location.getLatitude() + ", " + location.getLongitude();
                rota.add(location);
            }

            localAtual = location;

            String d = String.format("%.2f",
                    calculaDistancia(rota) / 1000);
            percursoSize.setText(d + " km");
            // distancia.setText(String.valueOf((double)calculaDistancia(distancias)/1000));

            velocidadeAtualVal = (double) (location.getSpeed() * 3600) / 1000;
            String v = String.format("%.2f", velocidadeAtualVal);
            velocidadeAtual.setText("" + v + " km/h");

            // mede a acuracia do gps
            GPSAccuraccy = (TextView) findViewById(R.id.accuracy);
            String acc = String.format("%.1f", location.getAccuracy());
            GPSAccuraccy.setText(acc + " m");


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Toast.makeText(PlayerActivity.this,
            // provider + "'s status changed to " + status + "!",
            // Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Toast.makeText(PlayerActivity.this,
            // "Provider " + provider + " enabled!", Toast.LENGTH_SHORT)
            // .show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            // Toast.makeText(PlayerActivity.this,
            // "Provider " + provider + " disabled!", Toast.LENGTH_SHORT)
            // .show();
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }


}
