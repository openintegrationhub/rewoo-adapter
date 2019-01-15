package com.rewoo.elastic.triggers;

import com.rewoo.elastic.connection.HttpClientUtils;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.EventEmitter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.Date;
import java.util.HashMap;

/**
 * Trigger to get all node types
 */
public class GetAllChangedFiles implements Module {

    public static final String LAST_RUN_CHANGED_FILES = "lastRunChangedFiles";
    public static final String FILE_LINKS_ENTRY_ID = "fileLinksEntryId";

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
        final String path = Constants.GET_CHANGED_FILES_METHOD;

        HashMap<String, String> params = new HashMap<>();
        params.put("since", since);
        JsonString selectedEntry = config.getJsonString("selectedEntry");
        params.put("entryId", selectedEntry.toString());
        final JsonObject changedFilesAnswer = HttpClientUtils.getSingle(path, config, params);
        final JsonArray changedFiles = changedFilesAnswer.getJsonArray(Constants.SCOPE_CHANGED_FILES_RESPONSE_KEY);
        logger.info("Got {} changed files", changedFiles.size());

        // emitting naked arrays is forbidden by the platform
        return changedFilesAnswer;
    }
}
