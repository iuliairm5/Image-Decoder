package com.example.imagedecoder.irimia.iulia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {


    Button mDownloadBtn;
    ImageView imgview;

    byte[] RSApubkey ;
    byte[] AESenc;
    byte[] ivEnc;
    byte[] imageEnc;
    byte[] imgDec;
    String url_iv_enc="https://student.ism.ase.ro/access/content/group/Y2S1_MAS/AndroidEncrypt/AES_IV_encrypted_with_RSA_PrivateKey";
    String url_aes_key_enc="https://student.ism.ase.ro/access/content/group/Y2S1_MAS/AndroidEncrypt/AES_Key_encrypted_with_RSA_PrivateKey";
    String url_img_enc="https://student.ism.ase.ro/access/content/group/Y2S1_MAS/AndroidEncrypt/Image_encrypted_with_AES";
    String url_rsa_pub_key="https://student.ism.ase.ro/access/content/group/Y2S1_MAS/AndroidEncrypt/RSA_PublicKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 55);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 56);


        final DownloadAsyncTask rsakey = new DownloadAsyncTask() {

            @Override
            protected void onPostExecute(ByteBuffer byteBuffer) {
                //rsakey.execute("https://student.ism.ase.ro/access/content/group/Y2S1_MAS/AndroidEncrypt/RSA_PublicKey");//on background thread
                //the result of do in background execute will end up in the onPostExecute (main thread)
                //the content got from the url is base64 encoded so we need to decode it !!!
                RSApubkey = android.util.Base64.decode(byteBuffer.array(), android.util.Base64.DEFAULT); //done on the main thread

            }
        };

        final DownloadAsyncTask aesenckey = new DownloadAsyncTask() {

            @Override
            protected void onPostExecute(ByteBuffer byteBuffer) {
                rsakey.execute(url_rsa_pub_key);//on background thread
                //the result of do in background execute will end up in the onPostExecute (main thread)
                AESenc = android.util.Base64.decode(byteBuffer.array(), android.util.Base64.DEFAULT); //done on the main thread

            }
        };
        final DownloadAsyncTask ivenckey = new DownloadAsyncTask() {

            @Override
            protected void onPostExecute(ByteBuffer byteBuffer) {
                //imageenc.execute("https://student.ism.ase.ro/access/content/group/Y2S1_MAS/AndroidEncrypt/Image_encrypted_with_AES"); //on background thread
                aesenckey.execute(url_aes_key_enc);
                //the result of do in background execute will end up in the onPostExecute (main thread)
                ivEnc = android.util.Base64.decode(byteBuffer.array(), android.util.Base64.DEFAULT); //done on the main thread

            }
        };
        final DownloadAsyncTaskImage imageenc = new DownloadAsyncTaskImage() {

            @Override
            protected void onPostExecute(String str) {

                //JUST ONE BACKGROUND THREAD (CANNOT HAVE MULTIPLE EXECUTES)
                ivenckey.execute(url_iv_enc);
                // imageenc.execute(); //not initialized

                //the result of do in background execute will end up in the onPostExecute (main thread)
                imageEnc = android.util.Base64.decode(str.getBytes(StandardCharsets.UTF_8), android.util.Base64.DEFAULT); //done on the main thread

            }
        };
        imageenc.execute(url_img_enc); //on background


        mDownloadBtn = findViewById(R.id.button);
        imgview = findViewById(R.id.imageView);
        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    Log.i("MyRSA as byte[]", Arrays.toString(RSApubkey));

                    X509EncodedKeySpec spec = new X509EncodedKeySpec(RSApubkey);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PublicKey rsaPubKey = kf.generatePublic(spec);

                    //Toast.makeText(getApplicationContext(),"Right after getting rsaPubKey",Toast.LENGTH_SHORT).show();
                    Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipherRSA.init(Cipher.DECRYPT_MODE,rsaPubKey);
                    byte[] AESdec = cipherRSA.doFinal(AESenc);
                    byte[] IVdec = cipherRSA.doFinal(ivEnc);


                    Log.i("MyAES enc as byte[]", Arrays.toString(AESenc));
                    Log.i("MyAES enc size in bytes", String.valueOf(AESenc.length));
                    Log.i("MyIV enc as byte[]", Arrays.toString(ivEnc));
                    Log.i("MyIV enc size in bytes", String.valueOf(ivEnc.length));
                    //Toast.makeText(getApplicationContext(),"Right after getting the IV",Toast.LENGTH_SHORT).show();

                    ///////////////////////////////////////////////////////////////////////////////////
                    javax.crypto.spec.SecretKeySpec keyspec = new javax.crypto.spec.SecretKeySpec(AESdec, "AES");
                    //javax.crypto.spec.IvParameterSpec ivspec = new javax.crypto.spec.IvParameterSpec(IVdec);


                    Log.i("MyAESdec as byte[]", Arrays.toString(AESdec));
                    Log.i("MyAESdec size in bytes", String.valueOf(AESdec.length));
                    Log.i("MyIVdec as byte[]", Arrays.toString(IVdec));
                    Log.i("MyIVdec size in bytes", String.valueOf(IVdec.length));



                    Log.i("img enc as byte[]", Arrays.toString(imageEnc));
                    Log.i("img enc size in bytes", String.valueOf(imageEnc.length));
                    Cipher cipherAES = Cipher.getInstance("AES/ECB/NoPadding");
                    //Toast.makeText(getApplicationContext(),"Right after getting cipherAES",Toast.LENGTH_SHORT).show();

                    cipherAES.init(Cipher.DECRYPT_MODE, keyspec);

                    imgDec = cipherAES.doFinal(imageEnc);
                    Log.i("img dec as byte[]", Arrays.toString(imgDec));
                    Log.i("img dec size in bytes", String.valueOf(imgDec.length));
                   // imgview.setImageBitmap(BitmapFactory.decodeByteArray(imgDec, 0, imgDec.length));

                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgDec, 0, imgDec.length);
                    imgview.setImageBitmap(bitmap);

                } catch (InvalidKeySpecException | InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                }


            }
        });
    }



}