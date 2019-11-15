/*
 *     Copyright 2019 REWOO Technologies AG
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

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
        String[] idString = configuration.getJsonString("copyButtonNodeType").getString().split(":");
        Long dataTypeId = Long.valueOf(idString[0]);
        Long nodeTypeId = Long.valueOf(idString[1]);

        Set<Long> dataTypeIds = new HashSet<>();
        dataTypeIds.add(dataTypeId);
        JsonObject entryMap = ScopeApi.getEntries(configuration, dataTypeIds, Constants.SCOPE_COPY_BUTTON_ENTRY_TYPE);

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonArray entriesForDataType = entryMap.getJsonArray(dataTypeId.toString());
        List<JsonObject> elementsForDataType = getElementsForNodeTypeId(configuration, nodeTypeId);
        if (!entriesForDataType.isEmpty() && !elementsForDataType.isEmpty()) {
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

    private List<JsonObject> getElementsForNodeTypeId(final JsonObject configuration, Long nodeTypeId) {
        Set<Long> nodeTypeIds = new HashSet<>();
        nodeTypeIds.add(nodeTypeId);
        JsonObject elements = ScopeApi.getElements(configuration, nodeTypeIds);
        return elements.getJsonArray(nodeTypeId.toString()).getValuesAs(JsonObject.class);
    }

}
