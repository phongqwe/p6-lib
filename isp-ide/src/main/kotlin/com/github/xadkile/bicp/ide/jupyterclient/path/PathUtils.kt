package com.github.xadkile.bicp.ide.jupyterclient.path

import java.nio.file.Path

object PathUtils {
    /**
     * find subpath from a list of path
     */
    fun findSubPath(path: List<Path>, subdirs: List<Path>): List<Path> {
        val subPathList = path.flatMap { path ->
            subdirs.map { subdir ->
                path.resolve(subdir).toAbsolutePath()
            }
        }
        return subPathList
    }

//    /**
//     * also include the original paths
//     */
//    fun findSubPathInclude(path: List<Path>, subdirs: List<Path>): List<Path> {
//        return path + findSubPath(path,subdirs)
//    }
}
