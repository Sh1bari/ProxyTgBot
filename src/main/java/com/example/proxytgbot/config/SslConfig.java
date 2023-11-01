package com.example.proxytgbot.config;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Description:
 *
 * @author Vladimir Krasnov
 */
@Component
public class SslConfig {

    @PostConstruct
    public void configureSsl() {
        try {
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // Обработка ошибок, если не удается настроить SSL
            e.printStackTrace();
        }
    }
}
