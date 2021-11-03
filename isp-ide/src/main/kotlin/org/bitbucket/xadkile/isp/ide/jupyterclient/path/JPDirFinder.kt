package org.bitbucket.xadkile.isp.ide.jupyterclient.path

import java.nio.file.Path

/**
 * find relevant jupyter directories
 *  see for detail: https://jupyter.readthedocs.io/en/latest/use/jupyter-directories.html
 */
interface JPDirFinder {
    fun findRuntimeDir(): Path
    fun findRuntimePath(): List<Path>

    fun findDataDir(): Path
    fun findDataPath(): List<Path>

    fun findConfigDir(): Path
    fun findConfigPath(): List<Path>

    fun findIPythonDir(): Path
}
