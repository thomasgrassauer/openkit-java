package com.dynatrace.openkit.core.configuration;

/**
 * Configuration implementation for AppMon
 */
public class AppMonConfiguration extends AbstractConfiguration {
	public AppMonConfiguration(String applicationName, long visitorID, String endpointURL, boolean verbose) {
		super(OpenKitType.APPMON, applicationName, applicationName, visitorID, endpointURL, verbose);

		setHttpClientConfiguration(
			new HTTPClientConfiguration(
				createBaseURL(endpointURL, OpenKitType.APPMON.getDefaultMonitorName()),
				OpenKitType.APPMON.getDefaultServerID(),
				applicationName,
				verbose));
	}

	@Override
	protected String createBaseURL(String endpointURL, String monitorName) {
		StringBuilder urlBuilder = new StringBuilder();

		urlBuilder.append(endpointURL);
		if (!endpointURL.endsWith("/") && !monitorName.startsWith("/")) {
			urlBuilder.append('/');
		}
		urlBuilder.append(monitorName);

		return urlBuilder.toString();
	}
}
