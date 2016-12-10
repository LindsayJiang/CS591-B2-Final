package com.example.wenjun.client1;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private Socket socket;

    private static final int SERVERPORT = 8000;
    private static final String SERVER_IP = "128.197.11.36";
    private TextView returnText;
    private String serverMessage;
    private EditText textToBeEncrypted;
    private TextView publicKey;
    private String encryptedMessage;
    private TextView decryptedMessage;
    private Button join;
    private KeyPair keyPair;
    private String strippedKey;
    private int myID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textToBeEncrypted = (EditText)findViewById(R.id.EditText01);
        returnText = (TextView)findViewById(R.id.returnText);
        join = (Button) findViewById(R.id.buttonJoin);
        publicKey = (TextView)findViewById(R.id.publicKey) ;
        decryptedMessage = (TextView)findViewById(R.id.decryptedMessage) ;
        // use it when encrypting: RSA.encryptWithKey(Crypto.stripPublicKeyHeaders(Crypto.writePublicKeyToPreferences(keyPair)),message);
        keyPair = RSA.generate();
        // use it when decrypting: RSA.decryptFromBase64(Crypto.getRSAPrivateKeyFromString(strippedKey), encryptedMessage)
        strippedKey = Crypto.stripPrivateKeyHeaders(Crypto.writePrivateKeyToPreferences(keyPair));
        keyPair.getPublic();
        //Toast.makeText(getBaseContext(),String.valueOf(socket==null),Toast.LENGTH_SHORT).show();
        //new Thread(new SendKeyThread("HELLOSERVER")).start();
        new Thread(new ClientThread()).start();








        //encryptedMessage = RSA.encryptWithKey(Crypto.stripPublicKeyHeaders(Crypto.writePublicKeyToPreferences(keyPair)),message);
        //publicKey.setText(encryptedMessage);

        /*try {
            decryptedMessage.setText(RSA.decryptFromBase64(Crypto.getRSAPrivateKeyFromString(strippedKey), encryptedMessage));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        join.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                new Thread(new SendKeyThread("HELLOSERVER")).start();
            }
        });





    }



    public void onClick(View view) {
        String message = textToBeEncrypted.getText().toString();
        new Thread(new SendKeyThread(message)).start();
    }

    class SendKeyThread implements Runnable{
        private String message;
        public SendKeyThread(String message){
            this.message = message;
        }
        @Override
        public void run() {
            try {
                if(socket==null) {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    socket = new Socket(serverAddr, SERVERPORT);
                }

                Log.i("debuggggg",String.valueOf(socket==null));
                //EditText et = (EditText) findViewById(R.id.EditText01);
                //String str = et.getText().toString();
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                /*
                //used when sending encrypted message.
                encryptedMessage = RSA.encryptWithKey(Crypto.stripPublicKeyHeaders(Crypto.writePublicKeyToPreferences(keyPair)),str);
                out.println(encryptedMessage);
                 */

                /*
                //used when sending unencryted key.
                 */
                out.println(message);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class ClientThread implements Runnable {

        @Override
        public void run() {

            String tag="debug";
            try {
                if(socket==null) {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    socket = new Socket(serverAddr, SERVERPORT);
                }
                Log.i("debuggggg",String.valueOf(socket==null));
                BufferedReader input_stream = new BufferedReader ( new InputStreamReader(socket.getInputStream() ));
                // accept server message for ID

                while (true){
                    serverMessage = input_stream.readLine();
                    if(serverMessage.contains("ID")){
                        myID = Integer.parseInt(serverMessage.substring(2,3));
                        Log.i("ID: ", Integer.toString(myID));
                        break;
                    }
                }
                // if we have the ID, we can now reveive other users' public key.
                while ( true )
                {
                    serverMessage = input_stream.readLine();
                    if (serverMessage != null )
                    {
                        //returnText.setText(serverMessage);
                        Log.d(tag,serverMessage );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(getBaseContext(),serverMessage,Toast.LENGTH_SHORT).show();
                                returnText.setText(serverMessage);
                            }
                        });
                        //Toast.makeText(getBaseContext(),serverMessage,Toast.LENGTH_SHORT).show();

                        if (serverMessage.contains("### Bye"))
                        {
                            //System.out.println("Server said Bye");
                            break;
                        }
                        //System.out.print("Client:");
                    }
                }

            } catch (UnknownHostException e1) {
        e1.printStackTrace();
    } catch (IOException e1) {
        e1.printStackTrace();
    }

}

    }
}
