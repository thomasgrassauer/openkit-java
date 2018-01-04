/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.protocol;

import java.util.StringTokenizer;

/**
 * Implements a status response which is sent for the request types status check and beacon send.
 */
public class StatusResponse extends Response {

    // status response constants
    private static final String RESPONSE_KEY_CAPTURE = "cp";
    private static final String RESPONSE_KEY_SEND_INTERVAL = "si";
    private static final String RESPONSE_KEY_MONITOR_NAME = "bn";
    private static final String RESPONSE_KEY_SERVER_ID = "id";
    private static final String RESPONSE_KEY_MAX_BEACON_SIZE = "bl";
    private static final String RESPONSE_KEY_CAPTURE_ERRORS = "er";
    private static final String RESPONSE_KEY_CAPTURE_CRASHES = "cr";

    // settings contained in status response
    private boolean capture = true;
    private int sendInterval = -1;
    private String monitorName = null;
    private int serverID = -1;
    private int maxBeaconSize = -1;
    private boolean captureErrors = true;
    private boolean captureCrashes = true;

    // *** constructors ***

    public StatusResponse(String response, int responseCode) {
        super(responseCode);
        parseResponse(response);
    }

    // *** private methods ***

    // parses status check response
    private void parseResponse(String response) {
        StringTokenizer tokenizer = new StringTokenizer(response, "&=");
        while (tokenizer.hasMoreTokens()) {
            String key = tokenizer.nextToken();
            String value = tokenizer.nextToken();

            if (RESPONSE_KEY_CAPTURE.equals(key)) {
                capture = (Integer.parseInt(value) == 1);
            } else if (RESPONSE_KEY_SEND_INTERVAL.equals(key)) {
                sendInterval = Integer.parseInt(value) * 1000;
            } else if (RESPONSE_KEY_MONITOR_NAME.equals(key)) {
                monitorName = value;
            } else if (RESPONSE_KEY_SERVER_ID.equals(key)) {
                serverID = Integer.parseInt(value);
            } else if (RESPONSE_KEY_MAX_BEACON_SIZE.equals(key)) {
                maxBeaconSize = Integer.parseInt(value) * 1024;
            } else if (RESPONSE_KEY_CAPTURE_ERRORS.equals(key)) {
                captureErrors = (Integer.parseInt(value) != 0);                    // 1 (always on) and 2 (only on WiFi) are treated the same
            } else if (RESPONSE_KEY_CAPTURE_CRASHES.equals(key)) {
                captureCrashes = (Integer.parseInt(value) != 0);                // 1 (always on) and 2 (only on WiFi) are treated the same
            }
        }
    }

    // *** getter methods ***

    public boolean isCapture() {
        return capture;
    }

    public int getSendInterval() {
        return sendInterval;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public int getServerID() {
        return serverID;
    }

    public int getMaxBeaconSize() {
        return maxBeaconSize;
    }

    public boolean isCaptureErrors() {
        return captureErrors;
    }

    public boolean isCaptureCrashes() {
        return captureCrashes;
    }

}
