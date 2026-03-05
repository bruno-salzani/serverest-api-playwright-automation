package com.serverest.utils;

import io.qameta.allure.Attachment;

public final class AllureAttachments {
    private AllureAttachments() {
    }

    @Attachment(value = "{name}", type = "application/json")
    public static String attachJson(String name, String content) {
        return content;
    }
}
