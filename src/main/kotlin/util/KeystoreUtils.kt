package util

/**
 * Utility object for managing Java KeyStore operations.
 */
object KeystoreUtils {

    /**
     * Loads a Java KeyStore file from the provided directory path.
     *
     * @param dir The directory path where the keystore file is located.
     *
     * @return An initialized and loaded java.security.KeyStore instance.
     */
    fun loadKeyStore(dir: String, password: String): java.security.KeyStore {
        val keyStore = java.security.KeyStore.getInstance("JKS")
        val fis = object {}.javaClass.getResourceAsStream("$dir/keystore.jks")
        keyStore.load(fis, password.toCharArray())
        return keyStore
    }
}
