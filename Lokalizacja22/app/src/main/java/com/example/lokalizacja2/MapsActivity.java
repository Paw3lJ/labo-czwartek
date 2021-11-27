package com.example.lokalizacja2;

import androidx.fragment.app.FragmentActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //private static final int REQUEST_ENABLE_BT=1;
    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;
    private LatLng latLng;
    private BroadcastReceiver receiver;
    private int REQUEST_ENABLE_BT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //BroadcastReceiver receiver;
        registerReceiver(receiver, filter);

        // Create a BroadcastReceiver for ACTION_FOUND.
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
        };
    }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            unregisterReceiver(receiver);
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Moja pozycja"));

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                   // String phoneNumber = "9999";
                    String myLatidude = String.valueOf(location.getLatitude());
                    String myLongitude = String.valueOf(location.getLongitude());

                    String message = "Latitude = " + myLatidude + " Longitude = " + myLongitude;
                    //SmsManager smsManager = SmsManager.getDefault();
                    //smsManager.sendTextMessage(phoneNumber,null,message,null,null);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME,MIN_DIST,locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIST,locationListener);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }

    }
    public void getLocationDetails(View view)
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }

        int requestCode = 1;
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, requestCode);

         class MyBluetoothService {
            private static final String TAG = "MY_APP_DEBUG_TAG";
            private Handler handler; // handler that gets info from Bluetooth service

            // Defines several constants used when transmitting messages between the
            // service and the UI.
            class MessageConstants {
                public static final int MESSAGE_READ = 0;
                public static final int MESSAGE_WRITE = 1;
                public static final int MESSAGE_TOAST = 2;

                // ... (Add other message types here as needed.)
            }

             class ConnectedThread extends Thread {
                private final BluetoothSocket mmSocket;
                private final InputStream mmInStream;
                private final OutputStream mmOutStream;
                private byte[] mmBuffer; // mmBuffer store for the stream

                public ConnectedThread(BluetoothSocket socket) {
                    mmSocket = socket;
                    InputStream tmpIn = null;
                    OutputStream tmpOut = null;

                    // Get the input and output streams; using temp objects because
                    // member streams are final.
                    try {
                        tmpIn = socket.getInputStream();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred when creating input stream", e);
                    }
                    try {
                        tmpOut = socket.getOutputStream();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred when creating output stream", e);
                    }

                    mmInStream = tmpIn;
                    mmOutStream = tmpOut;
                }

                public void run() {
                    mmBuffer = new byte[1024];
                    int numBytes; // bytes returned from read()

                    // Keep listening to the InputStream until an exception occurs.
                    while (true) {
                        try {
                            // Read from the InputStream.
                            numBytes = mmInStream.read(mmBuffer);
                            // Send the obtained bytes to the UI activity.
                            Message readMsg = handler.obtainMessage(
                                    MessageConstants.MESSAGE_READ, numBytes, -1,
                                    mmBuffer);
                            readMsg.sendToTarget();
                        } catch (IOException e) {
                            Log.d(TAG, "Input stream was disconnected", e);
                            break;
                        }
                    }
                }

                // Call this from the main activity to send data to the remote device.
                public void write(byte[] bytes) {
                    try {
                        mmOutStream.write(bytes);

                        // Share the sent message with the UI activity.
                        Message writtenMsg = handler.obtainMessage(
                                MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                        writtenMsg.sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred when sending data", e);

                        // Send a failure message back to the activity.
                        Message writeErrorMsg =
                                handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                        Bundle bundle = new Bundle();
                        bundle.putString("toast",
                                "Couldn't send data to the other device");
                        writeErrorMsg.setData(bundle);
                        handler.sendMessage(writeErrorMsg);
                    }
                }

                // Call this method from the main activity to shut down the connection.
                public void cancel() {
                    try {
                        mmSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                }
            }
        }

    }
}