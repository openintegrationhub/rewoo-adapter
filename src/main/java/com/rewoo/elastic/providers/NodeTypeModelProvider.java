package com.rewoo.elastic.providers;

import com.rewoo.elastic.connection.HttpClientUtils;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.HashMap;

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
public class NodeTypeModelProvider implements SelectModelProvider {

    private static final Logger logger = LoggerFactory.getLogger(NodeTypeModelProvider.class);

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        logger.info("About to retrieve node types from the REWOO Scope instance");
        final String path = Constants.GET_NODE_TYPES_METHOD;

        final JsonObject nodeTypesAnswer = HttpClientUtils.getSingle(path, configuration, new HashMap<>());
        final JsonArray nodeTypes = nodeTypesAnswer.getJsonArray(Constants.SCOPE_NODE_TYPES_RESPONSE_KEY);

        logger.info("Successfully retrieved {} node types", nodeTypes.size());

        final JsonObjectBuilder builder = Json.createObjectBuilder();

        nodeTypes.getValuesAs(JsonObject.class).stream().forEach(jsonObject -> {
            final Integer id = jsonObject.getInt("id");
            final String label = jsonObject.getString("name");
            builder.add(id.toString(), label);
        });

        return builder.build();
    }
}
