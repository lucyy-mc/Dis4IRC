/*
 * This file is part of Dis4IRC.
 *
 * Copyright (c) 2018-2021 Dis4IRC contributors
 *
 * MIT License
 */

package io.zachbr.dis4irc.bridge.pier.irc

import io.zachbr.dis4irc.bridge.message.BOT_SENDER
import io.zachbr.dis4irc.bridge.message.Message
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent
import org.kitteh.irc.client.library.event.channel.UnexpectedChannelLeaveViaKickEvent
import org.kitteh.irc.client.library.event.user.UserQuitEvent

class IrcJoinQuitListener(private val pier: IrcPier) {
    private val logger = pier.logger

    @Handler
    fun onUserJoinChan(event: ChannelJoinEvent) {
        // don't log our own joins
        if (event.user.nick == pier.getBotNick()) {
            return
        }

        val receiveTimestamp = System.nanoTime()
        logger.debug("IRC JOIN ${event.channel.name} ${event.user.nick}")

        val sender = BOT_SENDER
        val source = event.channel.asBridgeSource()
        val msgContent = "${event.user.nick} (${event.user.userString}@${event.user.host}) has joined ${event.channel.name}"
        val message = Message(msgContent, sender, source, receiveTimestamp)
        pier.sendToBridge(message)
    }

    @Handler
    fun onUserLeaveChan(event: ChannelPartEvent) {
        // don't log our own leaving
        if (event.user.nick == pier.getBotNick()) {
            return
        }

        val receiveTimestamp = System.nanoTime()
        logger.debug("IRC PART ${event.channel.name} ${event.user.nick}")

        val sender = BOT_SENDER
        val source = event.channel.asBridgeSource()
        val msgContent = "${event.user.nick} (${event.user.userString}@${event.user.host}) has left ${event.channel.name}"
        val message = Message(msgContent, sender, source, receiveTimestamp)
        pier.sendToBridge(message)
    }

    @Handler
    fun onUserKicked(event: UnexpectedChannelLeaveViaKickEvent) {
        // don't log our own quitting
        if (event.user.nick == pier.getBotNick()) {
            return
        }

        val receiveTimestamp = System.nanoTime()
        logger.debug("IRC KICK ${event.channel.name} ${event.target.nick} by ${event.user.nick}")

        val sender = BOT_SENDER
        val source = event.channel.asBridgeSource()
        val msgContent = "${event.user.nick} kicked ${event.target.nick} (${event.target.userString}@${event.target.host}) (${event.message})"
        val message = Message(msgContent, sender, source, receiveTimestamp)
        pier.sendToBridge(message)
    }

    @Handler
    fun onUserQuit(event: UserQuitEvent) {
        val receiveTimestamp = System.nanoTime()
        val sender = BOT_SENDER
        val msgContent = "${event.user.nick} (${event.user.userString}@${event.user.host}) has quit"
        logger.debug("IRC QUIT ${event.user.nick}")

        for (channel in event.user.channels) {
            val chan = event.client.getChannel(channel).toNullable() ?: continue

            val source = chan.asBridgeSource()
            val message = Message(msgContent, sender, source, receiveTimestamp)
            pier.sendToBridge(message)
        }
    }
}
