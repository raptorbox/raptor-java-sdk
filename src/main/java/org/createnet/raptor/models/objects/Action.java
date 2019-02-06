/*
 * Copyright 2017 FBK/CREATE-NET <http://create-net.fbk.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.createnet.raptor.models.objects.serializer.ActionSerializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import org.createnet.raptor.models.data.ActionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = ActionSerializer.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Action extends DeviceContainer {

    static final long serialVersionUID = 1000000000000265L;
    
    @JsonIgnore
    @Transient
    private final Logger logger = LoggerFactory.getLogger(Action.class);
    
    protected String id = null;
    protected String status = null;
    protected String name;
    protected String description;
    protected String deviceId;
    protected String userId;

    public static Action create(String name, String status) {
        Action a = new Action();
        a.name = name;
        a.status = status;
        return a;
    }

    public static Action create(String name) {
        return Action.create(name, null);
    }

    public Action(String json) {
        JsonNode tree;
        try {
            tree = mapper.readTree(json);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
        parse(tree, null);
    }

    public Action(String json, Device object) {
        ObjectMapper mapper = Device.getMapper();
        JsonNode tree;
        try {
            tree = mapper.readTree(json);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
        parse(tree, object);
    }

    public Action(JsonNode json, Device object) {
        parse(json, object);
    }

    public Action(JsonNode json) {
        parse(json, null);
    }

    public Action(String name, JsonNode json, Device object) {
        this.name = name;
        parse(json, object);
    }

    public Action(String name, JsonNode json) {
        this.name = name;
        parse(json, null);
    }

    public Action() {
    }

    protected void parse(JsonNode json) {

        if (json.isTextual()) {
            name = json.asText();
        } else {

            if (json.has("id")) {
                id = json.get("id").asText();
            }

            if (json.has("status")) {
                status = json.get("status").asText();
            }

            if (json.has("name")) {
                name = json.get("name").asText();
            }

            if (json.has("description")) {
                description = json.get("description").asText();
            }
        }
    }

    protected void parse(JsonNode json, Device object) {
        this.setDevice(object);
        parse(json);
    }

    @Override
    public void validate() {
        if (name == null) {
            throw new ValidationException("Action name is empty");
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

    /**
     * Return a status object for this action
     *
     * @return
     */
    public ActionStatus getStatus() {
        return new ActionStatus(this, this.status);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String status() {
        return status;
    }
    
    public Action id(String id) {
        this.id = id;
        return this;
    }

    public Action status(String status) {
        this.status = status;
        return this;
    }

    public Action name(String name) {
        this.name = name;
        return this;
    }

    public Action description(String description) {
        this.description = description;
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
    
    
    
}
