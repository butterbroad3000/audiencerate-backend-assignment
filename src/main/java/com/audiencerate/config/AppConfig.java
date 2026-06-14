package com.audiencerate.config;

public record AppConfig(
        String profilesJdbcUrl,
        String segmentsJdbcUrl,
        String activationsJdbcUrl,
        String dbUser,
        String dbPassword,
        int poolMaxSize,
        int poolMinIdle,
        long connectionTimeoutMs,
        int httpPort) {

    public static AppConfig fromEnv() {
        return new AppConfig(
                envOrDefault("DB_PROFILES_JDBC_URL", "jdbc:postgresql://localhost:5432/profiles"),
                envOrDefault("DB_SEGMENTS_JDBC_URL", "jdbc:postgresql://localhost:5432/segments"),
                envOrDefault("DB_ACTIVATIONS_JDBC_URL", "jdbc:postgresql://localhost:5432/activations"),
                envOrDefault("DB_USER", "audiencerate"),
                envOrDefault("DB_PASSWORD", "audiencerate"),
                Integer.parseInt(envOrDefault("DB_POOL_MAX_SIZE", "5")),
                Integer.parseInt(envOrDefault("DB_POOL_MIN_IDLE", "1")),
                Long.parseLong(envOrDefault("DB_CONNECTION_TIMEOUT_MS", "5000")),
                Integer.parseInt(envOrDefault("HTTP_PORT", "8080")));
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
