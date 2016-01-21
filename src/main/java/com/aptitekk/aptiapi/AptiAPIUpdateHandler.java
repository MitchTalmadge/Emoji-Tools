package com.aptitekk.aptiapi;

public abstract class AptiAPIUpdateHandler {

    private AptiAPI aptiAPI;

    /**
     * Called when a new update is available online.
     * @param newVersion The new version String. Ex: "1.4"
     * @param changeLog The change log, formatted with HTML.
     * @param downloadUrl The URL to a download page.
     */
    public abstract void onUpdateAvailable(String newVersion, String changeLog, String downloadUrl);

}
