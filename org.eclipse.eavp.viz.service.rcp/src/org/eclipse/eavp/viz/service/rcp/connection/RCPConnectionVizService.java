/*******************************************************************************
 * Copyright (c) 2017 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - 
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.eavp.viz.service.rcp.connection;

import java.net.URI;
import java.util.Set;

import org.eclipse.eavp.viz.service.AbstractVizService;
import org.eclipse.eavp.viz.service.IPlot;
import org.eclipse.eavp.viz.service.connections.ConnectionPlot;
import org.eclipse.eavp.viz.service.connections.IVizConnection;
import org.eclipse.eavp.viz.service.rcp.RCPVizService;
import org.eclipse.eavp.viz.service.rcp.preferences.CustomScopedPreferenceStore;

/**
 * An extension of RCPVizService for services that make use of remote
 * IVizConnections.
 * 
 * @author Robert Smith
 *
 * @param <T>
 */
abstract public class RCPConnectionVizService<T> extends RCPVizService {

	/**
	 * The associated connection manager. Its concrete type is irrelevant, as it
	 * only needs access to the underlying {@link IVizConnection}s, which will
	 * be passed to created {@link ConnectionPlot}s.
	 */
	protected final RCPConnectionManager<T> manager;

	public RCPConnectionVizService() {
		super();

		// Create the manager for the connections.
		manager = createConnectionManager();

		// Associate the manager with the preference store.
		CustomScopedPreferenceStore store;
		store = (CustomScopedPreferenceStore) getPreferenceStore();
		String connectionNodeId = getConnectionPreferencesNodeId();
		manager.setPreferenceStore(store, connectionNodeId);
	}

	/**
	 * Creates the connection manager used by this service to manage viz
	 * connections.
	 * 
	 * @return The connection manager for this viz service.
	 */
	protected abstract RCPConnectionManager<T> createConnectionManager();

	/**
	 * Creates a new, empty plot that can handle a viz connection.
	 * 
	 * @return A new, empty connection plot.
	 */
	protected abstract ConnectionPlot<T> createConnectionPlot();

	/**
	 * Overrides the default behavior from {@link AbstractVizService} to find a
	 * viz connection that can handle the URI based on its host (a {@code null}
	 * host is assumed to be local).
	 */
	@Override
	public IPlot createPlot(URI uri) throws Exception {
		// Check for a null URI and an unsupported extension.
		super.createPlot(uri);

		// TODO Provide a way to specify which of the available connections
		// should be used.
		// Get the host from the URI.
		String host = uri.getHost();

		if (host == null) {
			host = "localhost";
		}

		// Get the next available connection for the URI's host.
		Set<String> availableConnections = manager.getConnectionsForHost(host);
		if (availableConnections.isEmpty()) {
			throw new Exception("ConnectionVizService error: "
					+ "No configured connection to host \"" + host + "\".");
		}
		String name = availableConnections.iterator().next();
		IVizConnection<T> connection = manager.getConnection(name);

		// Create a plot using the sub-class' implementation.
		ConnectionPlot<T> plot = createConnectionPlot();

		// Set its connection and data source.
		plot.setConnection(connection);
		plot.setDataSource(uri);

		return plot;
	}

	/**
	 * Gets the ID of the preferences node under which all connections will be
	 * stored.
	 * 
	 * @return The preference node ID for persisted connection information.
	 */
	protected abstract String getConnectionPreferencesNodeId();
}