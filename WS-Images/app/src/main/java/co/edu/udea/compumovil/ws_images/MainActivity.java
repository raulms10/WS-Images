package co.edu.udea.compumovil.ws_images;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
///import android.support.annotation.RequiresAp;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

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
    private EditText txtId;
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
        txtId = (EditText) findViewById(R.id.txt_id);
        txtNombre = (EditText) findViewById(R.id.txt_nombre);
        txtApellidos = (EditText) findViewById(R.id.txt_apellido);
        txtGenero = (EditText) findViewById(R.id.txt_genero);
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



    @TargetApi(Build.VERSION_CODES.M)
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
        String id_Student = txtId.getText().toString();
        if ("".equals(id_Student)){
            Toast.makeText(this, "Ingrese un Id", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "http://192.168.0.34:3000/api/Students/"+id_Student;
        final String url_container = "http://192.168.0.34:3000/api/Containers/all/download/";
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
                                .load(url_container+student.getId()+student.getPhoto())
                                .into(img_mostrar);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error consultando información", Toast.LENGTH_SHORT).show();
                        Log.d("nada2",error.getMessage());
                    }
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void agregarEstudiante() {
        String url = "http://192.168.0.34:3000/api/Students";


        //url = "http://httpbin.org/post";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        lblApellido.setText(response);
                        //Suponiendo que salga todo bien
                        Student student = new Gson().fromJson(response, Student.class);
                        String url = "http://192.168.0.34:3000/api/Containers/all/upload";
                        String nombre = student.getId()+student.getPhoto();
                        sendImage(url,nombre);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error al crear el Student", Toast.LENGTH_SHORT).show();
                        Log.d("nada",error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("firstname", txtNombre.getText().toString());
                params.put("lastname", txtApellidos.getText().toString());
                params.put("gender", txtGenero.getText().toString());
                params.put("photo", "img.jpg");

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(postRequest);
        //url = "http://192.168.0.34:3000/api/Containers/all/upload";
        //sendImage(url,nombre);



    }

    private void sendImage(String url, final String nameImage) {

        //String url = "https://www.fusemobiledevelopment.com/AlertZone/Services/api/v1/user/profilepicture";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                lblNombre.setText(resultResponse);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error subiendo la imagen", Toast.LENGTH_SHORT).show();
                Log.d("nada3", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
//                params.put("api_token", "gh659gjhvdyudo973823tt9gvjf7i6ric75r76");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("image", new DataPart(nameImage, imagenSeleccionada, "image/jpeg"));
                //params.put("cover", new DataPart("file_cover.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mCoverImage.getDrawable()), "image/jpeg"));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("SessionId", mSessionId);
                return headers;
            }
        };
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
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
