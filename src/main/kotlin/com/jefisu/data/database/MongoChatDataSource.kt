package com.jefisu.data.database

import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.data.model.Chat
import com.jefisu.data.model.Message
import com.jefisu.data.model.response.UserDto
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.CoroutineDatabase

class MongoChatDataSource(
    db: CoroutineDatabase
) : ChatDataSource {

    private val chats = db.getCollection<Chat>()

    override suspend fun startNewChat(senderId: String, recipientId: String): Chat {
        val existChat = chats.findOne(Chat::userIds.contains(senderId), Chat::userIds.contains(recipientId))
        if (existChat != null) {
            return existChat
        }

        val chat = Chat(
            userIds = listOf(senderId, recipientId),
            messages = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        chats.insertOne(chat)
        return chat
    }

    override suspend fun insertMessage(chat: Chat, message: Message) {
        val newList = chat.messages.toMutableList().apply {
            add(0, message)
        }
        chats.updateOneById(
            id = chat.id,
            update = chat.copy(messages = newList)
        )
    }

    override suspend fun getChatById(chatId: String): Chat? {
        return chats.findOneById(chatId)
    }

    override suspend fun getChatsByUser(userId: String): List<Chat> {
        return chats.find(Chat::userIds.contains(userId))
            .descendingSort()
            .toList()
    }

    override suspend fun deleteChat(chatId: String) {
        chats.deleteOneById(chatId)
    }
}