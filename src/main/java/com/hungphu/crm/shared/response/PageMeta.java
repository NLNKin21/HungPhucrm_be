package com.hungphu.crm.shared.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageMeta {
    private int page;
    private int limit;
    private long total;
    private int totalPages;
}
