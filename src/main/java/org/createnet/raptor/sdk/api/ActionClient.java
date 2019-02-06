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

import org.createnet.raptor.sdk.Routes;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.events.callback.ActionCallback;
import org.createnet.raptor.sdk.events.callback.ActionEventCallback;
import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.objects.Action;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.payload.ActionPayload;
import org.createnet.raptor.models.payload.DispatcherPayload;

/**
 * Represent a Device action
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ActionClient extends AbstractClient {

    public ActionClient(Raptor container) {
        super(container);
    }

    /**
     * Subscribe to a data stream
     *
     * @param action
     * @param ev
     */
    protected void subscribe(Action action, ActionEventCallback ev) {
        getEmitter().subscribe(action, ev);
    }

    /**
     * Subscribe to a data stream
     *
     * @param action
     * @param ev
     */
    public void subscribe(Action action, ActionCallback ev) {
        subscribe(action, new ActionEventCallback() {
            @Override
            public void trigger(DispatcherPayload payload) {
                ActionPayload apayload = (ActionPayload) payload;
                ev.callback(action, apayload);
            }
        });
    }    

    /**
     * Get the action status for an object
     *
     * @param action
     * @return return the list of available action for an object
     */
    public ActionStatus getStatus(Action action) {
        return Device.getMapper().convertValue(
                getClient().get(
                        String.format(Routes.ACTION_STATUS, action.getDevice().id(), action.name())
                ),
                ActionStatus.class
        );
    }

    /**
     * Get the action status for an object
     *
     * @param deviceId id of the object
     * @param actionId name of the action
     * @return return the list of available action for an object
     */
    public ActionStatus getStatus(String deviceId, String actionId) {
        return ActionStatus.parseJSON(getClient().get(
            String.format(Routes.ACTION_STATUS, deviceId, actionId)
        ));
    }

    /**
     * Set the action status for an object
     *
     * @param action the action to set the status
     * @param status the current status
     * @return return the list of available action for an object
     */
    public ActionStatus setStatus(Action action, ActionStatus status) {
        return ActionStatus.parseJSON(getClient().post(
                String.format(Routes.ACTION_STATUS, action.getDevice().id(), action.name()), status.status
        ));
    }

    /**
     * Remove the action status for an object
     *
     * @param action the action to set the status
     */
    public void removeStatus(Action action) {
        getClient().delete(
                String.format(Routes.ACTION_STATUS, action.getDevice().id(), action.name())
        );
    }

    /**
     * Remove the action status for an object
     *
     * @param deviceId id of the object
     * @param actionId name of the action
     */
    public void removeStatus(String deviceId, String actionId) {
        getClient().delete(
                String.format(Routes.ACTION_STATUS, deviceId, actionId)
        );
    }

    /**
     * Set the action status for an object
     *
     * @param deviceId id of the object
     * @param actionId name of the action
     * @param status the current status
     * @return return the list of available action for an object
     */
    public ActionStatus setStatus(String deviceId, String actionId, ActionStatus status) {
        return Device.getMapper().convertValue(
                getClient().post(
                        String.format(Routes.ACTION_STATUS, deviceId, actionId), status.toJsonNode()
                ),
                ActionStatus.class
        );
    }

    /**
     * Invoke an action on the object
     *
     * @param action the action reference
     * @param payload the payload to send as string
     */
    public void invoke(Action action, String payload) {
        getClient().put(
                String.format(Routes.ACTION_INVOKE, action.getDevice().id(), action.name()), payload
        );
    }

}
