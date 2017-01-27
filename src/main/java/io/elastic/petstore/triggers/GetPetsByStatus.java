package io.elastic.petstore.triggers;

import io.elastic.api.*;
import io.elastic.petstore.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 * Trigger to get pets by status.
 */
public class GetPetsByStatus extends Component {
    private static final Logger logger = LoggerFactory.getLogger(GetPetsByStatus.class);

    /**
     * Creates a component instance with the given {@link EventEmitter}.
     *
     * @param eventEmitter emitter to emit events
     */
    public GetPetsByStatus(EventEmitter eventEmitter) {
        super(eventEmitter);
    }

    /**
     * Executes the trigger's logic by sending a request to the Petstore API and emitting response to the platform.
     *
     * @param parameters execution parameters
     */
    @Override
    public void execute(final ExecutionParameters parameters) {
        final JsonObject configuration = parameters.getConfiguration();

        // access the value of the status field defined in credentials section of component.json
        final JsonString status = configuration.getJsonString("status");
        if (status == null) {
            throw new IllegalStateException("status field is required");
        }
        logger.info("About to find pets by status {}", status.getString());

        final String path = "/pet/findByStatus?status=" + status.getString();

        final JsonArray pets = HttpClientUtils.getMany(path, configuration);

        logger.info("Got {} pets", pets.size());

        final JsonObject body = Json.createObjectBuilder()
                .add("pets", pets)
                .build();

        final Message data
                = new Message.Builder().body(body).build();

        logger.info("Emitting data");

        // emitting the message to the platform
        getEventEmitter().emitData(data);
    }
}
