package com.singularity.trackmyvehicle

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Imran Chowdhury on 2020-01-29.
 */


object EncryptionConstants {
    const val ENCRYPTION_IV = "c4b0493ad3867366"
    const val ENCRYPTION_KEY = "25cda6a3858b861169ed353a269aef2801cd48bbe22c9417ada387fbb25592849bb52d7923bf456f7465913614ee0169d9fe42fa0206d2248bdb85bdb2ba6bd8"
    const val ENCRYPTION_METHOD = "AES"
}


object AES {
    private var secretKey: SecretKeySpec? = null
    private var key: ByteArray? = null

    fun setKey(myKey: String) {
        try {
            key = myKey.toByteArray(charset("UTF-8"))
            key = key?.copyOf(16)
            secretKey = SecretKeySpec(key, EncryptionConstants.ENCRYPTION_METHOD)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun encrypt(strToEncrypt: String, secret: String): String {
        return try {
            setKey(secret)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(EncryptionConstants.ENCRYPTION_IV.toByteArray(charset("UTF-8"))))
            val encrypted = cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8")))
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            println("Error while encrypting: $e")
            e.message.toString()
        }
    }

}

fun String.toMD5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
    return bytes.toHex()
}
fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}