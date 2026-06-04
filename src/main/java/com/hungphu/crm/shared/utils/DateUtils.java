package com.hungphu.crm.shared.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateUtils {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private DateUtils() {}

    public static LocalDateTime nowVietnam() {
        return LocalDateTime.now(VIETNAM_ZONE);
    }

    public static LocalDate todayVietnam() {
        return LocalDate.now(VIETNAM_ZONE);
    }
}
