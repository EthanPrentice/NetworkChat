package com.ethanprentice.networkchat.adt.serialization

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ethanprentice.networkchat.util.HexConverter
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonOutput
import kotlinx.serialization.json.json
import java.io.*


object BitmapSerializer : KSerializer<Bitmap> {

    private val compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
    private const val compressQuality = 100

//    override val descriptor: SerialDescriptor = SerialDescriptor("Bitmap") {
//        element<Int>("buffer_length")
//        element<String>("data")
//    }

    override val descriptor = SerialClassDescImpl("Bitmap")

    @ImplicitReflectionSerializer
    override fun serialize(encoder: Encoder, obj: Bitmap) {
        val stream = ByteArrayOutputStream()
        obj.compress(compressFormat, compressQuality, stream)

        val byteArray = stream.toByteArray()

        val output = encoder as? JsonOutput ?: throw SerializationException("This class can be saved only by Json")
        output.encodeJson(json {
            "buffer_length" to byteArray.size
            "data" to HexConverter.printHexBinary(byteArray)
        })

//        val compositeOutput = encoder.beginStructure(descriptor)
//        compositeOutput.encodeIntElement(descriptor, 0, byteArray.size)
//        compositeOutput.encodeStringElement(descriptor, 1, HexConverter.printHexBinary(byteArray))
//        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Bitmap {
        val input = decoder as? JsonInput ?: throw SerializationException("This class can be read only by Json")
        val jsonObj = input.decodeJson() as? JsonObject ?: throw SerializationException("Expected JsonObject")

        val bufferLength: Int? = jsonObj.getPrimitiveOrNull("buffer_length")?.intOrNull
        val byteArrayStr: String? = jsonObj.getPrimitiveOrNull("data")?.contentOrNull

        if (bufferLength == null || byteArrayStr == null) {
            throw SerializationException("Unacceptable format, not able to decode")
        }

        val byteArray = HexConverter.parseHexBinary(byteArrayStr)
        val inStream = ByteArrayInputStream(byteArray)
        var pos = 0
        do {
            val read = inStream.read(byteArray, pos, bufferLength - pos)

            if (read != -1) {
                pos += read
            } else {
                break
            }

        } while (pos < bufferLength)

        return BitmapFactory.decodeByteArray(byteArray, 0, bufferLength)
    }
}
