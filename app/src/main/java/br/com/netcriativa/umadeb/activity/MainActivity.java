package br.com.netcriativa.umadeb.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.com.netcriativa.umadeb.R;
import br.com.netcriativa.umadeb.fragment.AgendaGeralFragment;
import br.com.netcriativa.umadeb.fragment.IntegrantesFragment;
import br.com.netcriativa.umadeb.fragment.MainFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "AndroidBash";
    private Firebase myFirebaseRef;
    private FirebaseAuth mAuth;
    private TextView name;
    private TextView welcomeText;
    private Button changeButton;
    private Button revertButton;
    // To hold Facebook profile picture
    private ImageView profilePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Seta a toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        // Cria uma referência para seu banco de dados Firebase
        // Adicione seu URL de referência do Firebase em vez do seguinte URL
        myFirebaseRef = new Firebase("https://aplicativoumadeb.firebaseio.com/users");
        mAuth = FirebaseAuth.getInstance();


        //Inicia o fragment1 como padrão
        if (savedInstanceState == null) {
            Fragment f = MainFragment.newInstance("fragment1");
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();
        }


        //Botão de Compartilhamento com Social Media
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "UMADEB Play, O aplicativo do maior congresso de Jovens Pentecostais do Brasil agora na sua mão e por onde for! https://play.google.com/store/apps/details?id=com.adeb&hl=pt_BR";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Umadeb Play, interatividade e informação.");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Compartilhar APP"));
            }
        });

        //Navigation Drawer Ativo nos fragments
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        name = (TextView) findViewById(R.id.txt_nome_usuario);
        welcomeText = (TextView) findViewById(R.id.text_view_welcome);
        changeButton = (Button) findViewById(R.id.button_change);
        revertButton = (Button) findViewById(R.id.button_revert);
        profilePicture = (ImageView) findViewById(R.id.image_view_perfil);

        //Get the uid for the currently logged in User from intent data passed to this activity
        String uid = getIntent().getExtras().getString("user_id");

        //Get the imageUrl  for the currently logged in User from intent data passed to this activity
        String imageUrl = getIntent().getExtras().getString("profile_picture");

        new ImageLoadTask(imageUrl, profilePicture).execute();

        // Referindo-se ao nome do Usuário que fez logon no momento e adicionando um valorChangeListener
        myFirebaseRef.child(uid).child("name").addValueEventListener(new ValueEventListener() {

            //onDataChange is called every time the name of the User changes in your Firebase Database
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Inside onDataChange we can get the data as an Object from the dataSnapshot
                //getValue returns an Object. We can specify the type by passing the type expected as a parameter
                String data = dataSnapshot.getValue(String.class);
                name.setText("Hello " + data + ", ");
            }

            //onCancelled is called in case of any error
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(), "" + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        //A firebase reference to the welcomeText can be created in following ways :
        // You can use this :
        //Firebase myAnotherFirebaseRefForWelcomeText=new Firebase("https://androidbashfirebaseupdat-bd094.firebaseio.com/welcomeText");*/
        //OR as shown below
        myFirebaseRef.child("welcomeText").addValueEventListener(new ValueEventListener() {
            //onDataChange is called every time the data changes in your Firebase Database
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Inside onDataChange we can get the data as an Object from the dataSnapshot
                //getValue returns an Object. We can specify the type by passing the type expected as a parameter
                String data = dataSnapshot.getValue(String.class);
                welcomeText.setText(data);
            }

            //onCancelled is called in case of any error
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(), "" + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        //onClicking changeButton the value of the welcomeText in the Firebase database gets changed
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myFirebaseRef.child("welcomeText").setValue("Android App Development @ AndroidBash");
            }
        });

        //onClicking revertButton the value of the welcomeText in the Firebase database gets changed
        revertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myFirebaseRef.child("welcomeText").setValue("Welcome to Learning @ AndroidBash");
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.btn_quem_somos) {

            AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
            alerta.setTitle("UMADEB Play");
            alerta.setIcon(R.mipmap.ic_umadeb_app_brasilia);
            alerta.setMessage("UNIÃO DE MOCIDADES DA ASSEMBLEIA DEUS DE BRASÍLIA");
            alerta.setCancelable(true);

            AlertDialog alertDialog = alerta.create();
            alertDialog.show();

            return true;
        } else if (id == R.id.action_logout){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.umadeb_2018) {

            setTitle(R.string.app_name);
            Fragment f = MainFragment.newInstance("fragment1");
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();

        } else if (id == R.id.integrantes) {

            setTitle("Integrantes");
            Fragment f = IntegrantesFragment.newInstance("fragment2");
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();

        } else if (id == R.id.agenda_geral) {

            //setTitle("Agenda Geral");
           // Fragment f = AgendaGeralFragment.newInstance("AgendaGeralFragment");
           // getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();

            // Intent intent = new Intent(this, AgendaGeralActivity.class);
            //startActivity(intent);

        } else if (id == R.id.btn_album) {

            Intent intent = new Intent(this, AlbumActivity.class);
            startActivity(intent);

        } else if (id == R.id.logout_btn) {
            mAuth.signOut();
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }
}