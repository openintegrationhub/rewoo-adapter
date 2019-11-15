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

package com.rewoo.elastic.triggers;

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.connection.HttpClientUtils;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.EventEmitter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.Date;
import java.util.HashMap;

/**
 * Trigger to get all changed files since last request
 */
@SuppressWarnings("unused")
public class GetAllChangedFiles implements Module {

    private static final String LAST_RUN_CHANGED_FILES = "lastRunChangedFiles";

    private static final Logger logger = LoggerFactory.getLogger(GetAllChangedFiles.class);

    @Override
    public void execute(final ExecutionParameters parameters) {
        JsonObject snapshot = parameters.getSnapshot();
        String timestamp = snapshot.getString(LAST_RUN_CHANGED_FILES, "0");
        Date since = new Date(Long.parseLong(timestamp));
        Date until = new Date();

        logger.info("Searching for all changed files since " + since);
        final JsonObject body = executePlain(parameters.getConfiguration(), timestamp);

        // emitting the message to the platform
        EventEmitter eventEmitter = parameters.getEventEmitter();
        snapshot = Json.createObjectBuilder().add(LAST_RUN_CHANGED_FILES, String.valueOf(until.getTime())).build();
        eventEmitter.emitSnapshot(snapshot);
        logger.info("Emitting data");
        final Message data = new Message.Builder().body(body).build();
        eventEmitter.emitData(data);
    }

    private JsonObject executePlain(final JsonObject config, String since) {
        HashMap<String, String> params = new HashMap<>();
        params.put("since", since);
        JsonString selectedEntry = config.getJsonString("fileContainer");
        params.put("entryId", selectedEntry.getString());
        JsonObject changedFilesAnswer = HttpClientUtils.getSingle(Constants.GET_CHANGED_FILES_METHOD, config, params);
        JsonArray changedFiles = changedFilesAnswer.getJsonArray(Constants.SCOPE_CHANGED_FILES_RESPONSE_KEY);
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        changedFiles.getValuesAs(JsonObject.class).forEach(file -> {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for(String key : file.keySet()) {
                if (key.equals("type")) {
                    builder.add(key, file.getString(key).toLowerCase());
                } else {
                    builder.add(key, file.get(key));
                }
            }
            String elementName = ScopeApi.resolveElementIdToName(config, (long) file.getInt("elementId"));
            builder.add("elementName", elementName);
            arrayBuilder.add(builder.build());
        });
        logger.info("Got {} changed files", changedFiles.size());

        // emitting naked arrays is forbidden by the platform
        return Json.createObjectBuilder().add(Constants.SCOPE_CHANGED_FILES_RESPONSE_KEY, arrayBuilder.build()).build();
    }
}
