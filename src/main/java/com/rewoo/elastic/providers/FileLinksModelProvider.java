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

import com.rewoo.elastic.api.ScopeApi;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.SelectModelProvider;

import javax.json.*;
import java.util.*;

/**
 * Implementation of {@link SelectModelProvider} providing a select model for the file container of a selected form type (see {@link NodeTypeModelProvider}).
 * The provide sends a HTTP request to the REWOO Scope API and returns a JSON array of JSON objects as shown below.
 * Further information can be found on https://rewoo.de/manual/T-rest-api.html#formularfelder-eines-typs-abfragen
 *
 * <pre>
 *     {
 *			id: 47,
 *			title: "Invoices",
 *			type: "FileLinks",
 *			isTableColumn: false
 *     }
 * </pre>
 *
 * The title property of the JSON objects are used to display the select.
 */
@SuppressWarnings("unused")
public class FileLinksModelProvider implements SelectModelProvider {

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        String[] idString = configuration.getJsonString("fileContainerNodeType").getString().split(":");
        Long dataTypeId = Long.valueOf(idString[0]);

        Set<Long> dataTypeIds = new HashSet<>();
        dataTypeIds.add(dataTypeId);
        JsonObject entries = ScopeApi.getEntries(configuration, dataTypeIds, Constants.SCOPE_FILE_LINKS_ENTRY_TYPE);

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonArray entriesForType = entries.getJsonArray(dataTypeId.toString());
        entriesForType.getValuesAs(JsonObject.class).forEach(jsonObject -> {
            final Integer id = jsonObject.getInt("id");
            final String label = jsonObject.getString("title");
            builder.add(id.toString(), label);
        });
        return builder.build();
    }

}
