package com.android.demo_share_lan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.io.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGetFile;
    EditText userName, pass;
    TextView txtNameFile;

    String TAG = MainActivity.class.getSimpleName();
    String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = findViewById(R.id.edTenDangNhapDN);
        pass = findViewById(R.id.edMatKhauDN);
        btnGetFile = findViewById(R.id.getFile);
        btnGetFile.setOnClickListener(this);
        txtNameFile = findViewById(R.id.txtNameFile);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        Log.e(TAG, "ip: " + ip);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.getFile){
            String user = userName.getText().toString();
            String userPass = pass.getText().toString();
            if (user.isEmpty() || userPass.isEmpty()){
                Toast.makeText(MainActivity.this,"nhập user name or password",Toast.LENGTH_SHORT).show();
            }else {
                new FileSMB(user, userPass).execute();
            }
        }

    }

    private class  FileSMB extends AsyncTask<String,Boolean, String> {

        String user;
        String userPass;

        public FileSMB(String user, String userPass) {
            this.user = user;
            this.userPass = userPass;
        }

        @Override
        protected String doInBackground(String... strings) {
            SMBClient client = new SMBClient();
            String filename = "";
            try {
                Connection connection = client.connect("192.168.0.22");
                AuthenticationContext ac = new AuthenticationContext(user, userPass.toCharArray(), "watermelons-Mac-min");
                Session session = connection.authenticate(ac);

                // Connect to Share
                DiskShare share = (DiskShare) session.connectShare("test_share");
                for (FileIdBothDirectoryInformation f : share.list("", "*.")) {
                    Log.e(TAG, f.getFileName());
                    System.out.println("File : " + f.getFileName());
                    filename = f.getFileName();
                    MainActivity.copyFile(f.getFileId(), filename);
                }
            } catch (Exception e) {
                Log.e("Exception", e.getMessage());
                System.out.println("Exception : " + e.getMessage());
                filename = e.getMessage();
            }
            return filename;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("File", s);
            MainActivity.copyFile(s,s);
            txtNameFile.setText(s);
        }
    }

    public static void copyFile(String src, String dst) {
        InputStream in = null;
        try {
            File file = new File(src);
            in = new FileInputStream(file);
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        }catch (Exception e){

        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyFile( byte[] bytes, String dst) {
        String root = Environment.getExternalStorageDirectory().toString();
        try {
            File file1 = new File(root + File.separator + dst);
            if (!file1.exists()){
                file1.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file1);
            fos.write(bytes);
        }catch (Exception e){
            Log.e("Exception copyFile", e.getMessage());
        }
    }
}