package com.edd.knug.player

import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session

class PlayerRegistry(private val playerManager: PlayerManager) {

    private val playerStore = HashMap<Session, Player>()
    private val remoteStore = HashMap<String, RemoteEndpoint>()

    fun getPlayer(session: Session) = playerStore[session]

    /**
     * @return list of registered players.
     */
    fun getPlayers() = playerStore.values

    /**
     * Register a player with a session and remote endpoint.
     *
     * @return registered player.
     */
    fun add(session: Session, remote: RemoteEndpoint): Player {
        return playerManager.create(
                session.upgradeRequest.parameterMap["name"]?.firstOrNull()
        ).also {
            playerStore[session] = it
            remoteStore[it.id] = remote
        }
    }

    /**
     * Remove player data related to a session.
     */
    fun remove(session: Session) {
        playerStore.remove(session)?.also {
            playerManager.remove(it)
            remoteStore.remove(it.id)
        }
    }

    /**
     * @return remote endpoint associated with the player.
     */
    fun getRemote(player: Player) = remoteStore[player.id]
}