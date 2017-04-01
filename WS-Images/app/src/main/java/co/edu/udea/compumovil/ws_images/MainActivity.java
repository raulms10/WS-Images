package co.edu.udea.compumovil.ws_images;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_GALERIA = 100;
    private final int MY_PERMISSIONS_INTERNET = 150;
    private final int OPEN_GALERIA = 200;

    private final String STR_PERMITIDO = "PERMITIDO";
    private final String STR_DENEGADO = "DENEGADO";
    private final String STR_GALERIA = "Galeria";
    private final String STR_INTERNET = "Internet";
    private final String STR_ESTADO = "El permiso de"; //Alamacenará "Estado del permiso:"

    /*private Button btn_consultar;
    private Button btn_selecc_imagen;
    private Button btn_agregar;*/
    private EditText txtNombre;
    private EditText txtApellidos;
    private EditText txtGenero;
    private TextView lblId;
    private TextView lblNombre;
    private TextView lblApellido;
    private TextView lblGenero;
    private ImageView img_agregar;
    private ImageView img_mostrar;

    private byte[] imagenSeleccionada = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*btn_consultar = (Button) findViewById(R.id.btn_cunsultar);
        btn_selecc_imagen = (Button) findViewById(R.id.btn_seleccionar);
        btn_agregar = (Button) findViewById(R.id.btn_agregar);*/
        lblId = (TextView) findViewById(R.id.lbl_id);
        lblNombre = (TextView) findViewById(R.id.lbl_nombre);
        lblApellido = (TextView) findViewById(R.id.lbl_apellido);
        lblGenero = (TextView) findViewById(R.id.lbl_genero);
        img_agregar = (ImageView) findViewById(R.id.img_agregar);
        img_mostrar = (ImageView) findViewById(R.id.img_mostrar);

        if(verificarPermisoInternet() || verificarPermisoGaleria()) //verificamos los permisos y actualizamos el Textview del estado
            Toast.makeText(this, STR_ESTADO+" "+STR_PERMITIDO, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, STR_ESTADO+" "+STR_DENEGADO, Toast.LENGTH_SHORT).show();

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickButton(View v){
        switch (v.getId()){
            case R.id.btn_cunsultar:
                if(verificarPermisoInternet()) { //verificar los permisos
                    obtenerEstudiante();
                    Toast.makeText(this,"Consultando",Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); //Creamos un Intent para abrir los contactos
                    //startActivityForResult(intent, OPEN_CONTACT); //Lanzamos el Intent y se espera a que se seleccione un contacto
                }else
                    requestPermissions(new String[]{INTERNET}, MY_PERMISSIONS_INTERNET); //Solicitamos los permisos para abrir los contactos

                break;
            case R.id.btn_seleccionar:
                if(verificarPermisoGaleria()) { //verificar los permisos
                    seleccionarImagen();
                    //Toast.makeText(this,"Selecionando",Toast.LENGTH_SHORT).show();
                }else
                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_GALERIA); //Solicitamos los permisos para abrir los contactos

                break;
            case R.id.btn_agregar:
                if(verificarPermisoInternet()) { //verificar los permisos
                    agregarEstudiante();
                    Toast.makeText(this, "Agreando", Toast.LENGTH_SHORT).show();
                }else
                    requestPermissions(new String[]{INTERNET}, MY_PERMISSIONS_INTERNET); //Solicitamos los permisos para abrir los contactos

        break;
        }

    }

    private void obtenerEstudiante() {
        String url = "http://studentapp-mraulio10785903.codeanyapp.com:3000/api/Students/1";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response){
                            Student student = new Gson().fromJson(response.toString(), Student.class);

                            lblId.setText(""+student.getId());
                            lblNombre.setText(student.getFirstname());
                            lblApellido.setText(student.getLastname());

                            Glide.with(MainActivity.this)
                                .load(student.getPhoto())
                                .into(img_mostrar);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error consultando información", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void agregarEstudiante() {

    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), OPEN_GALERIA);
    }

    public boolean verificarPermisoInternet(){
        /* Comprobar que la versión del dispositivo si sea la que admite los permisos en tiempo de
        *  de ejecución, es decir, de la versión de Android 6.0 o superior porque para versiones
        *  anteriores basta con colocar el permiso el el archivo manifiesto
        * */
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        /* Aqui es donde comprobamos que los permisos ya hayan sido aceptdos por el usuario */
        if(checkSelfPermission(INTERNET) == PackageManager.PERMISSION_GRANTED)
            return true;

        return false; //Los permisos no han sido aceptados por el usuario
    }

    public boolean verificarPermisoGaleria(){
        /* Comprobar que la versión del dispositivo si sea la que admite los permisos en tiempo de
        *  de ejecución, es decir, de la versión de Android 6.0 o superior porque para versiones
        *  anteriores basta con colocar el permiso el el archivo manifiesto
        * */
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        /* Aqui es donde comprobamos que los permisos ya hayan sido aceptdos por el usuario */
        if(checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true;

        return false; //Los permisos no han sido aceptados por el usuario
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS_INTERNET){ //Este es el código que hemos ingresado en el método requestPermisions
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ /*Verificar que si se haya aceptado el permiso*/
                Toast.makeText(this, STR_ESTADO+" "+STR_INTERNET+" "+STR_PERMITIDO, Toast.LENGTH_SHORT).show();
                //obtenerEstudiante();
            }else{
                Toast.makeText(this, STR_ESTADO+" "+STR_INTERNET+" "+STR_DENEGADO, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "Vuelva a pulsar el botón", Toast.LENGTH_SHORT).show();
        }

        if(requestCode == MY_PERMISSIONS_GALERIA){ //Este es el código que hemos ingresado en el método requestPermisions
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ /*Verificar que si se haya aceptado el permiso*/
                Toast.makeText(this, STR_ESTADO+" "+STR_GALERIA+" "+STR_PERMITIDO, Toast.LENGTH_SHORT).show();
                //seleccionarFoto();
            }else{
                Toast.makeText(this, STR_ESTADO+" "+STR_GALERIA+" "+STR_DENEGADO, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "Vuelva a pulsar el botón", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case OPEN_GALERIA:
                    Uri path_image = data.getData();
                    img_agregar.setImageURI(path_image);
                    Bitmap bitM = ((BitmapDrawable)img_agregar.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitM.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    imagenSeleccionada = stream.toByteArray();
                    img_agregar.setImageBitmap(bitM);

                    break;
            }

    }



}


}
