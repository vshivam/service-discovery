package com.service.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private String LOGTAG = getClass().getSimpleName();
	private String TAG = getClass().getSimpleName();
	private String SERVICE_NAME = "test_service_nsd";
	private String SERVICE_TYPE = "_http._tcp.";
	NsdManager mNsdManager;
	PlaceholderFragment placeholderFragment;
	Button server;
	Button client;
	Button play;
	Handler handler;
	Activity activity;
	String clientIp;
	int clientPort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		client = (Button) findViewById(R.id.client);
		server = (Button) findViewById(R.id.server);
		play = (Button) findViewById(R.id.play);
		handler = new Handler();
		activity = this;
		MusicServer.getListOfMusicFiles(this);

		try {
			MusicServer musicServer = new MusicServer(8090);
			musicServer.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		client.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				placeholderFragment.clearList();
				mNsdManager.discoverServices(SERVICE_TYPE,
						NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
			}
		});

		server.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					bindServer(findFreePort());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					mediaPlayer.setDataSource("http://" + clientIp + ":"
							+ (clientPort + 1));
					Log.d("Client Ip", "http://" + clientIp + ":"
							+ (clientPort + 1));
					mediaPlayer.prepare(); // might take long! (for buffering,
											// etc)
					mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

						@Override
						public void onPrepared(MediaPlayer mp) {
							mediaPlayer.start();

						}
					});
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
		placeholderFragment = new PlaceholderFragment();
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, placeholderFragment).commit();
		}
	}

	public int findFreePort() throws IOException {
		ServerSocket server = new ServerSocket(0);
		int port = server.getLocalPort();
		server.close();
		Log.d(LOGTAG, "Free Port : " + port);
		placeholderFragment.refreshPort(port);
		return port;
	}

	private void bindServer(int port) {
		GrillboxSocketServer grillboxServer;
		try {
			grillboxServer = new GrillboxSocketServer(port, handler, activity);
			grillboxServer.start();
			Toast.makeText(activity,
					"Starting on Port" + grillboxServer.getPort(),
					Toast.LENGTH_SHORT).show();
			registerService(port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		// mNsdManager.unregisterService(mRegistrationListener);
		// mNsdManager.stopServiceDiscovery(mDiscoveryListener);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	public void registerService(int port) {
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		serviceInfo.setPort(port);
		serviceInfo.setServiceName(SERVICE_NAME);
		serviceInfo.setServiceType(SERVICE_TYPE);

		Log.d(LOGTAG, "Registering Service on port  " + port);
		mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD,
				mRegistrationListener);
	}

	RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

		@Override
		public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
			// Save the service name. Android may have changed it in order
			// to
			// resolve a conflict, so update the name you initially
			// requested
			// with the name Android actually used.
			String mServiceName = nsdServiceInfo.getServiceName();
			SERVICE_NAME = mServiceName;
			Log.d(LOGTAG, "Registered name : " + mServiceName + " on "
					+ nsdServiceInfo.getHost() + ":" + nsdServiceInfo.getPort());
		}

		@Override
		public void onRegistrationFailed(NsdServiceInfo serviceInfo,
				int errorCode) {
			// Registration failed! Put debugging code here to determine
			// why.
			Log.d(LOGTAG, "Registration Failed");
		}

		@Override
		public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
			// Service has been unregistered. This only happens when you
			// call
			// NsdManager.unregisterService() and pass in this listener.
			Log.d(LOGTAG,
					"Service Unregistered : " + serviceInfo.getServiceName());
		}

		@Override
		public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
				int errorCode) {
			// Unregistration failed. Put debugging code here to determine
			// why.
		}
	};

	NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

		// Called as soon as service discovery begins.
		@Override
		public void onDiscoveryStarted(String regType) {
			Log.d(TAG, "Service discovery started");
		}

		@Override
		public void onServiceFound(NsdServiceInfo service) {
			// A service was found! Do something with it.
			Log.d(TAG, "Service discovery success : " + service);
			if (!service.getServiceType().equals(SERVICE_TYPE)) {
				// Service type is the string containing the protocol and
				// transport layer for this service.
				placeholderFragment.refreshList("Unknown Service : "
						+ service.getServiceName());
				Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
			} else if (service.getServiceName().equals(SERVICE_NAME)) {
				// The name of the service tells the user what they'd be
				// connecting to. It could be "Bob's Chat App".
				mNsdManager.resolveService(service, mResolveListener);
				placeholderFragment.refreshList("Self : "
						+ service.getServiceName());
				Log.d(TAG, "Same machine: " + SERVICE_NAME);
			} else if (service.getServiceName().contains(SERVICE_NAME)) {
				Log.d(TAG, "Diff Machine : " + service.getServiceName());
				mNsdManager.resolveService(service, mResolveListener);
				placeholderFragment.refreshList("Relevant Service : "
						+ service.getServiceName());
			}
		}

		@Override
		public void onServiceLost(NsdServiceInfo service) {
			// When the network service is no longer available.
			// Internal bookkeeping code goes here.
			Log.e(TAG, "service lost" + service);
		}

		@Override
		public void onDiscoveryStopped(String serviceType) {
			Log.i(TAG, "Discovery stopped: " + serviceType);
		}

		@Override
		public void onStartDiscoveryFailed(String serviceType, int errorCode) {
			Log.e(TAG, "Discovery failed: Error code:" + errorCode);
			mNsdManager.stopServiceDiscovery(this);
		}

		@Override
		public void onStopDiscoveryFailed(String serviceType, int errorCode) {
			Log.e(TAG, "Discovery failed: Error code:" + errorCode);
			mNsdManager.stopServiceDiscovery(this);
		}
	};

	GrillboxSocketClient grillboxClient;
	NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

		@Override
		public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
			// Called when the resolve fails. Use the error code to debug.
			Log.e(TAG, "Resolve failed" + errorCode);
		}

		@Override
		public void onServiceResolved(NsdServiceInfo serviceInfo) {
			Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
			int port = serviceInfo.getPort();
			InetAddress host = serviceInfo.getHost();
			Log.d(LOGTAG, "Host : " + host + "port : " + port);
			clientIp = host.getHostAddress();
			clientPort = serviceInfo.getPort();
			String uriString = "ws:/" + host + ":" + port;
			Toast.makeText(activity, uriString, Toast.LENGTH_SHORT).show();
			try {
				grillboxClient = new GrillboxSocketClient(new URI(uriString),
						handler, activity);
				grillboxClient.connect();

			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		ListView servicesListView;
		ArrayList<String> servicesList;
		ArrayAdapter<String> adapter;
		TextView serverPort;

		public PlaceholderFragment() {
		}

		public void refreshPort(int port) {
			serverPort.setText(port + "");
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			serverPort = (TextView) rootView.findViewById(R.id.hello_world);
			servicesList = new ArrayList<String>();
			servicesListView = (ListView) rootView
					.findViewById(R.id.servicesList);
			adapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, servicesList);
			servicesListView.setAdapter(adapter);
			return rootView;
		}

		public void refreshList(final String serviceName) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					servicesList.add(serviceName);
					adapter.notifyDataSetChanged();
				}
			});

		}

		public void clearList() {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					servicesList.clear();
					adapter.notifyDataSetChanged();
				}
			});

		}
	}

}
