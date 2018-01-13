package com.edd.knug.game.player

import org.eclipse.jetty.websocket.api.Session

class PlayerRegistry(private val playerManager: PlayerManager) {

    // Session store can be non-thread safe as player management is handled only in the main thread.
    private val sessionStore = HashMap<Session, Player>()

    val sessions: Map<Session, Player>

    init {
        sessions = sessionStore
    }

    /**
     * Get a player by providing an active session.
     */
    operator fun get(session: Session) = sessions[session]

    /**
     * Register a session and create a player for that session.
     */
    fun add(session: Session): Player {
        return playerManager.create(
                session.upgradeRequest.parameterMap["name"]?.firstOrNull()
        ).also {
            sessionStore[session] = it
        }
    }

    /**
     * Remove session and related player.
     */
    fun remove(session: Session) = sessionStore.remove(session)?.also {
        playerManager.remove(it)
    }
}