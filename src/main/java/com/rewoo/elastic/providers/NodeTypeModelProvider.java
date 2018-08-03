package com.rewoo.elastic.providers;


import com.rewoo.elastic.triggers.GetAllNodeTypes;
import io.elastic.api.SelectModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;

/**
 * Implementation of {@link SelectModelProvider} providing a select model for the Scope node type select.
 * The provide sends a HTTP request to the Scope server and returns a JSON object as shown below.
 *
 * <pre>
 *     {
 *         "id": "typename",
 *     }
 * </pre>
 *
 * The value in the returned JSON object are used to display option's labels.
 */
public class NodeTypeModelProvider implements SelectModelProvider {
    private static final Logger logger = LoggerFactory.getLogger(NodeTypeModelProvider.class);

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        logger.info("Try to retrieve node types from REWOO Scope instance");
        final JsonObject nodeTypes = new GetAllNodeTypes().executePlain(configuration);
        logger.info("Successfully retrieved {} node types");

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        nodeTypes.getJsonArray("nodeTypes").parallelStream().forEach(nodeType -> {
            if (nodeType.getValueType() == JsonValue.ValueType.OBJECT) {
                builder.add("id", ((JsonObject) nodeType).getString("name"));
            }
        });
        return builder.build();
    }
}
