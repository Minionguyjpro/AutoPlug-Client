/*
 * Copyright (c) 2024 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

 package com.osiris.autoplug.client.tasks.updater.search;

 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.osiris.autoplug.client.utils.UtilsURL;
 import com.osiris.jlib.json.Json;
 import com.osiris.jlib.search.Version;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.function.BiConsumer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class CustomCheckURL {
 
     public CustomCheckURL(){}

     public SearchResult doCustomCheck(String url, String currentVersion) {
         url = new UtilsURL().clean(url);
         Exception exception = null;
         String latest = null;
         String type = ".jar";
         String downloadUrl = null;
         SearchResult.Type code = SearchResult.Type.UP_TO_DATE;
         try {
             Matcher geyserBuildIdFinder = Pattern.compile("\\(b(\\d+)").matcher(currentVersion);
             if (geyserBuildIdFinder.find()) { // For example (b123)
                 String build = geyserBuildIdFinder.group(1); // "123"
                 currentVersion = currentVersion.substring(0, geyserBuildIdFinder.start()).trim() + "." + build;
             }

             JsonElement response = Json.get(url);
             List<String> builds = new ArrayList<>();
             List<String> latestVersions = new ArrayList<>();
             List<String> downloadUrls = new ArrayList<>();
             traverseJson("", response, (key, value) -> {
                 String s1 = getLatestBuildIfValid(key, value);
                 if (!s1.isEmpty()) builds.add(s1);

                 String s2 = getLatestVersionIfValid(key, value);
                 if (!s2.isEmpty()) latestVersions.add(s2);
 
                 String s3 = getDownloadUrlIfValid(key, value);
                 if (!s3.isEmpty()) downloadUrls.add(s3);
             });

             if (builds.isEmpty()) builds.add("0");
             if (!latestVersions.isEmpty()) latest = latestVersions.get(0);
             latest = latest + "." + builds.get(0);
             if (!downloadUrls.isEmpty()) downloadUrl = downloadUrls.get(0);

             if (latest == null) latest = "";
             String[] parts = Version.cleanAndSplitByDots(currentVersion);
             currentVersion = String.join(".", parts);
             if (Version.isFirstBigger(latest, currentVersion)) code = SearchResult.Type.UPDATE_AVAILABLE;
             
         } catch (Exception e) {
             exception = e;
             code = SearchResult.Type.API_ERROR;
         }
 
         if (downloadUrl == null && url == null)
             code = SearchResult.Type.API_ERROR;
         SearchResult result = new SearchResult(null, code, latest, downloadUrl, type, null, null, false);
         result.setException(exception);
         return result;
     }

     /**
      * Returns empty string if not valid.
      */
     private String getLatestBuildIfValid(String key, String value) {
         if (key.equals("build"))
             return value;
         else return "";
     }

     /**
      * Returns empty string if not valid.
      */
     private String getLatestVersionIfValid(String key, String value) {
         if (key.equals("version_number") || key.equals("version"))
             return value.replaceAll("[^0-9.]", "");
         else return "";
     }
 
     /**
      * Returns empty string if not valid.
      */
     private String getDownloadUrlIfValid(String key, String value) {
         if (key.equals("download_url") || key.equals("download") || key.equals("file") || key.equals("download_file"))
             return value;
         else return "";
     }
 
     public static void traverseJson(String key, JsonElement element, BiConsumer<String, String> code) {
         if (element.isJsonObject()) {
             JsonObject obj = element.getAsJsonObject();
             for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                 traverseJson(entry.getKey(), entry.getValue(), code);
             }
         } else if (element.isJsonArray()) {
             JsonArray array = element.getAsJsonArray();
             for (JsonElement item : array) {
                 traverseJson(key, item, code);
             }
         } else if (element.isJsonNull()) {
             code.accept(key, "");
         } else if (element.isJsonPrimitive()) {
             code.accept(key, element.getAsString());
         } else
             throw new IllegalArgumentException("Invalid JSON response format");
     }
 }
 