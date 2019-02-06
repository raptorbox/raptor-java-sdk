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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.RecordSet;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.query.DataQuery;
import org.createnet.raptor.sdk.PageResponse;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.Utils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public class DataStreamTest {

	final Logger log = LoggerFactory.getLogger(DataStreamTest.class);

	public static Device device;

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {

		Device d = new Device();
		d.name("data test " + System.currentTimeMillis());

		d.addStream("test", "string", "string");
		d.addStream("test", "number", "number");
		d.addStream("test", "boolean", "boolean");

		Assert.assertTrue(d.stream("test").channels().size() == 3);

		log.debug("Creating {} device", d.name());

		device = d;
	}

	@After
	public void tearDown() {
	}

	private Device createDevice(Raptor raptor, Device d) {
		return raptor.Inventory().create(d);
	}

	private Device createDevice(Raptor raptor) {
		return raptor.Inventory().create(device);
	}

	private List<RecordSet> createRecordSet(Stream stream, int length) {
		List<RecordSet> records = new ArrayList<>();
		for (int i = 0; i < length; i++) {

			long time = (long) (Instant.now().toEpochMilli() - (i * 1000) - (Math.random() * 100));
			log.debug("Set timestamp to {}", time);

			double posX = 11.45 + (System.currentTimeMillis() % 2 == 0 ? -1 * i : i);
			double posY = 45.11 + (System.currentTimeMillis() % 2 == 0 ? -1 * i : i);

			RecordSet record = new RecordSet(stream).channel("number", i)
					.channel("string", System.currentTimeMillis() % 2 == 0 ? "Hello world" : "See you later")
					.channel("boolean", System.currentTimeMillis() % 2 == 0).location(new GeoJsonPoint(posX, posY))
					.timestamp(new Date(time));

			records.add(record);
		}

		return records;
	}

	private void pushRecords(Raptor raptor, Stream s, int len) {
		pushRecords(raptor, s, len, 2500);
	}

	private void pushRecords(Raptor raptor, Stream s, int len, int waitFor) {
		log.debug("Pushing {} records on {}", len, s.name());
		List<RecordSet> records = createRecordSet(s, len);
		records.parallelStream().forEach(record -> raptor.Stream().push(record));
	}

	@Test
	public void pushData() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Push device data");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		pushRecords(raptor, s, 1);
	}

	@Test
	public void dropData() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Drop device data");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		pushRecords(raptor, s, 10);

		raptor.Stream().delete(s);

		PageResponse<RecordSet> results = raptor.Stream().pull(s);

		Assert.assertTrue(results.getContent().isEmpty());

	}

	@Test
	public void pullRecords() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Pull device data");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		int qt = 5;
		pushRecords(raptor, s, qt);

		PageResponse<RecordSet> results = raptor.Stream().pull(s);
		Assert.assertEquals(qt, results.getContent().size());
	}

	@Test
	public void pullLastUpdate() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Pull device last update");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		String msg = "LastUpdate";

		RecordSet r = new RecordSet(s).channel("number", 1).channel("string", msg).channel("boolean", true)
				.location(new GeoJsonPoint(11.45, 45.11));

		raptor.Stream().push(r);

		RecordSet record = raptor.Stream().lastUpdate(s);
		Assert.assertNotNull(record);

		Long val1 = record.value("number").getNumber().longValue();
		Long val2 = r.value("number").getNumber().longValue();

		Assert.assertTrue(val1.equals(val2));
		Assert.assertEquals(record.value("string").getString(), r.value("string").getString());
		Assert.assertEquals(record.value("boolean").getBoolean(), r.value("boolean").getBoolean());

	}

	@Test
	public void pullEmptyLastUpdate() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Pull empty device last update");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");
                
                Utils.waitFor(100);

		RecordSet record = raptor.Stream().lastUpdate(s);
		Assert.assertNull(record);
	}

	@Test
	public void searchByTimeRange() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Search by time range");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		Instant i = Instant.now();
		RecordSet record = new RecordSet(s).channel("number", 1).timestamp(i);
		raptor.Stream().push(record);

		record = new RecordSet(s).channel("number", 2).timestamp(i.plus(1, ChronoUnit.SECONDS));
		raptor.Stream().push(record);

		record = new RecordSet(s).channel("number", 3).timestamp(i.plus(2, ChronoUnit.SECONDS));
		raptor.Stream().push(record);

		record = new RecordSet(s).channel("number", 4).timestamp(i.plus(3, ChronoUnit.SECONDS));
		raptor.Stream().push(record);

		DataQuery q = new DataQuery().timeRange(i.plus(100, ChronoUnit.MILLIS), i.plus(2500, ChronoUnit.MILLIS));

		log.debug("Searching........ \n\n\n" + q.toJSON().toString());
		PageResponse<RecordSet> results = raptor.Stream().searchRecords(s, q);
		log.debug("Results " + results.getContent().size());

		Assert.assertEquals(2, results.getTotalElements());
	}

	@Test
	public void searchByNumericRange() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Search by numeric range");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		int cnt = 6, offset = 2;
		List<RecordSet> records = createRecordSet(s, cnt);
		records.parallelStream().forEach(record -> raptor.Stream().push(record));
		DataQuery q = new DataQuery();
		q.range("number", offset, cnt);
		 log.debug("Searching........ \n\n" + q.toJSON().toString());

		 PageResponse<RecordSet> results = raptor.Stream().searchRecords(s, q);
		log.debug("Results \n" + results.getTotalElements());

		Assert.assertEquals(cnt - offset, results.getTotalElements());
	}
	
//	@Test
//	public void searchByBoolean() {
//
//		Raptor raptor = Utils.createNewAdminInstance();
//
//		log.debug("Search by Boolean property");
//
//		Device dev = createDevice(raptor);
//		Stream s = dev.stream("test");
//		
//		RecordSet record = new RecordSet(s).channel("on", true);
//		raptor.Stream().push(record);
//
//		record = new RecordSet(s).channel("off", false);
//		raptor.Stream().push(record);
//
//		record = new RecordSet(s).channel("on", false);
//		raptor.Stream().push(record);
//		
//		DataQuery q = new DataQuery();
//		q.match("on", false);
//		log.debug("Searching........ \n\n" + q.toJSON().toString());
//
//		PageResponse<RecordSet> results = raptor.Stream().searchRecords(s, q);
//		log.debug("Results \n" + results.getTotalElements());
//
//		Assert.assertEquals(1, results.getTotalElements());
//	}

	@Test
	public void searchByDistance() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Search by distance");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		int qt = 10;
		pushRecords(raptor, s, qt);

		DataQuery q = new DataQuery();
		q.distance(new GeoJsonPoint(11.45, 45.11), 10000, Metrics.KILOMETERS);
		log.debug("Searching........ \n\n" + q.toJSON().toString());
		
		PageResponse<RecordSet> results = raptor.Stream().searchRecords(s, q);
		log.debug("Found {} records", results.getTotalElements());
		
		Assert.assertEquals(qt, results.getTotalElements());
	}

	@Test
	public void searchByBoundingBox() {

		Raptor raptor = Utils.createNewAdminInstance();

		log.debug("Search by bounding box");

		Device dev = createDevice(raptor);
		Stream s = dev.stream("test");

		int qt = 10;
		pushRecords(raptor, s, qt);
		
		DataQuery q = new DataQuery().boundingBox(new GeoJsonPoint(12, 45), new GeoJsonPoint(10, 44));

		PageResponse<RecordSet> results = raptor.Stream().searchRecords(s, q);
		log.debug("Searching........ \n\n" + q.toJSON().toString());
		
		log.debug("Found {} records", results.getTotalElements());
		Assert.assertTrue(results.getTotalElements() > 0);
	}

}
