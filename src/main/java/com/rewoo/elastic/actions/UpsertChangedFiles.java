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
        final Message message = parameters.getMessage();
        final JsonObject body = message.getBody();
        final JsonObject configuration = parameters.getConfiguration();

        final JsonArray changedFiles = body.getJsonArray(Constants.SCOPE_CHANGED_FILES_RESPONSE_KEY);
        if (changedFiles == null) {
            throw new IllegalStateException("Files are required");
        }

        Long nodeTypeId = Long.valueOf(configuration.getJsonString("nodeType").getString());
        Long fileEntryId = Long.valueOf(configuration.getJsonString("fileEntry").getString());
        String copyButtonDescription = configuration.getJsonString("copyButton").getString();
        String[] copyButtonIds = copyButtonDescription.split("@");
        String username = configuration.getJsonString("username").getString();
        Long userId = ScopeApi.getUserId(configuration, username);

        JsonObject elementsForTypes = ScopeApi.getElements(configuration, new HashSet<>(Arrays.asList(nodeTypeId)));
        Map<String, Long> elementNamesToIds = new HashMap<>();
        elementsForTypes.getJsonArray(nodeTypeId.toString()).getValuesAs(JsonObject.class).stream().forEach(element -> {
            elementNamesToIds.put(element.getString("name"), (long) element.getInt("id"));
        });

        changedFiles.getValuesAs(JsonObject.class).stream().forEach(file -> {
            String elementName = file.getString("elementName");
            String url = file.getString("url");
            Long elementId;
            if (elementNamesToIds.containsKey(elementName)) {
                elementId = elementNamesToIds.get(elementName);
            } else {
                // insert -> press copy button
                elementId = ScopeApi.copyByButton(configuration, Long.valueOf(copyButtonIds[0]), Long.valueOf(copyButtonIds[1]), userId, elementName);
            }
            ScopeApi.saveFileByUrl(configuration, elementId, fileEntryId, url);
        });

        logger.info("File successfully updated");

        JsonObject result = Json.createObjectBuilder().add("success", true).build();
        final Message data = new Message.Builder().body(result).build();

        logger.info("Emitting data");
        parameters.getEventEmitter().emitData(data);
    }
}
