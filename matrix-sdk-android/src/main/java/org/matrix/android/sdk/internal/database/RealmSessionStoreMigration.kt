/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.android.sdk.internal.database

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomJoinRulesContent
import org.matrix.android.sdk.api.session.room.model.VersioningState
import org.matrix.android.sdk.api.session.room.model.create.RoomCreateContent
import org.matrix.android.sdk.api.session.room.model.tag.RoomTag
import org.matrix.android.sdk.api.session.threads.ThreadNotificationState
import org.matrix.android.sdk.internal.crypto.model.event.EncryptionEventContent
import org.matrix.android.sdk.internal.database.model.ChunkEntityFields
import org.matrix.android.sdk.internal.database.model.CurrentStateEventEntityFields
import org.matrix.android.sdk.internal.database.model.EditAggregatedSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.EditionOfEventFields
import org.matrix.android.sdk.internal.database.model.EventEntityFields
import org.matrix.android.sdk.internal.database.model.EventInsertEntityFields
import org.matrix.android.sdk.internal.database.model.HomeServerCapabilitiesEntityFields
import org.matrix.android.sdk.internal.database.model.PendingThreePidEntityFields
import org.matrix.android.sdk.internal.database.model.PreviewUrlCacheEntityFields
import org.matrix.android.sdk.internal.database.model.RoomAccountDataEntityFields
import org.matrix.android.sdk.internal.database.model.RoomEntityFields
import org.matrix.android.sdk.internal.database.model.RoomMemberSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.RoomMembersLoadStatusType
import org.matrix.android.sdk.internal.database.model.RoomSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.RoomTagEntityFields
import org.matrix.android.sdk.internal.database.model.SpaceChildSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.SpaceParentSummaryEntityFields
import org.matrix.android.sdk.internal.database.model.TimelineEventEntityFields
import org.matrix.android.sdk.internal.database.model.presence.UserPresenceEntityFields
import org.matrix.android.sdk.internal.di.MoshiProvider
import org.matrix.android.sdk.internal.query.process
import org.matrix.android.sdk.internal.util.Normalizer
import timber.log.Timber
import javax.inject.Inject

internal class RealmSessionStoreMigration @Inject constructor(
        private val normalizer: Normalizer
) : RealmMigration {

    companion object {
        const val SESSION_STORE_SCHEMA_VERSION = 24L
    }

    /**
     * Forces all RealmSessionStoreMigration instances to be equal
     * Avoids Realm throwing when multiple instances of the migration are set
     */
    override fun equals(other: Any?) = other is RealmSessionStoreMigration
    override fun hashCode() = 1000

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        Timber.v("Migrating Realm Session from $oldVersion to $newVersion")

        if (oldVersion <= 0) migrateTo1(realm)
        if (oldVersion <= 1) migrateTo2(realm)
        if (oldVersion <= 2) migrateTo3(realm)
        if (oldVersion <= 3) migrateTo4(realm)
        if (oldVersion <= 4) migrateTo5(realm)
        if (oldVersion <= 5) migrateTo6(realm)
        if (oldVersion <= 6) migrateTo7(realm)
        if (oldVersion <= 7) migrateTo8(realm)
        if (oldVersion <= 8) migrateTo9(realm)
        if (oldVersion <= 9) migrateTo10(realm)
        if (oldVersion <= 10) migrateTo11(realm)
        if (oldVersion <= 11) migrateTo12(realm)
        if (oldVersion <= 12) migrateTo13(realm)
        if (oldVersion <= 13) migrateTo14(realm)
        if (oldVersion <= 14) migrateTo15(realm)
        if (oldVersion <= 15) migrateTo16(realm)
        if (oldVersion <= 16) migrateTo17(realm)
        if (oldVersion <= 17) migrateTo18(realm)
        if (oldVersion <= 18) migrateTo19(realm)
        if (oldVersion <= 19) migrateTo20(realm)
        if (oldVersion <= 20) migrateTo21(realm)
        if (oldVersion <= 21) migrateTo22(realm)
        if (oldVersion <= 22) migrateTo23(realm)
        if (oldVersion <= 23) migrateTo24(realm)
    }

    private fun migrateTo1(realm: DynamicRealm) {
        Timber.d("Step 0 -> 1")
        // Add hasFailedSending in RoomSummary and a small warning icon on room list

        realm.schema.get("RoomSummaryEntity")
                ?.addField(RoomSummaryEntityFields.HAS_FAILED_SENDING, Boolean::class.java)
                ?.transform { obj ->
                    obj.setBoolean(RoomSummaryEntityFields.HAS_FAILED_SENDING, false)
                }
    }

    private fun migrateTo2(realm: DynamicRealm) {
        Timber.d("Step 1 -> 2")
        realm.schema.get("HomeServerCapabilitiesEntity")
                ?.addField("adminE2EByDefault", Boolean::class.java)
                ?.transform { obj ->
                    obj.setBoolean("adminE2EByDefault", true)
                }
    }

    private fun migrateTo3(realm: DynamicRealm) {
        Timber.d("Step 2 -> 3")
        realm.schema.get("HomeServerCapabilitiesEntity")
                ?.addField("preferredJitsiDomain", String::class.java)
                ?.transform { obj ->
                    // Schedule a refresh of the capabilities
                    obj.setLong(HomeServerCapabilitiesEntityFields.LAST_UPDATED_TIMESTAMP, 0)
                }
    }

    private fun migrateTo4(realm: DynamicRealm) {
        Timber.d("Step 3 -> 4")
        realm.schema.create("PendingThreePidEntity")
                .addField(PendingThreePidEntityFields.CLIENT_SECRET, String::class.java)
                .setRequired(PendingThreePidEntityFields.CLIENT_SECRET, true)
                .addField(PendingThreePidEntityFields.EMAIL, String::class.java)
                .addField(PendingThreePidEntityFields.MSISDN, String::class.java)
                .addField(PendingThreePidEntityFields.SEND_ATTEMPT, Int::class.java)
                .addField(PendingThreePidEntityFields.SID, String::class.java)
                .setRequired(PendingThreePidEntityFields.SID, true)
                .addField(PendingThreePidEntityFields.SUBMIT_URL, String::class.java)
    }

    private fun migrateTo5(realm: DynamicRealm) {
        Timber.d("Step 4 -> 5")
        realm.schema.get("HomeServerCapabilitiesEntity")
                ?.removeField("adminE2EByDefault")
                ?.removeField("preferredJitsiDomain")
    }

    private fun migrateTo6(realm: DynamicRealm) {
        Timber.d("Step 5 -> 6")
        realm.schema.create("PreviewUrlCacheEntity")
                .addField(PreviewUrlCacheEntityFields.URL, String::class.java)
                .setRequired(PreviewUrlCacheEntityFields.URL, true)
                .addPrimaryKey(PreviewUrlCacheEntityFields.URL)
                .addField(PreviewUrlCacheEntityFields.URL_FROM_SERVER, String::class.java)
                .addField(PreviewUrlCacheEntityFields.SITE_NAME, String::class.java)
                .addField(PreviewUrlCacheEntityFields.TITLE, String::class.java)
                .addField(PreviewUrlCacheEntityFields.DESCRIPTION, String::class.java)
                .addField(PreviewUrlCacheEntityFields.MXC_URL, String::class.java)
                .addField(PreviewUrlCacheEntityFields.LAST_UPDATED_TIMESTAMP, Long::class.java)
    }

    private fun migrateTo7(realm: DynamicRealm) {
        Timber.d("Step 6 -> 7")
        realm.schema.get("RoomEntity")
                ?.addField(RoomEntityFields.MEMBERS_LOAD_STATUS_STR, String::class.java)
                ?.transform { obj ->
                    if (obj.getBoolean("areAllMembersLoaded")) {
                        obj.setString("membersLoadStatusStr", RoomMembersLoadStatusType.LOADED.name)
                    } else {
                        obj.setString("membersLoadStatusStr", RoomMembersLoadStatusType.NONE.name)
                    }
                }
                ?.removeField("areAllMembersLoaded")
    }

    private fun migrateTo8(realm: DynamicRealm) {
        Timber.d("Step 7 -> 8")

        val editionOfEventSchema = realm.schema.create("EditionOfEvent")
                .addField(EditionOfEventFields.CONTENT, String::class.java)
                .addField(EditionOfEventFields.EVENT_ID, String::class.java)
                .setRequired(EditionOfEventFields.EVENT_ID, true)
                .addField(EditionOfEventFields.SENDER_ID, String::class.java)
                .setRequired(EditionOfEventFields.SENDER_ID, true)
                .addField(EditionOfEventFields.TIMESTAMP, Long::class.java)
                .addField(EditionOfEventFields.IS_LOCAL_ECHO, Boolean::class.java)

        realm.schema.get("EditAggregatedSummaryEntity")
                ?.removeField("aggregatedContent")
                ?.removeField("sourceEvents")
                ?.removeField("lastEditTs")
                ?.removeField("sourceLocalEchoEvents")
                ?.addRealmListField(EditAggregatedSummaryEntityFields.EDITIONS.`$`, editionOfEventSchema)

        // This has to be done once a parent use the model as a child
        // See https://github.com/realm/realm-java/issues/7402
        editionOfEventSchema.isEmbedded = true
    }

    private fun migrateTo9(realm: DynamicRealm) {
        Timber.d("Step 8 -> 9")

        realm.schema.get("RoomSummaryEntity")
                ?.addField(RoomSummaryEntityFields.LAST_ACTIVITY_TIME, Long::class.java, FieldAttribute.INDEXED)
                ?.setNullable(RoomSummaryEntityFields.LAST_ACTIVITY_TIME, true)
                ?.addIndex(RoomSummaryEntityFields.MEMBERSHIP_STR)
                ?.addIndex(RoomSummaryEntityFields.IS_DIRECT)
                ?.addIndex(RoomSummaryEntityFields.VERSIONING_STATE_STR)

                ?.addField(RoomSummaryEntityFields.IS_FAVOURITE, Boolean::class.java)
                ?.addIndex(RoomSummaryEntityFields.IS_FAVOURITE)
                ?.addField(RoomSummaryEntityFields.IS_LOW_PRIORITY, Boolean::class.java)
                ?.addIndex(RoomSummaryEntityFields.IS_LOW_PRIORITY)
                ?.addField(RoomSummaryEntityFields.IS_SERVER_NOTICE, Boolean::class.java)
                ?.addIndex(RoomSummaryEntityFields.IS_SERVER_NOTICE)

                ?.transform { obj ->
                    val isFavorite = obj.getList(RoomSummaryEntityFields.TAGS.`$`).any {
                        it.getString(RoomTagEntityFields.TAG_NAME) == RoomTag.ROOM_TAG_FAVOURITE
                    }
                    obj.setBoolean(RoomSummaryEntityFields.IS_FAVOURITE, isFavorite)

                    val isLowPriority = obj.getList(RoomSummaryEntityFields.TAGS.`$`).any {
                        it.getString(RoomTagEntityFields.TAG_NAME) == RoomTag.ROOM_TAG_LOW_PRIORITY
                    }

                    obj.setBoolean(RoomSummaryEntityFields.IS_LOW_PRIORITY, isLowPriority)

//                    XXX migrate last message origin server ts
                    obj.getObject(RoomSummaryEntityFields.LATEST_PREVIEWABLE_EVENT.`$`)
                            ?.getObject(TimelineEventEntityFields.ROOT.`$`)
                            ?.getLong(EventEntityFields.ORIGIN_SERVER_TS)?.let {
                                obj.setLong(RoomSummaryEntityFields.LAST_ACTIVITY_TIME, it)
                            }
                }
    }

    private fun migrateTo10(realm: DynamicRealm) {
        Timber.d("Step 9 -> 10")
        realm.schema.create("SpaceChildSummaryEntity")
                ?.addField(SpaceChildSummaryEntityFields.ORDER, String::class.java)
                ?.addField(SpaceChildSummaryEntityFields.CHILD_ROOM_ID, String::class.java)
                ?.addField(SpaceChildSummaryEntityFields.AUTO_JOIN, Boolean::class.java)
                ?.setNullable(SpaceChildSummaryEntityFields.AUTO_JOIN, true)
                ?.addRealmObjectField(SpaceChildSummaryEntityFields.CHILD_SUMMARY_ENTITY.`$`, realm.schema.get("RoomSummaryEntity")!!)
                ?.addRealmListField(SpaceChildSummaryEntityFields.VIA_SERVERS.`$`, String::class.java)

        realm.schema.create("SpaceParentSummaryEntity")
                ?.addField(SpaceParentSummaryEntityFields.PARENT_ROOM_ID, String::class.java)
                ?.addField(SpaceParentSummaryEntityFields.CANONICAL, Boolean::class.java)
                ?.setNullable(SpaceParentSummaryEntityFields.CANONICAL, true)
                ?.addRealmObjectField(SpaceParentSummaryEntityFields.PARENT_SUMMARY_ENTITY.`$`, realm.schema.get("RoomSummaryEntity")!!)
                ?.addRealmListField(SpaceParentSummaryEntityFields.VIA_SERVERS.`$`, String::class.java)

        val creationContentAdapter = MoshiProvider.providesMoshi().adapter(RoomCreateContent::class.java)
        realm.schema.get("RoomSummaryEntity")
                ?.addField(RoomSummaryEntityFields.ROOM_TYPE, String::class.java)
                ?.addField(RoomSummaryEntityFields.FLATTEN_PARENT_IDS, String::class.java)
                ?.addField(RoomSummaryEntityFields.GROUP_IDS, String::class.java)
                ?.transform { obj ->

                    val creationEvent = realm.where("CurrentStateEventEntity")
                            .equalTo(CurrentStateEventEntityFields.ROOM_ID, obj.getString(RoomSummaryEntityFields.ROOM_ID))
                            .equalTo(CurrentStateEventEntityFields.TYPE, EventType.STATE_ROOM_CREATE)
                            .findFirst()

                    val roomType = creationEvent?.getObject(CurrentStateEventEntityFields.ROOT.`$`)
                            ?.getString(EventEntityFields.CONTENT)?.let {
                                creationContentAdapter.fromJson(it)?.type
                            }

                    obj.setString(RoomSummaryEntityFields.ROOM_TYPE, roomType)
                }
                ?.addRealmListField(RoomSummaryEntityFields.PARENTS.`$`, realm.schema.get("SpaceParentSummaryEntity")!!)
                ?.addRealmListField(RoomSummaryEntityFields.CHILDREN.`$`, realm.schema.get("SpaceChildSummaryEntity")!!)
    }

    private fun migrateTo11(realm: DynamicRealm) {
        Timber.d("Step 10 -> 11")
        realm.schema.get("EventEntity")
                ?.addField(EventEntityFields.SEND_STATE_DETAILS, String::class.java)
    }

    private fun migrateTo12(realm: DynamicRealm) {
        Timber.d("Step 11 -> 12")

        val joinRulesContentAdapter = MoshiProvider.providesMoshi().adapter(RoomJoinRulesContent::class.java)
        realm.schema.get("RoomSummaryEntity")
                ?.addField(RoomSummaryEntityFields.JOIN_RULES_STR, String::class.java)
                ?.transform { obj ->
                    val joinRulesEvent = realm.where("CurrentStateEventEntity")
                            .equalTo(CurrentStateEventEntityFields.ROOM_ID, obj.getString(RoomSummaryEntityFields.ROOM_ID))
                            .equalTo(CurrentStateEventEntityFields.TYPE, EventType.STATE_ROOM_JOIN_RULES)
                            .findFirst()

                    val roomJoinRules = joinRulesEvent?.getObject(CurrentStateEventEntityFields.ROOT.`$`)
                            ?.getString(EventEntityFields.CONTENT)?.let {
                                joinRulesContentAdapter.fromJson(it)?.joinRules
                            }

                    obj.setString(RoomSummaryEntityFields.JOIN_RULES_STR, roomJoinRules?.name)
                }

        realm.schema.get("SpaceChildSummaryEntity")
                ?.addField(SpaceChildSummaryEntityFields.SUGGESTED, Boolean::class.java)
                ?.setNullable(SpaceChildSummaryEntityFields.SUGGESTED, true)
    }

    private fun migrateTo13(realm: DynamicRealm) {
        Timber.d("Step 12 -> 13")
        // Fix issue with the nightly build. Eventually play again the migration which has been included in migrateTo12()
        realm.schema.get("SpaceChildSummaryEntity")
                ?.takeIf { !it.hasField(SpaceChildSummaryEntityFields.SUGGESTED) }
                ?.addField(SpaceChildSummaryEntityFields.SUGGESTED, Boolean::class.java)
                ?.setNullable(SpaceChildSummaryEntityFields.SUGGESTED, true)
    }

    private fun migrateTo14(realm: DynamicRealm) {
        Timber.d("Step 13 -> 14")
        val roomAccountDataSchema = realm.schema.create("RoomAccountDataEntity")
                .addField(RoomAccountDataEntityFields.CONTENT_STR, String::class.java)
                .addField(RoomAccountDataEntityFields.TYPE, String::class.java, FieldAttribute.INDEXED)

        realm.schema.get("RoomEntity")
                ?.addRealmListField(RoomEntityFields.ACCOUNT_DATA.`$`, roomAccountDataSchema)

        realm.schema.get("RoomSummaryEntity")
                ?.addField(RoomSummaryEntityFields.IS_HIDDEN_FROM_USER, Boolean::class.java, FieldAttribute.INDEXED)
                ?.transform {
                    val isHiddenFromUser = it.getString(RoomSummaryEntityFields.VERSIONING_STATE_STR) == VersioningState.UPGRADED_ROOM_JOINED.name
                    it.setBoolean(RoomSummaryEntityFields.IS_HIDDEN_FROM_USER, isHiddenFromUser)
                }

        roomAccountDataSchema.isEmbedded = true
    }

    private fun migrateTo15(realm: DynamicRealm) {
        Timber.d("Step 14 -> 15")
        // fix issue with flattenParentIds on DM that kept growing with duplicate
        // so we reset it, will be updated next sync
        realm.where("RoomSummaryEntity")
                .process(RoomSummaryEntityFields.MEMBERSHIP_STR, Membership.activeMemberships())
                .equalTo(RoomSummaryEntityFields.IS_DIRECT, true)
                .findAll()
                .onEach {
                    it.setString(RoomSummaryEntityFields.FLATTEN_PARENT_IDS, null)
                }
    }

    private fun migrateTo16(realm: DynamicRealm) {
        Timber.d("Step 15 -> 16")
        realm.schema.get("HomeServerCapabilitiesEntity")
                ?.addField(HomeServerCapabilitiesEntityFields.ROOM_VERSIONS_JSON, String::class.java)
                ?.transform { obj ->
                    // Schedule a refresh of the capabilities
                    obj.setLong(HomeServerCapabilitiesEntityFields.LAST_UPDATED_TIMESTAMP, 0)
                }
    }

    private fun migrateTo17(realm: DynamicRealm) {
        Timber.d("Step 16 -> 17")
        realm.schema.get("EventInsertEntity")
                ?.addField(EventInsertEntityFields.CAN_BE_PROCESSED, Boolean::class.java)
    }

    private fun migrateTo18(realm: DynamicRealm) {
        Timber.d("Step 17 -> 18")
        realm.schema.create("UserPresenceEntity")
                ?.addField(UserPresenceEntityFields.USER_ID, String::class.java)
                ?.addPrimaryKey(UserPresenceEntityFields.USER_ID)
                ?.setRequired(UserPresenceEntityFields.USER_ID, true)
                ?.addField(UserPresenceEntityFields.PRESENCE_STR, String::class.java)
                ?.addField(UserPresenceEntityFields.LAST_ACTIVE_AGO, Long::class.java)
                ?.setNullable(UserPresenceEntityFields.LAST_ACTIVE_AGO, true)
                ?.addField(UserPresenceEntityFields.STATUS_MESSAGE, String::class.java)
                ?.addField(UserPresenceEntityFields.IS_CURRENTLY_ACTIVE, Boolean::class.java)
                ?.setNullable(UserPresenceEntityFields.IS_CURRENTLY_ACTIVE, true)
                ?.addField(UserPresenceEntityFields.AVATAR_URL, String::class.java)
                ?.addField(UserPresenceEntityFields.DISPLAY_NAME, String::class.java)

        val userPresenceEntity = realm.schema.get("UserPresenceEntity") ?: return
        realm.schema.get("RoomSummaryEntity")
                ?.addRealmObjectField(RoomSummaryEntityFields.DIRECT_USER_PRESENCE.`$`, userPresenceEntity)

        realm.schema.get("RoomMemberSummaryEntity")
                ?.addRealmObjectField(RoomMemberSummaryEntityFields.USER_PRESENCE_ENTITY.`$`, userPresenceEntity)
    }

    private fun migrateTo19(realm: DynamicRealm) {
        Timber.d("Step 18 -> 19")
        realm.schema.get("RoomSummaryEntity")
                ?.addField(RoomSummaryEntityFields.NORMALIZED_DISPLAY_NAME, String::class.java)
                ?.transform {
                    it.getString(RoomSummaryEntityFields.DISPLAY_NAME)?.let { displayName ->
                        val normalised = normalizer.normalize(displayName)
                        it.set(RoomSummaryEntityFields.NORMALIZED_DISPLAY_NAME, normalised)
                    }
                }
    }

    private fun migrateTo20(realm: DynamicRealm) {
        Timber.d("Step 19 -> 20")

        realm.schema.get("ChunkEntity")?.apply {
            if (hasField("numberOfTimelineEvents")) {
                removeField("numberOfTimelineEvents")
            }
            var cleanOldChunks = false
            if (!hasField(ChunkEntityFields.NEXT_CHUNK.`$`)) {
                cleanOldChunks = true
                addRealmObjectField(ChunkEntityFields.NEXT_CHUNK.`$`, this)
            }
            if (!hasField(ChunkEntityFields.PREV_CHUNK.`$`)) {
                cleanOldChunks = true
                addRealmObjectField(ChunkEntityFields.PREV_CHUNK.`$`, this)
            }
            if (cleanOldChunks) {
                val chunkEntities = realm.where("ChunkEntity").equalTo(ChunkEntityFields.IS_LAST_FORWARD, false).findAll()
                chunkEntities.deleteAllFromRealm()
            }
        }
    }

    private fun migrateTo21(realm: DynamicRealm) {
        Timber.d("Step 20 -> 21")

        realm.schema.get("RoomSummaryEntity")
                ?.addField(RoomSummaryEntityFields.E2E_ALGORITHM, String::class.java)
                ?.transform { obj ->

                    val encryptionContentAdapter = MoshiProvider.providesMoshi().adapter(EncryptionEventContent::class.java)

                    val encryptionEvent = realm.where("CurrentStateEventEntity")
                            .equalTo(CurrentStateEventEntityFields.ROOM_ID, obj.getString(RoomSummaryEntityFields.ROOM_ID))
                            .equalTo(CurrentStateEventEntityFields.TYPE, EventType.STATE_ROOM_ENCRYPTION)
                            .findFirst()

                    val encryptionEventRoot = encryptionEvent?.getObject(CurrentStateEventEntityFields.ROOT.`$`)
                    val algorithm = encryptionEventRoot
                            ?.getString(EventEntityFields.CONTENT)?.let {
                                encryptionContentAdapter.fromJson(it)?.algorithm
                            }

                    obj.setString(RoomSummaryEntityFields.E2E_ALGORITHM, algorithm)
                    obj.setBoolean(RoomSummaryEntityFields.IS_ENCRYPTED, encryptionEvent != null)
                    encryptionEventRoot?.getLong(EventEntityFields.ORIGIN_SERVER_TS)?.let {
                        obj.setLong(RoomSummaryEntityFields.ENCRYPTION_EVENT_TS, it)
                    }
                }
    }

    private fun migrateTo22(realm: DynamicRealm) {
        Timber.d("Step 21 -> 22")
        val listJoinedRoomIds = realm.where("RoomEntity")
                .equalTo(RoomEntityFields.MEMBERSHIP_STR, Membership.JOIN.name).findAll()
                .map { it.getString(RoomEntityFields.ROOM_ID) }

        val hasMissingStateEvent = realm.where("CurrentStateEventEntity")
                .`in`(CurrentStateEventEntityFields.ROOM_ID, listJoinedRoomIds.toTypedArray())
                .isNull(CurrentStateEventEntityFields.ROOT.`$`).findFirst() != null

        if (hasMissingStateEvent) {
            Timber.v("Has some missing state event, clear session cache")
            realm.deleteAll()
        }
    }

    private fun migrateTo23(realm: DynamicRealm) {
        Timber.d("Step 22 -> 23")
        val eventEntity = realm.schema.get("TimelineEventEntity") ?: return

        realm.schema.get("EventEntity")
                ?.addField(EventEntityFields.IS_ROOT_THREAD, Boolean::class.java, FieldAttribute.INDEXED)
                ?.addField(EventEntityFields.ROOT_THREAD_EVENT_ID, String::class.java, FieldAttribute.INDEXED)
                ?.addField(EventEntityFields.NUMBER_OF_THREADS, Int::class.java)
                ?.addField(EventEntityFields.THREAD_NOTIFICATION_STATE_STR, String::class.java)
                ?.transform {
                    it.setString(EventEntityFields.THREAD_NOTIFICATION_STATE_STR, ThreadNotificationState.NO_NEW_MESSAGE.name)
                }
                ?.addRealmObjectField(EventEntityFields.THREAD_SUMMARY_LATEST_MESSAGE.`$`, eventEntity)
    }

    private fun migrateTo24(realm: DynamicRealm) {
        Timber.d("Step 23 -> 24")
        realm.schema.get("PreviewUrlCacheEntity")
                ?.addField(PreviewUrlCacheEntityFields.IMAGE_WIDTH, Int::class.java)
                ?.setNullable(PreviewUrlCacheEntityFields.IMAGE_WIDTH, true)
                ?.addField(PreviewUrlCacheEntityFields.IMAGE_HEIGHT, Int::class.java)
                ?.setNullable(PreviewUrlCacheEntityFields.IMAGE_HEIGHT, true)
    }
}
