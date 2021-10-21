package org.bitbucket.xadkile.taiga.jupyterclient.path

import java.nio.file.Path
import java.nio.file.Paths

object CommonPath {
    /**
     * path to home
     */
    val userHomePath: Path = Paths.get(System.getProperty("user.home")).toAbsolutePath()
    val xdgRuntimePath: Path? = System.getenv("XDG_RUNTIME_DIR")?.let { Paths.get(it).toAbsolutePath() }
    val xdgConfigPath: Path? = System.getenv("XDG_CONFIG_HOME ")?.let { Paths.get(it).toAbsolutePath() }
}

