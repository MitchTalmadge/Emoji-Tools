/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 Mitch Talmadge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact Mitch Talmadge at mitcht@liveforcode.net
 */

package com.aptitekk.aptiapi;

import com.aptitekk.aptiapi.gui.UpdateNoticeDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AptiAPI {

    private static final String API_URL = "http://AptiTekk.com/AptiAPI/";
    private static final String API_VERSION = "V2";
    private static final String TOKEN_GENERATOR = "TokenGenerator.php";
    private static final String ERROR_REPORTER = "ErrorReporter.php";
    private static final String UPDATE_CHECKER = "UpdateChecker.php";
    protected final ArrayList<AptiAPIListener> APIListeners = new ArrayList<>();
    private AptiAPIErrorHandler errorHandler;
    private AptiCrypto aptiCrypto;
    private AptiAPIVersioningDetails versioningDetails;
    private Image icon;

    public AptiAPI(AptiAPIVersioningDetails versioningDetails, Image icon) {
        this.versioningDetails = versioningDetails;
        this.icon = icon;
    }

    public void addAPIListener(AptiAPIListener listener) {
        if (!APIListeners.contains(listener))
            APIListeners.add(listener);
    }

    public void removeAPIListener(AptiAPIListener listener) {
        if (APIListeners.contains(listener))
            APIListeners.remove(listener);
    }

    protected void info(String message) {
        for (AptiAPIListener listener : APIListeners) {
            listener.aptiApiInfo(message);
        }
    }

    protected void error(String message) {
        for (AptiAPIListener listener : APIListeners) {
            listener.aptiApiError(message);
        }
    }

    public void shutdownListeners() {
        for (AptiAPIListener listener : APIListeners) {
            listener.shutdown();
        }
    }

    private String POSTData(String token, String url, String data) {
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data + (token != null ? "&token=" + token : ""));
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

            String rawResponse = response.toString();

            if (token != null && aptiCrypto != null) { //Data will be encrypted
                try {
                    return aptiCrypto.decrypt(rawResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return rawResponse;
            }

        } catch (UnknownHostException e) {
            System.out.println("Could not connect to AptiTekk.");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getToken() {
        String tokenResponse = POSTData(null, API_URL + API_VERSION + "/" + TOKEN_GENERATOR, "projectID=" + versioningDetails.getAptiAPIProjectID());

        if (tokenResponse == null) {
            System.out.println("Could not generate token: Null response!");
            return null;
        }

        String[] responseSplit = tokenResponse.split("§");
        if (responseSplit.length < 3) {
            error("Could not generate token: Response length is < 3!");
            return null;
        }

        if (responseSplit[1].equals("FAILURE")) {
            error("Could not generate token: " + responseSplit[2]);
            return null;
        }

        String token = responseSplit[2];

        try {
            aptiCrypto = new AptiCrypto(token.substring(0, 16));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return token;
    }

    public void checkForUpdates() {
        int currentVersionID = versioningDetails.getAptiAPIVersionID();

        String token = getToken();

        if (token != null) {
            try {
                String versionIDEncrypted = aptiCrypto.encrypt(currentVersionID + "");
                String updateInfo = POSTData(token, API_URL + API_VERSION + "/" + UPDATE_CHECKER, "projectID=" + versioningDetails.getAptiAPIProjectID() + "&currentVersionID=" + versionIDEncrypted);

                if (updateInfo == null) {
                    System.out.println("Could not check for updates: Null response!");
                    return;
                }

                String[] response = updateInfo.split("§");
                if (response[1].equals("FAILURE")) {
                    error("Could not check for updates: " + response[2]);
                    return;
                }

                if (response[2].equals("1")) {
                    if (response.length < 7) {
                        error("Could not check for updates: Response length is < 7!");
                        return;
                    }

                    new UpdateNoticeDialog(this, response[4], response[5], response[6]).setVisible(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void sendErrorReport(ErrorReport errorReport) {
        new Thread(new ErrorReportSenderTask(errorReport)).start();
        getErrorHandler().onSendingStarted();
    }

    public AptiAPIVersioningDetails getVersioningDetails() {
        return this.versioningDetails;
    }

    public Image getIconImage() {
        return icon;
    }

    public AptiAPIErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(AptiAPIErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private class ErrorReportSenderTask extends Task<Boolean> {

        private ErrorReport errorReport;

        public ErrorReportSenderTask(ErrorReport errorReport) {
            this.errorReport = errorReport;
            getErrorHandler().bindProperties(this.progressProperty(), this.messageProperty());
        }

        @Override
        protected Boolean call() throws Exception {
            try {
                String token = getToken();

                if (token != null) {
                    String encryptedReport = aptiCrypto.encrypt(errorReport.generateExceptionReport());

                    String errorReportResponse = POSTData(token, API_URL + API_VERSION + "/" + ERROR_REPORTER, "projectID=" + versioningDetails.getAptiAPIProjectID() + "&report=" + encryptedReport);

                    if (errorReportResponse == null) {
                        updateMessage("Could not submit report: Null response!");
                        error("Could not submit report: Null response!");
                        return false;
                    }

                    String[] response = errorReportResponse.split("§");
                    if (response.length < 2) {
                        updateMessage("Could not submit report: Response length is < 2!");
                        error("Could not submit report: Response length is < 2!");
                        return false;
                    }

                    if (response[1].equals("FAILURE")) {
                        updateMessage("Could not submit report: " + response[2]);
                        error("Could not submit report: " + response[2]);
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void done() {
            try {
                boolean result = get();
                Platform.runLater(() -> getErrorHandler().onSendingComplete(result));
            } catch (InterruptedException | ExecutionException e) {
                error("Error occurred while sending report!");
                error(e.getMessage());
            }
        }
    }
}
