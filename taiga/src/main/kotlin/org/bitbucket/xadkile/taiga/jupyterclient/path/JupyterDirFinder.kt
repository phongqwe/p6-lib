package org.bitbucket.xadkile.taiga.jupyterclient.path

import java.nio.file.Path

/**
 * find relevant jupyter directories
 *  see for detail: https://jupyter.readthedocs.io/en/latest/use/jupyter-directories.html
 */
interface JupyterDirFinder {
//    fun findAllConfigDirs():List<Path>
//    fun findAllRuntimeDirs():List<Path>
//    fun findAllDataDir():List<Path>

    fun findConfigPath():Path
    fun findRuntimePath():Path
    fun findDataPath():Path
}
