package com.studyhub.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "studyhub")
public class ApplicationProperties {

    private Jwt jwt = new Jwt();
    private Admin admin = new Admin();
    private Cors cors = new Cors();
    private Storage storage = new Storage();
    private Ai ai = new Ai();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }

    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public Ai getAi() { return ai; }
    public void setAi(Ai ai) { this.ai = ai; }

    public static class Jwt {
        private String secret;
        private long accessExpirationMs = 900000;
        private long refreshExpirationMs = 604800000;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getAccessExpirationMs() { return accessExpirationMs; }
        public void setAccessExpirationMs(long accessExpirationMs) { this.accessExpirationMs = accessExpirationMs; }

        public long getRefreshExpirationMs() { return refreshExpirationMs; }
        public void setRefreshExpirationMs(long refreshExpirationMs) { this.refreshExpirationMs = refreshExpirationMs; }
    }

    public static class Admin {
        private String bootstrapEmail = "admin@studyhub.edu";
        private String bootstrapPassword = "AdminStudyHub#2026";
        private String bootstrapName = "System Administrator";

        public String getBootstrapEmail() { return bootstrapEmail; }
        public void setBootstrapEmail(String bootstrapEmail) { this.bootstrapEmail = bootstrapEmail; }

        public String getBootstrapPassword() { return bootstrapPassword; }
        public void setBootstrapPassword(String bootstrapPassword) { this.bootstrapPassword = bootstrapPassword; }

        public String getBootstrapName() { return bootstrapName; }
        public void setBootstrapName(String bootstrapName) { this.bootstrapName = bootstrapName; }
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class Storage {
        private String type = "LOCAL";
        private String localPath = "./uploads";
        private int maxSizeMb = 25;
        private String s3Endpoint;
        private String s3Bucket;
        private String s3Region = "us-east-1";
        private String s3AccessKey;
        private String s3SecretKey;
        private String s3PublicUrl;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getLocalPath() { return localPath; }
        public void setLocalPath(String localPath) { this.localPath = localPath; }

        public int getMaxSizeMb() { return maxSizeMb; }
        public void setMaxSizeMb(int maxSizeMb) { this.maxSizeMb = maxSizeMb; }

        public String getS3Endpoint() { return s3Endpoint; }
        public void setS3Endpoint(String s3Endpoint) { this.s3Endpoint = s3Endpoint; }

        public String getS3Bucket() { return s3Bucket; }
        public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }

        public String getS3Region() { return s3Region; }
        public void setS3Region(String s3Region) { this.s3Region = s3Region; }

        public String getS3AccessKey() { return s3AccessKey; }
        public void setS3AccessKey(String s3AccessKey) { this.s3AccessKey = s3AccessKey; }

        public String getS3SecretKey() { return s3SecretKey; }
        public void setS3SecretKey(String s3SecretKey) { this.s3SecretKey = s3SecretKey; }

        public String getS3PublicUrl() { return s3PublicUrl; }
        public void setS3PublicUrl(String s3PublicUrl) { this.s3PublicUrl = s3PublicUrl; }
    }

    public static class Ai {
        private String provider = "GEMINI";
        private String apiKey = "";
        private String model = "gemini-1.5-flash";
        private int rateLimitPerMinute = 15;
        private int dailyQuota = 100;
        private int timeoutSeconds = 20;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public int getRateLimitPerMinute() { return rateLimitPerMinute; }
        public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }

        public int getDailyQuota() { return dailyQuota; }
        public void setDailyQuota(int dailyQuota) { this.dailyQuota = dailyQuota; }

        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }
}
