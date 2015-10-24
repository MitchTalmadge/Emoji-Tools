package com.AptiTekk.AptiAPI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AptiAPI {

    private static final String API_URL = "http://AptiTekk.com/AptiAPI/";
    private static final String API_VERSION = "V1";
    private static final String TOKEN_GENERATOR = "TokenGenerator.php";
    private static final String ERROR_REPORTER = "ErrorReporter.php";
    protected final ArrayList<AptiAPIListener> APIListeners = new ArrayList<>();
    private final int projectID;

    public AptiAPI(int projectID) {
        this.projectID = projectID;
    }

    public boolean sendErrorReport(String report) {

        try {

            //Step 1 -- Generate Token
            String tokenResponse = POSTData(API_URL + API_VERSION + "/" + TOKEN_GENERATOR, "projectID=" + projectID);

            if (tokenResponse == null) {
                displayError("Could not generate token -- Null response!");
                return false;
            }

            System.out.println("Response: " + tokenResponse);

            String[] responseSplit = tokenResponse.split(":");
            if (responseSplit.length < 3) {
                displayError("Token response length is < 3!");
                return false;
            }

            if (responseSplit[1].equals("FAILURE")) {
                displayError("Could not submit report: " + responseSplit[2]);
                return false;
            }

            String token = responseSplit[2];

            String encryptedReport = new AptiCrypto(token.substring(0, 16)).encrypt(report);

            //Step 2 -- Submit Report
            String errorReportResponse = POSTData(API_URL + API_VERSION + "/" + ERROR_REPORTER, "projectID=" + projectID + "&token=" + token + "&report=" + encryptedReport);

            if (errorReportResponse == null) {
                displayError("Could not submit report: Null response");
            }

            responseSplit = tokenResponse.split(":");
            if (responseSplit.length < 2) {
                displayError("Could not submit report: Token response length is < 2");
                return false;
            }

            if (responseSplit[1].equals("FAILURE")) {
                displayError("Could not submit report: " + responseSplit[2]);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void addAPIListener(AptiAPIListener listener) {
        if (!APIListeners.contains(listener))
            APIListeners.add(listener);
    }

    public void removeAPIListener(AptiAPIListener listener) {
        if (APIListeners.contains(listener))
            APIListeners.remove(listener);
    }

    private void displayInfo(String message) {
        for (AptiAPIListener listener : APIListeners) {
            listener.displayInfo(message);
        }
    }

    private void displayError(String message) {
        for (AptiAPIListener listener : APIListeners) {
            listener.displayError(message);
        }
    }

    private void shutdown() {
        for (AptiAPIListener listener : APIListeners) {
            listener.shutdown();
        }
    }

    private String POSTData(String url, String data) {
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (UnknownHostException e) {
            displayError("Could not connect to Error Reporter. Is your Internet working?");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
