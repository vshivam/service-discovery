package com.service.discovery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {

	private String LOGTAG = getClass().getSimpleName();
	private String TAG = getClass().getSimpleName();
	private String SERVICE_NAME = "test_service_nsd";
	private String SERVICE_TYPE = "_http._tcp.";
	NsdManager mNsdManager;
	PlaceholderFragment placeholderFragment;
	Button server;
	Button client;
	ServerSocket mServerSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		client = (Button) findViewById(R.id.client);
		server = (Button) findViewById(R.id.server);

		client.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mNsdManager.discoverServices(SERVICE_TYPE,
						NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
			}
		});

		server.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Runnable() {

					@Override
					public void run() {

						try {
							mServerSocket = new ServerSocket(0);
							registerService(mServerSocket.getLocalPort());
							while (!Thread.currentThread().isInterrupted()) {
								Socket clientSocket = mServerSocket.accept();

								CommunicationThread mCommunicationThread = new CommunicationThread(
										clientSocket);
								new Thread(mCommunicationThread).start();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
			}
		});

		mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
		placeholderFragment = new PlaceholderFragment();
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, placeholderFragment).commit();
		}
	}

	class CommunicationThread implements Runnable {

		private Socket clientSocket;

		private BufferedReader input;

		public CommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;

			try {
				this.input = new BufferedReader(new InputStreamReader(
						this.clientSocket.getInputStream()));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					String read = input.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		mNsdManager.unregisterService(mRegistrationListener);
		mNsdManager.stopServiceDiscovery(mDiscoveryListener);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	public void registerService(int port) {
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		serviceInfo.setServiceName(SERVICE_NAME);
		serviceInfo.setServiceType(SERVICE_TYPE);
		serviceInfo.setPort(port);

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
			new Thread(new ClientThread(host, port)).start();

			PrintWriter out;
			try {
				out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream())), true);
				out.println("Hi! Server");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	Socket socket;

	class ClientThread implements Runnable {

		InetAddress inetAddress;
		int port;

		public ClientThread(final InetAddress inetAddress, int port) {
			this.inetAddress = inetAddress;
			this.port = port;
		}

		@Override
		public void run() {

			try {
				socket = new Socket(inetAddress, port);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
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
	}

}
