package org.bitbucket.xadkile.myide.common

import com.google.common.hash.HashCode
import com.google.common.hash.Hashing
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HMACMaker{
    /**
     * @dontuse
     */
    @Throws(Exception::class)
    fun calHmacSHA256(key: ByteArray, messageList:List<ByteArray>):ByteArray{
        val mac:Mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(key,"HmacSHA256")
        mac.init(keySpec)
        for(message in messageList){
            mac.update(message)
        }
        val rt = mac.doFinal()
        return rt
    }

    /**
     * TODO may be not use guava here.
     */
    fun makeHmacSha256Sig(key:ByteArray, elements:List<ByteArray>):HashCode{
        val hashFunction = Hashing.hmacSha256(key).newHasher()
        for(e in elements){
            hashFunction.putBytes(e)
        }
        val o:HashCode = hashFunction.hash()
        return o
    }

    fun makeHmacSha256SigInByteArray(key:ByteArray, elements:List<ByteArray>):ByteArray{
        return makeHmacSha256Sig(key,elements).toString().toByteArray(Charsets.UTF_8)
    }
}

