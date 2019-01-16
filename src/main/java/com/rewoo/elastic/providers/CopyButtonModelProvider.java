package com.rewoo.elastic.providers;

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

public class CopyButtonModelProvider implements SelectModelProvider {

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        Map<Long, List<JsonObject>> dataTypesMap = ScopeApi.getDataTypesMap(configuration);
        Set<Long> dataTypeIds = dataTypesMap.keySet();
        JsonObject entryMap = ScopeApi.getEntries(configuration, dataTypeIds, Constants.SCOPE_COPY_BUTTON_ENTRY_TYPE);

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Long dataTypeId : dataTypeIds) {
            JsonArray entriesForDataType = entryMap.getJsonArray(dataTypeId.toString());
            if (entriesForDataType.isEmpty()) {
                continue;
            }
            List<JsonObject> nodeTypes = dataTypesMap.get(dataTypeId);
            List<JsonObject> elementsForDataType = getElementsForNodeTypes(configuration, nodeTypes);
            if (elementsForDataType.isEmpty()) {
                continue;
            }

            for (JsonObject entry : entriesForDataType.getValuesAs(JsonObject.class)) {
                final Integer entryId = entry.getInt("id");
                final String entryTitle = entry.getString("title");
                for (JsonObject element : elementsForDataType) {
                    final Integer elementId = element.getInt("id");
                    final String elementName = element.getString("name");
                    final String label = elementName + "@" + entryTitle;
                    builder.add(elementId.toString() + "@" + entryId.toString(), label);

                }
            }
        }
        return builder.build();
    }

    private List<JsonObject> getElementsForNodeTypes(final JsonObject configuration, List<JsonObject> nodeTypes) {
        Set<Long> nodeTypeIds = new HashSet<>();
        for (JsonObject nodeType : nodeTypes) {
            nodeTypeIds.add((long) nodeType.getInt(Constants.SCOPE_ENTITY_ID_KEY));
        }
        JsonObject elements = ScopeApi.getElements(configuration, nodeTypeIds);
        List<JsonObject> result = new ArrayList<>();
        for (Long nodeTypeId : nodeTypeIds) {
            result.addAll(elements.getJsonArray(nodeTypeId.toString()).getValuesAs(JsonObject.class));
        }
        return result;
    }

}
