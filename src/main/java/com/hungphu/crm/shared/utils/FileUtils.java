package com.hungphu.crm.shared.utils;

import org.springframework.util.StringUtils;

public final class FileUtils {

    private FileUtils() {}

    public static String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public static boolean isPdf(String filename) {
        return "pdf".equalsIgnoreCase(getExtension(filename));
    }
}
