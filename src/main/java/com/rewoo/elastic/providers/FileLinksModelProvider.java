package com.rewoo.elastic.providers;

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;

import javax.json.*;
import java.util.*;

public class FileLinksModelProvider implements SelectModelProvider {

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        Map<Long, List<JsonObject>> dataTypesMap = ScopeApi.getDataTypesMap(configuration);
        Set<Long> dataTypeIds = dataTypesMap.keySet();
        JsonObject entries = ScopeApi.getEntries(configuration, dataTypeIds, Constants.SCOPE_FILE_LINKS_ENTRY_TYPE);
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Long dataTypeId : dataTypeIds) {
            List<JsonObject> nodeTypes = dataTypesMap.get(dataTypeId);
            if (nodeTypes.isEmpty()) {
                continue;
            }
            String dataTypeName = nodeTypes.get(0).getString(Constants.SCOPE_TYPE_DATA_TYPE_NAME_KEY);
            JsonArray entriesForType = entries.getJsonArray(dataTypeId.toString());
            entriesForType.getValuesAs(JsonObject.class).forEach(jsonObject -> {
                final Integer id = jsonObject.getInt("id");
                final String label = dataTypeName + "@" + jsonObject.getString("title");
                builder.add(id.toString(), label);
            });
        }
        return builder.build();
    }

}
