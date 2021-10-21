package org.bitbucket.xadkile.taiga.jupyterclient.path

import java.nio.file.Path

/**
 * find relevant jupyter directories
 *  see for detail: https://jupyter.readthedocs.io/en/latest/use/jupyter-directories.html
 */
interface JupyterDirFinder {
    fun findRuntimeDir():Path
    fun findRuntimePath():List<Path>

    fun findDataDir(): Path
    fun findDataPath(subdirs:List<Path> = emptyList()): List<Path>

    fun findConfigDir(): Path
    fun findConfigPath(): List<Path>
}
