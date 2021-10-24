package org.bitbucket.xadkile.taiga.jupyterclient.kernel.spec

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KernelSpecTest{
    @Test
    fun fromJson(){
        val input = " {'argv': ['/home/abc/Applications/anaconda3/envs/dl_hw_01/bin/python', '-m', 'ipykernel_launcher', '-f', '{connection_file}'], 'display_name': 'Python 3', 'language': 'python'}"
        val gson = Gson()
        val out = gson.fromJson(input, KernelSpec::class.java)
        assertEquals("Python 3",out.displayName)
        assertEquals("python",out.language)
        assertEquals(listOf("/home/abc/Applications/anaconda3/envs/dl_hw_01/bin/python","-m","ipykernel_launcher","-f","{connection_file}"),out.argv)
    }
}
