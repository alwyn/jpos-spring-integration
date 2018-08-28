package org.`as`.jpos.spring.integration.serializer

import org.jpos.iso.ISOMsg
import org.jpos.iso.packager.XML2003Packager
import org.jpos.iso.packager.XMLPackager
import org.springframework.core.serializer.Deserializer
import org.springframework.core.serializer.Serializer
import java.io.BufferedReader
import java.io.EOFException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

object Xml2003ChannelSerializer : Serializer<ISOMsg>, Deserializer<ISOMsg> {

    val packager by lazy { XML2003Packager() }

    override fun deserialize(inputStream: InputStream): ISOMsg {
        val br = BufferedReader(InputStreamReader(inputStream))
        var sp = 0
        val sb = StringBuilder()
        while (br != null) {
            val s = br.readLine() ?: throw EOFException()
            sb.append(s)
            if (s.contains("<" + XMLPackager.ISOMSG_TAG))
                sp++
            if (s.contains("</" + XMLPackager.ISOMSG_TAG + ">") && --sp <= 0)
                break
        }
        val msg = packager.createISOMsg()
        msg.packager = packager
        msg.unpack(sb.toString().toByteArray())
        return msg
    }

    override fun serialize(msg: ISOMsg, outputStream: OutputStream) {
        msg.direction = ISOMsg.OUTGOING
        msg.packager = this.packager
        val bytes = msg.pack()
        outputStream.write(bytes)
        outputStream.flush()
    }

}
