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
package org.createnet.raptor.sdk.api;

import org.createnet.raptor.models.acl.Operation;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DevicePayload;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.query.DeviceQuery;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.PageResponse;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.admin.DevicePermissionClient;
import org.createnet.raptor.sdk.events.callback.ActionCallback;
import org.createnet.raptor.sdk.events.callback.DataCallback;
import org.createnet.raptor.sdk.events.callback.DeviceCallback;
import org.createnet.raptor.sdk.events.callback.DeviceEventCallback;
import org.createnet.raptor.sdk.events.callback.StreamCallback;
import org.createnet.raptor.sdk.exception.ClientException;
import org.createnet.raptor.sdk.exception.MissingAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class InventoryClient extends AbstractClient {

    protected DevicePermissionClient Permission;

    public DevicePermissionClient Permission() {
        if (Permission == null) {
            Permission = new DevicePermissionClient(getContainer());
        }
        return Permission;
    }

    public InventoryClient(Raptor container) {
        super(container);
    }

    final static Logger logger = LoggerFactory.getLogger(InventoryClient.class);

    /**
     * Register for device events
     *
     * @param device the device to listen for
     * @param callback The callback to fire on event arrival
     */
    public void subscribe(Device device, DeviceEventCallback callback) {
        getEmitter().subscribe(device, callback);
    }

    /**
     * Subscribe only to device related events like update or delete
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, DeviceCallback ev) {
        getEmitter().subscribe(dev, (DispatcherPayload payload) -> {
        	System.out.println(payload + " =====> " + payload.getType());
            switch (payload.getType()) {
                case device:
                    ev.callback(dev, (DevicePayload) payload);
                    break;
            }
        });
    }

    /**
     * Subscribe only to stream related events like update, push or delete
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, StreamCallback ev) {
        getEmitter().subscribe(dev, (DispatcherPayload payload) -> {
            switch (payload.getType()) {
                case stream:
                    StreamPayload spayload = (StreamPayload) payload;
                    ev.callback(dev.stream(spayload.streamId), spayload);
                    break;
            }
        });
    }

    /**
     * Subscribe only to action related events like invoke or status change
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, ActionCallback ev) {
        getEmitter().subscribe(dev, (DispatcherPayload payload) -> {
//        	System.out.println(payload.getType() + " =====> " + payload.getOp());
            switch (payload.getType()) {
                case action:
                    ActionPayload apayload = (ActionPayload) payload;
                    ev.callback(dev.action(apayload.actionId), apayload);
                    break;
            }
        });
    }

    /**
     * Subscribe only to data updates
     *
     * @param dev
     * @param ev
     */
    public void subscribe(Device dev, DataCallback ev) {
        getEmitter().subscribe(dev, new DeviceEventCallback() {
            @Override
            public void trigger(DispatcherPayload payload) {
            	System.out.println(payload.getType() + " =====> " + payload.getOp());
                switch (payload.getType()) {
                    case stream:
                        if (!payload.getOp().equals(Operation.create)) {
                            return;
                        }
                        StreamPayload dpayload = (StreamPayload) payload;
                        ev.callback(dev.stream(dpayload.record.streamId()), dpayload.record);
                        break;
                }
            }
        });
    }

    /**
     * Create a new device instance
     *
     * @param obj device definition to create
     * @return the Device instance
     */
    public Device create(Device obj) {
        JsonNode node = getClient().post(Routes.INVENTORY_CREATE, obj.toJsonNode());
        if (!node.has("id")) {
            throw new ClientException("Missing ID on object creation");
        }
        obj.id(node.get("id").asText());
        return obj;
    }

    /**
     * Load a device definition
     *
     * @param id unique id of the device
     * @return the Device instance
     */
    public Device load(String id) {
        Device obj = new Device();
        obj.parse(getClient().get(String.format(Routes.INVENTORY_LOAD, id)));
        return obj;
    }

    /**
     * Update a device instance
     *
     * @param obj the Device to update
     * @return the updated Device instance
     */
    public Device update(Device obj) {
        obj.parse(
                getClient().put(
                        String.format(Routes.INVENTORY_UPDATE, obj.id()),
                        obj.toJsonNode()
                )
        );
        return obj;
    }

    /**
     * Search for Devices
     *
     * @param query the query to match the device definitions
     * @return a list of Devices matching the query
     */
    public PageResponse<Device> search(DeviceQuery query) {
        if (query.getUserId() == null) {
            User user = getContainer().Auth().getUser();
            if (user == null) {
                throw new MissingAuthenticationException("User is not available");
            }
            query.userId(user.getId());
        }
        return search(query, 0, 20, new Sort(Sort.Direction.DESC, "createdAt"));
//        JsonNode json = getClient().post(
//                Routes.INVENTORY_SEARCH,
//                query.toJSON()
//        );
//        PageResponse<Device> results = Device.getMapper().convertValue(json, new TypeReference<PageResponse<Device>>() {
//        });
//        return results;
    }
    
    /**
     * Search for Devices
     *
     * @param query the query to match the device definitions
     * @return a list of Devices matching the query
     */
    public PageResponse<Device> search(DeviceQuery query, int page, int size, Sort sort) {
        if (query.getUserId() == null) {
            User user = getContainer().Auth().getUser();
            if (user == null) {
                throw new MissingAuthenticationException("User is not available");
            }
            query.userId(user.getId());
        }
        JsonNode json = getClient().post(
                Routes.INVENTORY_SEARCH + String.format("?page=%s&size=%s&sort=", page, size),
                query.toJSON()
        );
        PageResponse<Device> results = Device.getMapper().convertValue(json, new TypeReference<PageResponse<Device>>() {
        });
        return results;
    }

    /**
     * Delete a Device instance and all of its data
     *
     * @param obj Device to delete
     */
    public void delete(Device obj) {
        getClient().delete(
                String.format(Routes.INVENTORY_DELETE, obj.id())
        );
        obj.id(null);
    }

    /**
     * List accessible devices
     *
     * @param page
     * @param size
     * @param sort
     * @return a pager of Devices
     */
    public PageResponse<Device> list(int page, int size, Sort sort) {
        
        String url = Routes.INVENTORY_LIST + String.format("?page=%s&size=%s&sort=", page, size);
        for (Sort.Order next : sort) {
            url += next.getProperty() + "," + next.getDirection().name();
        }
        JsonNode json = getClient().get(url);
        PageResponse<Device> list = Device.getMapper().convertValue(json, new TypeReference<PageResponse<Device>>() {});
        return list;
    }
    
    /**
     * List accessible devices
     *
     * @return the Device instance
     */
    public PageResponse<Device> list() {
        return list(0, 20, new Sort(Sort.Direction.DESC, "createdAt"));
    }

}
