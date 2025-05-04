package com.osiris.autoplug.client.tasks.updater.search;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomCheckURLTest {
    @Test
    void test() {
        CustomCheckURL customCheckURL = new CustomCheckURL();
        String url = "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest";
        String currentVersion = "2.2.4-SNAPSHOT (b115)";
        customCheckURL.doCustomCheck(url, currentVersion);
    }
}
