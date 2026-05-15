package com.programmer.escrow.common.util;

import cn.hutool.json.JSONUtil;

import java.util.Collections;
import java.util.List;

public final class JsonArrayUtils {

    private JsonArrayUtils() {
    }

    public static String toJson(List<String> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(items);
    }

    public static List<String> toStringList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(JSONUtil.parseArray(json), String.class);
    }

    public static List<Long> toLongList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(JSONUtil.parseArray(json), Long.class);
    }
}
