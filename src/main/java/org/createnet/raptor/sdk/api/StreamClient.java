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
import org.createnet.raptor.sdk.PageResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.events.callback.DataCallback;
import org.createnet.raptor.sdk.events.callback.StreamEventCallback;

import java.util.List;

import org.createnet.raptor.models.app.App;
//import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.RecordSet;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.models.payload.StreamPayload;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.sdk.RequestOptions;
import org.createnet.raptor.sdk.events.callback.RaptorCallback;
import org.createnet.raptor.sdk.events.callback.StreamCallback;

/**
 * Represent a Device data stream
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class StreamClient extends AbstractClient {

    public StreamClient(Raptor container) {
        super(container);
    }

    /**
     * Subscribe to a data stream
     *
     * @param stream
     * @param ev
     */
    public void subscribe(Stream stream, StreamEventCallback ev) {
        getEmitter().subscribe(stream, ev);
    }

    /**
     * Subscribe to a data stream
     *
     * @param stream
     * @param ev
     */
    public void subscribe(Stream stream, RaptorCallback ev) {
        subscribe(stream, new StreamEventCallback() {
            @Override
            public void trigger(DispatcherPayload dpayload) {

                StreamPayload payload = (StreamPayload) dpayload;

                if (ev instanceof DataCallback) {
                    DataCallback ev1 = (DataCallback) ev;
                    ev1.callback(stream, payload.record);
                }

                if (ev instanceof StreamCallback) {
                    StreamCallback ev1 = (StreamCallback) ev;
                    ev1.callback(stream, payload);
                }

            }
        });
    }

    /**
     * Send stream data
     *
     * @param record the record to send
     */
    public void push(RecordSet record) {
        getClient().put(String.format(Routes.STREAM_PUSH, record.getStream().getDevice().id(), record.getStream().name()), record.toJsonNode(), RequestOptions.retriable().waitFor(300).maxRetries(5));
    }

    /**
     * Send stream data
     *
     * @param s
     * @param record the record to send
     */
    public void push(Stream s, RecordSet record) {
        getClient().put(String.format(Routes.STREAM_PUSH, s.getDevice().id(), s.name()), record.toJsonNode());
    }

    /**
     * Send stream data
     *
     * @param deviceId id of the object
     * @param streamId name of the stream
     * @param data data to send
     */
    public void push(String deviceId, String streamId, RecordSet data) {
        getClient().put(String.format(Routes.STREAM_PUSH, deviceId, streamId), data.toJsonNode());
    }

    /**
     * Retrieve data from a stream
     *
     * @param stream the stream to read from
     * @return the data resultset
     */
    public PageResponse<RecordSet> pull(Stream stream) {
        return pull(stream, 0, null);
    }

    /**
     * Retrieve data from a stream
     *
     * @param stream the stream to read from
     * @param offset results start from offset
     * @param limit limit the total size of result
     * @return the data resultset
     */
    public PageResponse<RecordSet> pull(Stream stream, Integer offset, Integer limit) {
        String qs = buildQueryString(offset, limit);
        JsonNode json = getClient().get(String.format(Routes.STREAM_PULL, stream.getDevice().id(), stream.name()) + qs);
        PageResponse<RecordSet> list = getMapper().convertValue(json, new TypeReference<PageResponse<RecordSet>>() {});
        return list;
    }

    /**
     * Retrieve data from a stream
     *
     * @param deviceId id of the object
     * @param streamId name of the stream
     * @param offset results start from offset
     * @param limit limit the total size of result
     * @return the data resultset
     */
    public JsonNode pull(String deviceId, String streamId, Integer offset, Integer limit) {
        String qs = buildQueryString(offset, limit);
        return getClient().get(String.format(Routes.STREAM_PULL, deviceId, streamId) + qs);
    }

    /**
     * Retrieve data from a stream
     *
     * @param deviceId id of the object
     * @param streamId name of the stream
     * @return the data resultset
     */
    public JsonNode pull(String deviceId, String streamId) {
        return pull(deviceId, streamId, null, null);
    }

    /**
     * Fetch the last record stored in the stream
     *
     * @param stream
     * @return
     */
    public RecordSet lastUpdate(Stream stream) {
        JsonNode result = getClient().get(String.format(Routes.STREAM_LAST_UPDATE, stream.getDevice().id(), stream.name()), RequestOptions.retriable().maxRetries(3).waitFor(500));
        if (result == null) {
            return null;
        }
        return RecordSet.fromJSON(result);
    }

    /**
     * Search for data in the stream
     *
     * @param stream the stream to search in
     * @param query the search query
     * @return
     */
//    public ResultSet search(Stream stream, DataQuery query) {
//        JsonNode results = getClient().post(
//                String.format(Routes.STREAM_SEARCH, stream.getDevice().id(), stream.name()),
//                query.toJSON(),
//                RequestOptions.retriable().maxRetries(3).waitFor(500)
//        );
////        PageResponse<RecordSet> list = getMapper().convertValue(results, new TypeReference<PageResponse<RecordSet>>() {});
////        return list;
//        return ResultSet.fromJSON(stream, results);
//    }
    
    /**
     * Search for data in the stream
     *
     * @param stream the stream to search in
     * @param query the search query
     * @return
     */
    public PageResponse<RecordSet> searchRecords(Stream stream, DataQuery query) {
        JsonNode results = getClient().post(
                String.format(Routes.STREAM_SEARCH, stream.getDevice().id(), stream.name()),
                query.toJSON(),
                RequestOptions.retriable().maxRetries(3).waitFor(500)
        );
        PageResponse<RecordSet> list = getMapper().convertValue(results, new TypeReference<PageResponse<RecordSet>>() {});
        return list;
    }

    /**
     * Drop all data stored in a stream
     *
     * @param stream
     */
    public void delete(Stream stream) {
        getClient().delete(
                String.format(Routes.STREAM_GET, stream.getDevice().id(), stream.name())
        );
    }

}
