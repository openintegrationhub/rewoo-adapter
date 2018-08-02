package com.rewoo.elastic.triggers;

import com.rewoo.elastic.Constants;
import com.rewoo.elastic.HttpClientUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Collections;
import java.util.HashMap;

/**
 * Trigger to get all node types
 */
public class GetAllNodeTypes implements Module {
    private static final Logger logger = LoggerFactory.getLogger(GetAllNodeTypes.class);

    @Override
    public void execute(final ExecutionParameters parameters) {
        logger.info("Searching for all available node types");
        final String path = Constants.GET_NODE_TYPES_METHOD;

        final JsonObject nodeTypesAnswer = HttpClientUtils.getSingle(path, parameters.getConfiguration(), new HashMap<>());
        final JsonArray nodeTypes = nodeTypesAnswer.getJsonArray(Constants.SCOPE_NODE_TYPES_RESPONSE_KEY);
        logger.info("Got {} node types", nodeTypes.size());

        // emitting naked arrays is forbidden by the platform
        final JsonObject body = Json.createObjectBuilder()
                .add("nodeTypes", nodeTypes)
                .build();

        final Message data
                = new Message.Builder().body(body).build();

        logger.info("Emitting data");

        // emitting the message to the platform
        parameters.getEventEmitter().emitData(data);
    }
}
