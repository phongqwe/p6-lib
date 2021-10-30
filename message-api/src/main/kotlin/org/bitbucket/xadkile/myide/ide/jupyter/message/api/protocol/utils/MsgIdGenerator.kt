package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.SessionUUID
import javax.inject.Named
import javax.inject.Singleton

interface MsgIdGenerator {
    fun next():String
}
