package com.rewoo.elastic.providers;

import io.elastic.api.DynamicMetadataProvider;

import javax.json.JsonObject;

public class MDMProvider implements DynamicMetadataProvider {
    @Override
    public JsonObject getMetaModel(JsonObject configuration) {
        return null;
    }
}
