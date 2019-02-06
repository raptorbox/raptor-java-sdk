/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.objects;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class Channel extends StreamContainer {

    static final long serialVersionUID = 1000000000000165L;
    
    public static final List<String> Types = new ArrayList<String>(Arrays.asList(
            "number",
            "string",
            "boolean",
            "geopoint",
            "object",
            "array"
    ));

    protected String name;
    protected String type;
    protected String unit;
    protected String deviceId;
    protected String streamId;

    public static Channel create(String name, String type, String unit) {
        Channel channel = new Channel();
        channel.name = name;
        channel.type = type;
        channel.unit = unit;

        return channel;
    }

    public static Channel create(String name, String type) {
        return Channel.create(name, type, null);
    }

    public Channel() {
    }

    public Channel(JsonNode json) {
        this();
        parse(json);
    }

    public Channel(String name, JsonNode json) {
        this();
        this.name = name;
        parse(json);
    }

    @Override
    public void validate() {

        if (name == null) {
            throw new ValidationException("Channel name is empty");
        }

        if (type == null) {
            throw new ValidationException("Channel type is empty");
        }

        if (!Types.contains(type.toLowerCase())) {
            throw new ValidationException("Channel type not supported: " + type);
        }

    }

    @Override
    public void parse(String json) {
        try {
            parse(mapper.readTree(json));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

    public void parse(JsonNode json) {

        if (json.isTextual()) {
            type = json.asText();
        } else {
            if (json.has("name")) {
                name = json.get("name").asText();
            }

            if (json.has("type")) {
                type = json.get("type").asText();
            }

            if (json.has("unit")) {
                unit = json.get("unit").asText();
            }
        }
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public String unit() {
        return unit;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public Channel name(String name) {
        this.name = name;
        return this;
    }

    public Channel type(String type) {
        this.type = type;
        return this;
    }

    public Channel unit(String unit) {
        this.unit = unit;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}
    
    

}
