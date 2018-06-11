package unl.fct.srsc.client.config;

public class SecurityConfig {

    private String ciphersuite;
    private String provider;
    private String hmac;
    private String keyStoreType;
    private String keyStoreName;
    private String keyName;
    private String keyPassword;
    private String keyStorePassword;
    private String signatureAlgorithm;
    private String signatureAlgProvider;
    private String signatureKeyName;
    private String SignatureKeyPassword;
    private String redisPassword;

    public SecurityConfig() {
    }

    public SecurityConfig(String ciphersuite, String provider, String hmac, String keyStoreType, String keyStoreName, String keyName, String keyPassword, String keyStorePassword, String signatureAlgorithm, String signatureAlgProvider, String signatureKeyName, String signatureKeyPassword, String redisPassword) {
        this.ciphersuite = ciphersuite;
        this.provider = provider;
        this.hmac = hmac;
        this.keyStoreType = keyStoreType;
        this.keyStoreName = keyStoreName;
        this.keyName = keyName;
        this.keyPassword = keyPassword;
        this.keyStorePassword = keyStorePassword;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureAlgProvider = signatureAlgProvider;
        this.signatureKeyName = signatureKeyName;
        SignatureKeyPassword = signatureKeyPassword;
        this.redisPassword = redisPassword;
    }

    public String getCiphersuite() {
        return ciphersuite;
    }

    public void setCiphersuite(String ciphersuite) {
        this.ciphersuite = ciphersuite;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStoreName() {
        return keyStoreName;
    }

    public void setKeyStoreName(String keyStoreName) {
        this.keyStoreName = keyStoreName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignatureAlgProvider() {
        return signatureAlgProvider;
    }

    public void setSignatureAlgProvider(String signatureAlgProvider) {
        this.signatureAlgProvider = signatureAlgProvider;
    }

    public String getSignatureKeyName() {
        return signatureKeyName;
    }

    public void setSignatureKeyName(String signatureKeyName) {
        this.signatureKeyName = signatureKeyName;
    }

    public String getSignatureKeyPassword() {
        return SignatureKeyPassword;
    }

    public void setSignatureKeyPassword(String signatureKeyPassword) {
        SignatureKeyPassword = signatureKeyPassword;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }
}