package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgStatus


/**
[
kernel.bef3811d-3af2-4cf7-a307-1820169ce9c1.execute_result,
<IDS|MSG>,

b9818656de3fc42e5df3c0588d8cd9a0d6ff5b3f819b15ac44a3df784d977d33,  [HMAC Sig]

{"msg_id":"b47b2b8b-885b33974d603331be94e061_3","msg_type":"execute_result","username":"abc","session":"b47b2b8b-885b33974d603331be94e061","date":"2021-10-28T02:12:06.972997Z","version":"5.3"}

, {"msg_id":"c61cdd62-932e-4fa1-ade0-4f8e7d5520b0_1","msg_type":"execute_request","username":"abc","session":"c61cdd62-932e-4fa1-ade0-4f8e7d5520b0","date":"2021-10-28T02:12:06.000772Z","version":"5.3"}

,{},

{"data":{"text/plain":"6"},"metadata":{},"execution_count":1}]
 */
class ShellCodeExecutionResult {
    class Facade(
        val status: MsgStatus,
        val execution_count:Int,
        val payload:List<Any>,
        val user_expressions:Map<String,String>
    )

    class Payload(
        val source:String,
        val data:Any,
        val start:Int
    )
}
