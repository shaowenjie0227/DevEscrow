package com.programmer.escrow.demand.util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.programmer.escrow.demand.model.DemandFileItem;
import com.programmer.escrow.demand.model.DemandPayload;

import java.util.ArrayList;
import java.util.List;

public final class DemandPayloadUtils {

    private DemandPayloadUtils() {
    }

    public static String toJson(DemandPayload payload) {
        if (payload == null) {
            return null;
        }
        return JSONUtil.toJsonStr(payload);
    }

    public static DemandPayload parse(String json) {
        if (json == null || json.isBlank()) {
            return new DemandPayload();
        }
        try {
            Object parsed = JSONUtil.parse(json);
            if (parsed instanceof JSONArray) {
                JSONArray array = (JSONArray) parsed;
                DemandPayload payload = new DemandPayload();
                List<DemandFileItem> attachments = new ArrayList<>();
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }
                    String value = String.valueOf(item);
                    if (value.isBlank()) {
                        continue;
                    }
                    DemandFileItem fileItem = new DemandFileItem();
                    fileItem.setName("历史附件");
                    fileItem.setUrl(value);
                    attachments.add(fileItem);
                }
                payload.setAttachments(attachments);
                return payload;
            }
            DemandPayload payload = JSONUtil.toBean(JSONUtil.parseObj(parsed), DemandPayload.class);
            return payload == null ? new DemandPayload() : payload;
        } catch (Exception ignored) {
            return new DemandPayload();
        }
    }
}
