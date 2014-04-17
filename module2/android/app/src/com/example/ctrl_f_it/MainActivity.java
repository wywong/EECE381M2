package com.example.ctrl_f_it;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static String IPADDRESS;
    public static int PORT;
    public static Socket sock = null;
    private Button btnConnect;
    private Button btnDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnConnect = (Button) findViewById(R.id.button_connect);
        btnDisconnect = (Button) findViewById(R.id.button_disconnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                openSocket();
            }

        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                closeSocket();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_camera:
                openCamera();
                return true;
            case R.id.action_editor:
                openEditor();
                return true;
            case R.id.action_help:
                openHelp();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_processing:
                openGridView();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openCamera() {
        Intent intent = new Intent(this, camActivity.class);
        startActivity(intent);
    }

    public void openEditor() {
        Intent intent =  new Intent(this, TextListActivity.class);
        startActivity(intent);
    }

    public void openHelp(){
        Intent intent =  new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void openSettings() {
        Toast t = Toast.makeText(getApplicationContext(), "I would now call the settings function", Toast.LENGTH_LONG);
        t.show();
    }

    public void openProcessing() {
        Intent intent = new Intent(this, ProcessingActivity.class);
        startActivity(intent);
    }

    public void openGridView(){
        Intent intent = new Intent(this, GridViewActivity.class);
        startActivity(intent);
    }

    // Construct an IP address from the four boxes
     public String getConnectToIP() {
         String addr = "";
         EditText text_ip;
         text_ip = (EditText) findViewById(R.id.ip1);
         addr += text_ip.getText().toString();
         text_ip = (EditText) findViewById(R.id.ip2);
         addr += "." + text_ip.getText().toString();
         text_ip = (EditText) findViewById(R.id.ip3);
         addr += "." + text_ip.getText().toString();
         text_ip = (EditText) findViewById(R.id.ip4);
         addr += "." + text_ip.getText().toString();
         return addr;
     }

     // Gets the Port from the appropriate field.
     public Integer getConnectToPort() {
         Integer port;
         EditText text_port;

         text_port = (EditText) findViewById(R.id.port);
         port = Integer.parseInt(text_port.getText().toString());

         return port;
     }

     public void openSocket() {
        //MyApplication app = (MyApplication) getApplication();

        // Make sure the socket is not already opened
        if (sock != null && sock.isConnected() && !sock.isClosed()) {
            Log.d("SOCKET", "Socket already open");

            //LOG CAT ERROR MESSAGE
            return;
        }

        // open the socket.  SocketConnect is a new subclass
        // (defined below).  This creates an instance of the subclass
        // and executes the code in it.
        new SocketConnect().execute((Void) null);
    }

    public class SocketConnect extends AsyncTask<Void, Void, Socket> {

        // The main parcel of work for this thread.  Opens a socket
        // to connect to the specified IP.

        protected Socket doInBackground(Void... voids) {
            Socket s = null;
            String ip = getConnectToIP();
            Integer port = getConnectToPort();

            try {
                s = new Socket(ip, port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }

        // After executing the doInBackground method, this is
        // automatically called, in the UI (main) thread to store
        // the socket in this app's persistent storage

        protected void onPostExecute(Socket s) {
            sock = s;
        }
    }

    // Called when the user closes a socket
     public void closeSocket() {
         Socket s = sock;
         try {
             s.getOutputStream().close();
             s.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
}
