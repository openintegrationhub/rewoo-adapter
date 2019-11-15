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

package com.rewoo.elastic.actions;

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.*;

/**
 * Action to update or create files.
 */
public class UpsertChangedFiles implements Module {
    private static final Logger logger = LoggerFactory.getLogger(UpsertChangedFiles.class);

    /**
     *
     * @param parameters execution parameters
     */
    @Override
    public void execute(final ExecutionParameters parameters) {
        logger.info("About to create or update files");
        final JsonArray changedFiles = parameters.getMessage().getBody().getJsonArray(Constants.SCOPE_CHANGED_FILES_RESPONSE_KEY);
        if (changedFiles == null) {
            throw new IllegalStateException("Files are required");
        }
        final JsonObject configuration = parameters.getConfiguration();

        Long fileEntryId = getFileContainerId(configuration);
        String[] copyButtonIds = getCopyButtonIds(configuration);
        String username = configuration.getJsonString("username").getString();
        Long userId = ScopeApi.getUserId(configuration, username);
        Map<String, Long> elementNamesToIds = getElementNamesToIds(configuration);

        changedFiles.getValuesAs(JsonObject.class).forEach(file -> {
            String elementName = file.getString("elementName");
            String url = file.getString("url");
            Long elementId;
            if (elementNamesToIds.containsKey(elementName)) {
                elementId = elementNamesToIds.get(elementName);
            } else {
                // insert -> press copy button
                elementId = ScopeApi.copyByButton(configuration, Long.valueOf(copyButtonIds[0]), Long.valueOf(copyButtonIds[1]), userId, elementName);
            }
            String filename = file.getString("name") + "." + file.getString("extension");
            ScopeApi.saveFileByUrl(configuration, elementId, fileEntryId, url, filename);
        });
        logger.info("File successfully updated");

        logger.info("Emitting data");
        parameters.getEventEmitter().emitData(createResultData());
    }

    private Long getFileContainerId(final JsonObject configuration) {
        return Long.valueOf(configuration.getJsonString("fileContainer").getString());
    }

    private Long getFileContainerNodeTypeId(final JsonObject configuration) {
        String[] idString = configuration.getJsonString("fileContainerNodeType").getString().split(":");
        return Long.valueOf(idString[1]);
    }

    private String[] getCopyButtonIds(final JsonObject configuration) {
        String copyButtonDescription = configuration.getJsonString("copyButton").getString();
        return copyButtonDescription.split("@");
    }

    private Map<String, Long> getElementNamesToIds(final JsonObject configuration) {
        Long fileContainerNodeTypeId = getFileContainerNodeTypeId(configuration);
        JsonObject elementsForTypes = ScopeApi.getElements(configuration, new HashSet<>(Arrays.asList(fileContainerNodeTypeId)));
        Map<String, Long> elementNamesToIds = new HashMap<>();
        elementsForTypes.getJsonArray(fileContainerNodeTypeId.toString()).getValuesAs(JsonObject.class).forEach(element -> {
            elementNamesToIds.put(element.getString("name"), (long) element.getInt("id"));
        });
        return elementNamesToIds;
    }

    private Message createResultData() {
        JsonObject result = Json.createObjectBuilder().add("success", true).build();
        return new Message.Builder().body(result).build();
    }
}
