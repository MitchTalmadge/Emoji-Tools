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
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
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

    private String POSTData(String url, MultipartEntityBuilder entityBuilder) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost(url);

            HttpEntity httpEntity = entityBuilder.build();
            post.setEntity(httpEntity);

            CloseableHttpResponse httpResponse = httpClient.execute(post);
            HttpEntity responseEntity = httpResponse.getEntity();

            BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            br.close();

            return new String(builder);

        } catch (UnknownHostException e) {
            System.out.println("Could not connect to AptiTekk.");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getToken() {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("projectID", versioningDetails.getAptiAPIProjectID() + "");
        String tokenResponse = POSTData(API_URL + API_VERSION + "/" + TOKEN_GENERATOR, entityBuilder);

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
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.addTextBody("token", token);
                entityBuilder.addTextBody("projectID", versioningDetails.getAptiAPIProjectID() + "");
                entityBuilder.addTextBody("currentVersionID", versionIDEncrypted);
                String updateInfo = POSTData(API_URL + API_VERSION + "/" + UPDATE_CHECKER, entityBuilder);

                updateInfo = aptiCrypto.decrypt(updateInfo);

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

                    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                    entityBuilder.addTextBody("token", token);
                    entityBuilder.addTextBody("projectID", versioningDetails.getAptiAPIProjectID() + "");
                    entityBuilder.addTextBody("report", encryptedReport);
                    if (errorReport.getLogFile() != null) {
                        entityBuilder.addBinaryBody("logFile", errorReport.getLogFile(), ContentType.TEXT_PLAIN, "EmojiTools.log");
                    }

                    String errorReportResponse = POSTData(API_URL + API_VERSION + "/" + ERROR_REPORTER, entityBuilder);

                    errorReportResponse = aptiCrypto.decrypt(errorReportResponse);

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
                    return true;
                } else
                    return false;
            } catch (Exception e) {
                return false;
            }
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
