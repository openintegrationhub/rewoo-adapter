package com.rewoo.elastic.providers;

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;

import javax.json.*;
import java.util.*;

public class FileLinksModelProvider implements SelectModelProvider {

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        Map<Long, String> dataTypeIdToTitle = ScopeApi.getDataTypesMap(configuration);
        Set<Long> dataTypeIds = dataTypeIdToTitle.keySet();
        JsonObject entries = ScopeApi.getEntries(configuration, dataTypeIds, Constants.SCOPE_FILE_LINKS_ENTRY_TYPE);
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Long dataTypeId : dataTypeIds) {
            String dataTypeName = dataTypeIdToTitle.get(dataTypeId);
            JsonArray entriesForType = entries.getJsonArray(dataTypeId.toString());
            entriesForType.getValuesAs(JsonObject.class).stream().forEach(jsonObject -> {
                final Integer id = jsonObject.getInt("id");
                final String label = dataTypeName + "@" + jsonObject.getString("title");
                builder.add(id.toString(), label);
            });
        }
        return builder.build();
    }

}
