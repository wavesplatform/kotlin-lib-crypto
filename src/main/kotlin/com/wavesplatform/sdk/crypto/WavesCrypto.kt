package com.wavesplatform.sdk.crypto

import org.apache.commons.codec.binary.Base64
import java.util.*

typealias Bytes = ByteArray
typealias PublicKey = String
typealias PrivateKey = String
typealias Seed = String
typealias Address = String

interface KeyPair {
    val publicKey: PublicKey
    val privateKey: PrivateKey
}

/**
 * Collection of functions to work with Waves basic types and crypto primitives used by Waves
 */
interface WavesCrypto {

    /**
     * BLAKE2 are cryptographic hash function
     *
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun blake2b(input: Bytes): Bytes

    /**
     * Keccak are secure hash algorithm
     *
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun keccak(input: Bytes): Bytes

    /**
     * SHA-256 are cryptographic hash function
     *
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun sha256(input: Bytes): Bytes

    /**
     * Base58 binary-to-text encoding function used to represent large integers as alphanumeric text.
     * Compared to Base64 like in base64encode(), the following similar-looking letters are omitted:
     * 0 (zero), O (capital o), I (capital i) and l (lower case L) as well
     * as the non-alphanumeric characters + (plus) and / (slash)
     *
     * @param input byte array containing binary data to encode
     * @return encoded string containing Base58 characters
     */
    fun base58encode(input: Bytes): String

    /**
     * Base58 text-to-binary function used to restore data encoded by Base58,
     * reverse of base58encode()
     *
     * @param input encoded Base58 string
     * @return decoded byte array
     */
    fun base58decode(input: String): Bytes

    /**
     *  Base64 binary-to-text encoding function used to represent binary data in an ASCII
     *  string format by translating it into a radix-64 representation.
     *  The implementation uses A–Z, a–z, and 0–9 for the first 62 values and '+', '/'
     *
     *  @param input byte array containing binary data to encode.
     *  @return String containing Base64 characters
     */
    fun base64encode(input: Bytes): String

    /**
     * Base64 text-to-binary function used to restore data encoded by Base64,
     * reverse of base64encode()
     *
     * @param input encoded Base64 string
     * @return decoded byte array
     */
    fun base64decode(input: String): Bytes

    /**
     * @return a public and private key-pair by seed-phrase
     */
    fun keyPair(seed: Seed): KeyPair

    /**
     * @return a public key as String by seed-phrase
     */
    fun publicKey(seed: Seed): PublicKey

    /**
     * @return a private key as String by seed-phrase
     */
    fun privateKey(seed: Seed): PrivateKey

    /**
     * @return a new generated Waves address as String from the publicKey and chainId
     */
    fun addressByPublicKey(publicKey: PublicKey, chainId: String?): Address

    /**
     * @return a new generated Waves address as String from the seed-phrase
     */
    fun addressBySeed(seed: Seed, chainId: String?): Address

    /**
     * Random Seed-phrase generator from 2048 prepared words.
     * It's a list of words which store all the information needed to recover a private key
     * @return a new randomly generated BIP39 seed-phrase
     */
    fun randomSeed(): Seed

    /**
     * @param privateKey is a key to an address that gives access
     * to the management of the tokens on that address as String.
     * It is string encoded by Base58 from byte array.
     * @return signature for the bytes by privateKey as byte array
     */
    fun signBytesWithPrivateKey(bytes: Bytes, privateKey: PrivateKey): Bytes

    /**
     * @return signature for the bytes by seed-phrase as byte array
     */
    fun signBytesWithSeed(bytes: Bytes, seed: Seed): Bytes

    /**
     * @return true if signature is a valid signature of bytes by publicKey
     */
    fun verifySignature(publicKey: PublicKey, bytes: Bytes, signature: Bytes): Boolean

    /**
     * @return true if publicKey is a valid public key
     */
    fun verifyPublicKey(publicKey: PublicKey): Boolean

    /**
     * Checks address for a valid by optional chainId and publicKey params
     * If params non null it's will be checked.
     * @param address a unique identifier of an account on the Waves blockchain
     * @param chainId it's id of blockchain network 'W' for production and 'T' for test net
     * @param publicKey
     * @return true if address is a valid Waves address for optional chainId and publicKey
     */
    fun verifyAddress(address: Address, chainId: String? = null, publicKey: PublicKey? = null): Boolean


    /**
     *
     */
    fun aesEncrypt(data: String, secret: String): String

    /**
     *
     */
    fun aesDecrypt(encryptedData: String, secret: String): String

    companion object : WavesCrypto {

        const val PUBLIC_KEY_LENGTH = 32
        const val PRIVATE_KEY_LENGTH = 32
        const val SIGNATURE_LENGTH = 64

        const val ADDRESS_VERSION: Byte = 1
        const val CHECK_SUM_LENGTH = 4
        const val HASH_LENGTH = 20
        const val ADDRESS_LENGTH = 1 + 1 + CHECK_SUM_LENGTH + HASH_LENGTH

        const val MAIN_NET_CHAIN_ID: Byte = 87
        const val TEST_NET_CHAIN_ID: Byte = 84

        fun addressFromPublicKey(publicKey: ByteArray, scheme: Byte = MAIN_NET_CHAIN_ID)
                : String {
            return try {
                val publicKeyHash = Hash.keccak(publicKey).copyOf(HASH_LENGTH)
                val withoutChecksum = com.google.common.primitives.Bytes.concat(
                    byteArrayOf(ADDRESS_VERSION, scheme),
                    publicKeyHash)
                Base58.encode(com.google.common.primitives.Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)))
            } catch (e: Exception) {
                "Unknown address"
            }
        }

        fun calcCheckSum(bytes: ByteArray): ByteArray {
            return Arrays.copyOfRange(Hash.keccak(bytes), 0, CHECK_SUM_LENGTH)
        }

        override fun blake2b(input: Bytes): Bytes {
            return Hash.blake2b(input)
        }

        override fun keccak(input: Bytes): Bytes {
            return Hash.keccak(input)
        }

        override fun sha256(input: Bytes): Bytes {
            return Hash.sha256(input)
        }

        override fun base58encode(input: Bytes): String {
            return Base58.encode(input)
        }

        override fun base58decode(input: String): Bytes {
            return Base58.decode(input)
        }

        override fun base64encode(input: Bytes): String {
            return Base64.encodeBase64String(input)
        }

        override fun base64decode(input: String): Bytes {
            return Base64.decodeBase64(input)
        }

        override fun keyPair(seed: Seed): KeyPair {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return object : KeyPair {
                override val publicKey: PublicKey
                    get() = account.publicKeyStr
                override val privateKey: PrivateKey
                    get() = account.privateKeyStr
            }
        }

        override fun publicKey(seed: Seed): PublicKey {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return account.publicKeyStr
        }

        override fun privateKey(seed: Seed): PrivateKey {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return account.privateKeyStr
        }

        override fun addressByPublicKey(publicKey: PublicKey, chainId: String?): Address {
            return if (chainId.isNullOrEmpty()) {
                addressFromPublicKey(base58decode(publicKey))
            } else {
                addressFromPublicKey(base58decode(publicKey), chainId.toByte())
            }
        }

        override fun addressBySeed(seed: Seed, chainId: String?): Address {
            return addressByPublicKey(publicKey(seed), chainId)
        }

        override fun randomSeed(): Seed {
            return WalletManager.createWalletSeed()
        }

        override fun signBytesWithPrivateKey(bytes: Bytes, privateKey: PrivateKey): Bytes {
            return CryptoProvider.sign(base58decode(privateKey), bytes)
        }

        override fun signBytesWithSeed(bytes: Bytes, seed: Seed): Bytes {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return CryptoProvider.sign(account.privateKey, bytes)
        }

        override fun verifySignature(publicKey: PublicKey, bytes: Bytes, signature: Bytes): Boolean {
            return CryptoProvider.get().verifySignature(base58decode(publicKey), bytes, signature)
        }

        override fun verifyPublicKey(publicKey: PublicKey): Boolean {
            val publicKeyDecode = base58decode(publicKey)
            return publicKeyDecode.size == PUBLIC_KEY_LENGTH
        }

        override fun verifyAddress(address: Address, chainId: String?, publicKey: PublicKey?): Boolean {
            if (address.isEmpty()) {
                return false
            }

            val bytes = base58decode(address)

            if (!chainId.isNullOrEmpty()) {
                try {
                    if (chainId.toByte().toChar() != bytes[1].toChar()) {
                        return false
                    }
                } catch (error: NumberFormatException) {
                    return false
                }
            }

            if (publicKey != null
                    && addressByPublicKey(publicKey, chainId) != address) {
                return false
            }

            return try {
                if (bytes.size == ADDRESS_LENGTH
                        && bytes[0] == ADDRESS_VERSION) {
                    val checkSum = Arrays.copyOfRange(bytes, bytes.size - CHECK_SUM_LENGTH, bytes.size)
                    val checkSumGenerated = calcCheckSum(bytes.copyOf(bytes.size - CHECK_SUM_LENGTH))
                    Arrays.equals(checkSum, checkSumGenerated)
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        override fun aesEncrypt(data: String, secret: String) : String {
            return AESUtil.encrypt(data, secret)
        }

        override fun aesDecrypt(encryptedData: String, secret: String): String {
            return AESUtil.decrypt(encryptedData, secret)
        }
    }
}