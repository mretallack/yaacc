package de.yaacc.upnp;

import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.UpnpService;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/*
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 * A client facade to the upnp lookup and access framework. This class provides
 * all services to manage devices.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class UpnpClient implements RegistryListener {

	private List<UpnpClientListener> listeners = new ArrayList<UpnpClientListener>();

	private List<UpnpDeviceHolder> upnpDevices = new ArrayList<UpnpDeviceHolder>();

	private UpnpRegistryService upnpRegistryService;

	public UpnpClient() {

	}

	public void initialize(Context context) {
		context.bindService(new Intent(context, UpnpRegistryService.class),
				new AndroidUpnpServiceConnection(), Context.BIND_AUTO_CREATE);
	}

	/**
	 * Add an listener.
	 * 
	 * @param listener
	 *            the listener to be added
	 */
	public void addUpnpClientListener(UpnpClientListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove the given listener.
	 * 
	 * @param listener
	 *            the listener which is to be removed
	 */
	public void removeUpnpClientListener(UpnpClientListener listener) {
		listeners.remove(listener);
	}

	public UpnpRegistryService getUpnpRegistryService() {
		return upnpRegistryService;
	}

	/**
	 * Setting an new upnpRegistryService. If the service is not null, refresh
	 * the device list.
	 * 
	 * @param upnpRegistryService
	 */
	public void setUpnpRegistryService(UpnpRegistryService upnpRegistryService) {
		this.upnpRegistryService = upnpRegistryService;
		refreshUpnpDeviceCatalog();

	}

	private void refreshUpnpDeviceCatalog() {
		if (getUpnpRegistryService() != null) {
			for (@SuppressWarnings("rawtypes")
			Device device : getUpnpRegistryService().getRegistry().getDevices()) {
				this.deviceAdded(device);
			}

			// Getting ready for future device advertisements
			getUpnpRegistryService().getRegistry().addListener(this);

			// Search asynchronously for all devices
			getUpnpRegistryService().getUpnpService().getControlPoint().search();
		}
	}

	private void deviceAdded(@SuppressWarnings("rawtypes") final Device device) {
		UpnpDeviceHolder upnpDeviceHolder = new UpnpDeviceHolder(device);
		upnpDevices.add(upnpDeviceHolder);
		fireDeviceAdded(upnpDeviceHolder);

	}

	private void deviceRemoved(@SuppressWarnings("rawtypes") final Device device) {
		UpnpDeviceHolder upnpDeviceHolder = new UpnpDeviceHolder(device);
		upnpDevices.remove(upnpDeviceHolder);
		fireDeviceRemoved(upnpDeviceHolder);
	}

	private void deviceUpdated(@SuppressWarnings("rawtypes") final Device device) {
		UpnpDeviceHolder upnpDeviceHolder = new UpnpDeviceHolder(device);		
		fireDeviceUpdated(upnpDeviceHolder);
	}
	
	private void fireDeviceAdded(UpnpDeviceHolder holder) {
		for (UpnpClientListener listener : listeners) {
			listener.deviceAdded(holder);
		}
	}

	private void fireDeviceRemoved(UpnpDeviceHolder holder) {
		for (UpnpClientListener listener : listeners) {
			listener.deviceRemoved(holder);
		}
	}

	private void fireDeviceUpdated(UpnpDeviceHolder holder) {
		for (UpnpClientListener listener : listeners) {
			listener.deviceUpdated(holder);
		}
	}
	
	public Registry getRegistry(){
		return getUpnpRegistryService().getRegistry();
	}
	
	// ----------Implementation RemoteListerner Interface

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry,
			RemoteDevice remotedevice) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry,
			RemoteDevice remotedevice, Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice remotedevice) { 
		deviceAdded(remotedevice);

	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice remotedevice) {
		deviceUpdated(remotedevice);
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice remotedevice) {
		deviceRemoved(remotedevice);

	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice localdevice) {
		deviceAdded(localdevice);

	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice localdevice) {
		deviceRemoved(localdevice);

	}

	@Override
	public void beforeShutdown(Registry registry) {
		upnpDevices.clear();

	}

	@Override
	public void afterShutdown() {
		

	}

	/*
	 * 
	 * This program is free software; you can redistribute it and/or modify it
	 * under the terms of the GNU General Public License as published by the
	 * Free Software Foundation; either version 3 of the License, or (at your
	 * option) any later version.
	 * 
	 * This program is distributed in the hope that it will be useful, but
	 * WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
	 * Public License for more details.
	 * 
	 * You should have received a copy of the GNU General Public License along
	 * with this program; if not, write to the Free Software Foundation, Inc.,
	 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
	 */
	/**
	 * Connector to monitor android service creation and destruction.
	 * 
	 * @author Tobias Schöne (openbit)
	 * 
	 */
	class AndroidUpnpServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName className, IBinder service) {
			setUpnpRegistryService((UpnpRegistryService)service);

		}

		public void onServiceDisconnected(ComponentName className) {
			setUpnpRegistryService(null);
		}

	}
}