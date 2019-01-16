package com.rewoo.elastic.providers;

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;
import java.util.Set;

public class CopyButtonModelProvider implements SelectModelProvider {

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        Map<Long, String> dataTypeIdToTitle = ScopeApi.getDataTypesMap(configuration);
        Set<Long> dataTypeIds = dataTypeIdToTitle.keySet();
        JsonObject entries = ScopeApi.getEntries(configuration, dataTypeIds, Constants.SCOPE_COPY_BUTTON_ENTRY_TYPE);
        JsonObject elements = ScopeApi.getElements(configuration, dataTypeIds);
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Long dataTypeId : dataTypeIds) {
            JsonArray entriesForType = entries.getJsonArray(dataTypeId.toString());
            if (entriesForType.isEmpty()) {
                continue;
            }
            JsonArray elementsForType = elements.getJsonArray(dataTypeId.toString());
            if (elementsForType.isEmpty()) {
                continue;
            }

            entriesForType.getValuesAs(JsonObject.class).stream().forEach(entry -> {
                final Integer entryId = entry.getInt("id");
                final String title = entry.getString("title");
                elementsForType.getValuesAs(JsonObject.class).stream().forEach(element -> {
                    final Integer elementId = element.getInt("id");
                    final String elementName = element.getString("name");
                    final String label = elementName + "@" + title;
                    builder.add(elementId.toString() + "@" + entryId.toString(), label);

                });
            });
        }
        return builder.build();
    }

}
