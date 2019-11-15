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

package com.rewoo.elastic.api;

import com.rewoo.elastic.connection.HttpClientUtils;
import com.rewoo.elastic.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;
import java.util.stream.Collectors;

public class ScopeApi {

    private static final Logger logger = LoggerFactory.getLogger(ScopeApi.class);

    public static Map<Long, List<JsonObject>> getDataTypesMap(final JsonObject configuration) {
        final JsonArray nodeTypes = getNodeTypes(configuration);
        Map<Long, List<JsonObject>> dataTypeIdToTitle = new HashMap<>();
        for (JsonValue value : nodeTypes) {
            JsonObject nodeType = (JsonObject) value;
            Long dataTypeId = (long) nodeType.getInt(Constants.SCOPE_TYPE_DATA_TYPE_ID_KEY);
            dataTypeIdToTitle.computeIfAbsent(dataTypeId, k -> new ArrayList<>()).add(nodeType);
        }
        return dataTypeIdToTitle;
    }

    public static JsonArray getNodeTypes(final JsonObject configuration) {
        logger.info("About to retrieve NodeTypes from the REWOO Scope instance");
        final JsonObject nodeTypesAnswer = HttpClientUtils.getSingle(Constants.GET_NODE_TYPES_METHOD, configuration, new HashMap<>());
        final JsonArray nodeTypes = nodeTypesAnswer.getJsonArray(Constants.SCOPE_NODE_TYPES_RESPONSE_KEY);
        logger.info("Successfully retrieved {} node types", nodeTypes.size());
        return nodeTypes;
    }

    public static JsonObject getEntries(final JsonObject configuration, final Set<Long> dataTypeIds, final String entryType) {
        logger.info("About to retrieve " + entryType + " entries from the REWOO Scope instance");
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SCOPE_TYPE_IDS_KEY, dataTypeIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        params.put(Constants.SCOPE_ENTRY_TYPE_KEY, entryType);
        final JsonObject entriesAnswer = HttpClientUtils.getSingle(Constants.GET_ENTRIES_METHOD, configuration, params);
        final JsonObject entries = entriesAnswer.getJsonObject(Constants.SCOPE_ENTRIES_RESPONSE_KEY);
        logger.info("Successfully retrieved entries for {} types", entries.size());
        return entries;
    }

    public static JsonObject getElements(final JsonObject configuration, final Set<Long> typeIds) {
        logger.info("About to retrieve elements from the REWOO Scope instance");
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SCOPE_TYPE_IDS_KEY, typeIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        final JsonObject elementsAnswer = HttpClientUtils.getSingle(Constants.GET_ELEMENTS_METHOD, configuration, params);
        final JsonObject elements = elementsAnswer.getJsonObject(Constants.SCOPE_ELEMENTS_RESPONSE_KEY);
        logger.info("Successfully retrieved elements for {} types", elements.size());
        return elements;
    }

    public static String resolveElementIdToName(final JsonObject configuration, final Long elementId) {
        logger.info("About to retrieve elements from the REWOO Scope instance");
        HashMap<String, String> params = new HashMap<>();
        params.put("entityId", elementId.toString());
        params.put("entityType", "ELEMENT");
        final JsonObject nameAnswer = HttpClientUtils.getSingle(Constants.RESOLVE_ID_TO_NAME_METHOD, configuration, params);
        logger.info("Successfully retrieved element name");
        return nameAnswer.getString("entityName");
    }

    public static void saveFileByUrl(final JsonObject configuration, final Long elementId, final Long entryId, final String url, final String filename) {
        logger.info("About to save file to REWOO Scope instance");
        HashMap<String, String> params = new HashMap<>();
        params.put("elementId", elementId.toString());
        params.put("entryId", entryId.toString());
        params.put("url", url);
        params.put("filename", filename);
        params.put("overwriteAll", "true");
        HttpClientUtils.getSingle(Constants.SAVE_FILE_BY_URL_METHOD, configuration, params);
        logger.info("Successfully saved file");
    }

    public static Long copyByButton(final JsonObject configuration, final Long elementId, final Long buttonId, final Long actorId, final String name) {
        logger.info("About to copy template node by button to REWOO Scope instance");
        HashMap<String, String> params = new HashMap<>();
        params.put("elementId", elementId.toString());
        params.put("buttonId", buttonId.toString());
        params.put("actorId", actorId.toString());
        params.put("copyName", name);
        params.put("values", "[]");
        params.put("connectionButtonExecutions", "[]");
        JsonObject copiedElements = HttpClientUtils.getSingle(Constants.COPY_BY_BUTTON_METHOD, configuration, params);
        logger.info("Successfully copied template");
        return copiedElements.getJsonArray("copies").getValuesAs(JsonNumber.class).stream().findFirst().get().longValue();
    }

    public static Long getUserId(final JsonObject configuration, final String name) {
        logger.info("About get users from REWOO Scope instance");
        HashMap<String, String> params = new HashMap<>();
        final JsonObject nameAnswer = HttpClientUtils.getSingle(Constants.GET_SCOPE_USERS_METHOD, configuration, params);
        JsonObject foundUser = nameAnswer.getJsonArray("scopeUsers").getValuesAs(JsonObject.class).stream().filter(user -> {
            return user.getString("userName").equals(name);
        }).findFirst().get();
        logger.info("Successfully copied template");
        return (long) foundUser.getInt("id");
    }


}
