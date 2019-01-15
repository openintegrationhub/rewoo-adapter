package com.rewoo.elastic.providers;

import com.rewoo.elastic.connection.HttpClientUtils;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SelectModelProvider} providing a select model for the pet status select.
 * The provide sends a HTTP request to the Petstore API and returns a JSON object as shown below.
 *
 * <pre>
 *     {
 *         "available": "Available",
 *         "sold": "Sold",
 *         "pending": "Pending"
 *     }
 * </pre>
 *
 * The value in the returned JSON object are used to display option's labels.
 */
public class FileLinksModelProvider implements SelectModelProvider {

    private static final Logger logger = LoggerFactory.getLogger(FileLinksModelProvider.class);

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        Map<Long, String> dataTypeIdToTitle = getDataTypesMap(configuration);
        Set<Long> dataTypeIds = dataTypeIdToTitle.keySet();
        JsonObject entries = getEntries(configuration, dataTypeIds);
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

    private Map<Long, String> getDataTypesMap(final JsonObject configuration) {
        logger.info("About to retrieve NodeTypes from the REWOO Scope instance");
        final JsonObject nodeTypesAnswer = HttpClientUtils.getSingle(Constants.GET_NODE_TYPES_METHOD, configuration, new HashMap<>());
        final JsonArray nodeTypes = nodeTypesAnswer.getJsonArray(Constants.SCOPE_NODE_TYPES_RESPONSE_KEY);
        logger.info("Successfully retrieved {} node types", nodeTypes.size());
        Map<Long, String> dataTypeIdToTitle = new HashMap<>();
        for (JsonValue value : nodeTypes) {
            JsonObject nodeType = (JsonObject) value;
            dataTypeIdToTitle.put((long) nodeType.getInt(Constants.SCOPE_TYPE_DATA_TYPE_ID_KEY), nodeType.getString(Constants.SCOPE_TYPE_DATA_TYPE_NAME_KEY));
        }
        return dataTypeIdToTitle;
    }

    private JsonObject getEntries(final JsonObject configuration, Set<Long> dataTypeIds) {
        logger.info("About to retrieve FileLinks entries from the REWOO Scope instance");
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.SCOPE_TYPE_IDS_KEY, dataTypeIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        params.put(Constants.SCOPE_ENTRY_TYPE_KEY, Constants.SCOPE_FILE_LINKS_ENTRY_TYPE);
        final JsonObject entriesAnswer = HttpClientUtils.getSingle(Constants.GET_ENTRIES_METHOD, configuration, params);
        final JsonObject entries = entriesAnswer.getJsonObject(Constants.SCOPE_ENTRIES_RESPONSE_KEY);
        logger.info("Successfully retrieved entries for {} types", entries.size());
        return entries;
    }

}
