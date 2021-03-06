/*
 * Copyright 2010-2012 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.arecibo.util.timeline.persistent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.skife.jdbi.v2.DefaultMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.sqlobject.stringtemplate.ExternalizedSqlViaStringTemplate3;

import com.ning.arecibo.util.timeline.CategoryIdAndSampleKind;
import com.ning.arecibo.util.timeline.CategoryIdAndSampleKindBinder;
import com.ning.arecibo.util.timeline.CategoryIdAndSampleKindMapper;
import com.ning.arecibo.util.timeline.HostIdAndSampleKindId;
import com.ning.arecibo.util.timeline.HostIdAndSampleKindIdMapper;
import com.ning.arecibo.util.timeline.StartTimes;
import com.ning.arecibo.util.timeline.StartTimesBinder;
import com.ning.arecibo.util.timeline.StartTimesMapper;
import com.ning.arecibo.util.timeline.chunks.TimelineChunk;
import com.ning.arecibo.util.timeline.chunks.TimelineChunkBinder;

@ExternalizedSqlViaStringTemplate3()
@RegisterMapper({CategoryIdAndSampleKindMapper.class, StartTimesMapper.class, HostIdAndSampleKindIdMapper.class})
public interface TimelineDAOQueries extends Transactional<TimelineDAOQueries>
{
    @SqlQuery
    Integer getHostId(@Bind("hostName") final String host);

    @SqlQuery
    String getHost(@Bind("hostId") final Integer hostId);

    @SqlQuery
    @Mapper(DefaultMapper.class)
    List<Map<String, Object>> getHosts();

    @SqlUpdate
    void addHost(@Bind("hostName") final String host);

    @SqlBatch
    @BatchChunkSize(1000)
    void bulkInsertHosts(@Bind("hostName") Iterator<String> hostsIterator);

    @SqlQuery
    Integer getEventCategoryId(@Bind("eventCategory") final String eventCategory);

    @SqlQuery
    String getEventCategory(@Bind("eventCategoryId") final Integer eventCategoryId);

    @SqlUpdate
    void addEventCategory(@Bind("eventCategory") final String eventCategory);

    @SqlBatch
    @BatchChunkSize(1000)
    void bulkInsertEventCategories(@Bind("eventCategory") Iterator<String> cateogoryNames);

    @SqlQuery
    Iterable<Integer> getSampleKindIdsByHostId(@Bind("hostId") final Integer hostId);

    @SqlQuery
    Iterable<HostIdAndSampleKindId> getSampleKindIdsForAllHosts();

    @SqlQuery
    Integer getSampleKindId(@Bind("eventCategoryId") final int eventCategoryId, @Bind("sampleKind") final String sampleKind);

    @SqlQuery
    CategoryIdAndSampleKind getEventCategoryIdAndSampleKind(@Bind("sampleKindId") final Integer sampleKindId);

    @SqlUpdate
    void addSampleKind(@Bind("eventCategoryId") final int eventCategoryId, @Bind("sampleKind") final String sampleKind);

    @SqlBatch
    @BatchChunkSize(1000)
    void bulkInsertSampleKinds(@CategoryIdAndSampleKindBinder Iterator<CategoryIdAndSampleKind> categoriesAndSampleKinds);

    @SqlQuery
    @Mapper(DefaultMapper.class)
    List<Map<String, Object>> getEventCategories();

    @SqlQuery
    @Mapper(DefaultMapper.class)
    List<Map<String, Object>> getSampleKinds();

    @SqlQuery
    int getLastInsertedId();

    @SqlQuery
    long getHighestTimelineChunkId();

    @SqlUpdate
    void insertTimelineChunk(@TimelineChunkBinder final TimelineChunk timelineChunk);

    @SqlBatch
    @BatchChunkSize(1000)
    void bulkInsertTimelineChunks(@TimelineChunkBinder Iterator<TimelineChunk> chunkIterator);

    @SqlUpdate
    Integer insertLastStartTimes(@StartTimesBinder final StartTimes startTimes);

    @SqlQuery
    StartTimes getLastStartTimes();

    @SqlUpdate
    void deleteLastStartTimes();

    @SqlUpdate
    void test();
}
