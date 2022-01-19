/*
 * Copyright (c) 2022 New Vector Ltd
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

package im.vector.app

import android.view.View
import im.vector.app.features.home.room.detail.RoomDetailAction
import im.vector.app.features.home.room.detail.timeline.TimelineEventController
import im.vector.app.features.home.room.detail.timeline.item.MessageInformationData
import im.vector.app.features.home.room.detail.timeline.item.ReadReceiptData
import im.vector.app.features.media.ImageContentRenderer
import im.vector.app.features.media.VideoContentRenderer
import org.matrix.android.sdk.api.session.room.model.message.MessageAudioContent
import org.matrix.android.sdk.api.session.room.model.message.MessageImageInfoContent
import org.matrix.android.sdk.api.session.room.model.message.MessageVideoContent
import org.matrix.android.sdk.api.session.room.timeline.Timeline
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent

abstract class DefaultTimelineEventControllerCallback : TimelineEventController.Callback {
    override fun onLoadMore(direction: Timeline.Direction) {
    }

    override fun onEventInvisible(event: TimelineEvent) {
    }

    override fun onEventVisible(event: TimelineEvent) {
    }

    override fun onRoomCreateLinkClicked(url: String) {
    }

    override fun onEncryptedMessageClicked(informationData: MessageInformationData, view: View) {
    }

    override fun onImageMessageClicked(messageImageContent: MessageImageInfoContent, mediaData: ImageContentRenderer.Data, view: View) {
    }

    override fun onVideoMessageClicked(messageVideoContent: MessageVideoContent, mediaData: VideoContentRenderer.Data, view: View) {
    }

    override fun onEditedDecorationClicked(informationData: MessageInformationData) {
    }

    override fun onTimelineItemAction(itemAction: RoomDetailAction) {
    }

    override fun onVoiceControlButtonClicked(eventId: String, messageAudioContent: MessageAudioContent) {
    }

    override fun onEventCellClicked(informationData: MessageInformationData, messageContent: Any?, view: View) {
    }

    override fun onClickOnReactionPill(informationData: MessageInformationData, reaction: String, on: Boolean) {
    }

    override fun onLongClickOnReactionPill(informationData: MessageInformationData, reaction: String) {
    }

    override fun onAvatarClicked(informationData: MessageInformationData) {
    }

    override fun onMemberNameClicked(informationData: MessageInformationData) {
    }

    override fun onReadReceiptsClicked(readReceipts: List<ReadReceiptData>) {
    }

    override fun onReadMarkerVisible() {
    }

    override fun onPreviewUrlClicked(url: String) {
    }

    override fun onPreviewUrlCloseClicked(eventId: String, url: String) {
    }

    override fun onPreviewUrlImageClicked(sharedView: View?, mxcUrl: String?, title: String?) {
    }
}
