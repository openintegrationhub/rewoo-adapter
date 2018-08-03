package com.rewoo.elastic.actions;

import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;

public final class SaveDatasheet implements Module {
    private static final Logger logger = LoggerFactory.getLogger(SaveDatasheet.class);
    
    @Override
    public void execute(ExecutionParameters parameters) {
        final JsonObject result = Json.createObjectBuilder().add("success", true).build();
        final Message data = new Message.Builder().body(result).build();
        parameters.getEventEmitter().emitData(data);
    }
}
