/*
 *     Copyright 2019 REWOO Software GmbH
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

import com.rewoo.elastic.connection.HttpClientUtils;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.HashMap;

/**
 * Implementation of {@link SelectModelProvider} providing a select model for the directory level of the file hierarchy.
 * The provide sends a HTTP request to the REWOO Scope API and returns a JSON array of JSON objects as shown below.
 * Further information can be found on https://rewoo.de/manual/T-rest-api.html#nodeTypes
 *
 * <pre>
 *         {
 *             "id": 12,
 *             "dataTypeId": 11,
 *             "name": "Invoices",
 *             "type": "NODE"
 *         }
 * </pre>
 *
 * The name property of the JSON objects in the returned JSON array are used to display the select.
 */
public class NodeTypeModelProvider implements SelectModelProvider {

    private static final Logger logger = LoggerFactory.getLogger(NodeTypeModelProvider.class);

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        logger.info("About to retrieve node types from the REWOO Scope instance");
        final JsonObject nodeTypesAnswer = HttpClientUtils.getSingle(Constants.GET_NODE_TYPES_METHOD, configuration, new HashMap<>());
        final JsonArray nodeTypes = nodeTypesAnswer.getJsonArray(Constants.SCOPE_NODE_TYPES_RESPONSE_KEY);
        logger.info("Successfully retrieved {} node types", nodeTypes.size());

        final JsonObjectBuilder builder = Json.createObjectBuilder();

        nodeTypes.getValuesAs(JsonObject.class).forEach(jsonObject -> {
            final String id = jsonObject.getInt("dataTypeId") + ":" + jsonObject.getInt("id");
            final String label = jsonObject.getString("name");
            builder.add(id, label);
        });

        return builder.build();
    }
}
