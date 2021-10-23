package org.bitbucket.xadkile.taiga.jupyterclient.kernel

class KernelImp(val cf:KernelConnectionFileContent, val p : Process) : Kernel{
    override fun stop() {
        p.destroy()
    }

}
