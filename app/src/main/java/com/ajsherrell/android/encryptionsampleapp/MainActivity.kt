package com.ajsherrell.android.encryptionsampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.ajsherrell.android.encryptionsampleapp.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher

private const val CRYPTO_METHOD = "RSA"
private const val CRYPTO_BITS = 2048

class MainActivity : AppCompatActivity() {
    private lateinit var editText: TextInputEditText
    private lateinit var fromTextView: TextView
    private lateinit var clearButton: Button
    private lateinit var encryptButton: Button
    private lateinit var decryptButton: Button
    private var publicKey: RSAPublicKey? = null
    var privateKey: RSAPrivateKey? = null
    private lateinit var encryptedBytes: ByteArray
    private lateinit var decryptedBytes: ByteArray
    private var toMessage: ObservableField<String> = ObservableField("")
    private var displayedMessage: ObservableField<String> = ObservableField("Show result here.")
    private var fromMessage: ObservableField<String> = ObservableField("")
    var isEncrypted = ObservableBoolean(false)

    init {
        generateKeyPair()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )
        editText = binding.editMessage
        clearButton = binding.clearText
        encryptButton = binding.encryptButton
        decryptButton = binding.decryptButton
        fromTextView = binding.fromMessage
        fromTextView.text = displayedMessage.get()

        toMessage.set(editText.text.toString())
        encryptButton.setOnClickListener {
            encryptThis()
            fromTextView.text = displayedMessage.get()
        }

        fromMessage.set(fromTextView.text.toString())
        decryptButton.setOnClickListener {
            decryptThis()
            fromTextView.text = displayedMessage.get()
        }

        clearButton.setOnClickListener {
            editText.text?.clear()
            fromTextView.text = getString(R.string.clear_result)
            displayedMessage.set("")
            fromMessage.set("")
            toMessage.set("")
        }
    }

    private fun encryptThis() {
        val messageToEncrypt = editText.text.toString()
        isEncrypted.set(true)
        displayedMessage.set(encrypt(messageToEncrypt))
        Log.i(this.toString(), "Message is: Start--- ${displayedMessage.get()} ---End")
    }

    private fun decryptThis() {
        val messageToDecrypt = fromTextView.text.toString()
        isEncrypted.set(false)
        displayedMessage.set(decrypt(messageToDecrypt))
        Log.i(this.toString(), "Message is: Start--- ${displayedMessage.get()} ---End")
    }

    private fun generateKeyPair() {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(CRYPTO_METHOD)
        val kp: KeyPair = kpg.genKeyPair()
        kpg.initialize(CRYPTO_BITS)
        publicKey = kp.public as RSAPublicKey?
        privateKey = kp.private as RSAPrivateKey?
    }

    private fun encrypt(toCipher: String?): String {
        try {
            if (toCipher.isNullOrEmpty()) return "Not a string!"
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            encryptedBytes = cipher.doFinal(toCipher.toByteArray(StandardCharsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.message?.let{ Log.e("encryptor", it) }
        }
        return "null"
    }

    private fun decrypt(result: String?): String {
        try {
            if (result.isNullOrEmpty()) return "Not a returned string!"
            val bytes = Base64.decode(result, Base64.DEFAULT)
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            decryptedBytes = cipher.doFinal(bytes)
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.message?.let{ Log.e("decryptor", it) }
        }
        return "null"
    }
}

