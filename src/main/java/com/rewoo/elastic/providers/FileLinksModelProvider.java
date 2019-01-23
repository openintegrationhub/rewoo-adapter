package com.rewoo.elastic.providers;

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;

import javax.json.*;
import java.util.*;

public class FileLinksModelProvider implements SelectModelProvider {

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        String[] idString = configuration.getJsonString("fileContainerNodeType").getString().split(":");
        Long dataTypeId = Long.valueOf(idString[0]);

        Set<Long> dataTypeIds = new HashSet<>();
        dataTypeIds.add(dataTypeId);
        JsonObject entries = ScopeApi.getEntries(configuration, dataTypeIds, Constants.SCOPE_FILE_LINKS_ENTRY_TYPE);

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonArray entriesForType = entries.getJsonArray(dataTypeId.toString());
        entriesForType.getValuesAs(JsonObject.class).forEach(jsonObject -> {
            final Integer id = jsonObject.getInt("id");
            final String label = jsonObject.getString("title");
            builder.add(id.toString(), label);
        });
        return builder.build();
    }

}
