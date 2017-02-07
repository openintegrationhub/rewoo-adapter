package io.elastic.petstore.triggers;

import io.elastic.api.Component;
import io.elastic.api.EventEmitter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.petstore.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Trigger to get pets by status.
 */
public class GetPetsByStatusJaxRs extends Component {
    private static final Logger logger = LoggerFactory.getLogger(GetPetsByStatusJaxRs.class);

    /**
     * Creates a component instance with the given {@link EventEmitter}.
     *
     * @param eventEmitter emitter to emit events
     */
    public GetPetsByStatusJaxRs(EventEmitter eventEmitter) {
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

        // access the value of the status field defined in trigger's fields section of component.json
        final JsonString status = configuration.getJsonString("status");
        if (status == null) {
            throw new IllegalStateException("status field is required");
        }
        // access the value of the apiKey field defined in credentials section of component.json
        final JsonString apiKey = configuration.getJsonString("apiKey");
        if (apiKey == null) {
            throw new IllegalStateException("apiKey is required");
        }

        logger.info("About to find pets by status {}", status.getString());

        final JsonArray pets = ClientBuilder.newClient()
                .target("https://petstore.elastic.io")
                .path("v2/pet/findByStatus")
                .queryParam("status", status.getString())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("api-key", apiKey.getString())
                .get(JsonArray.class);

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
