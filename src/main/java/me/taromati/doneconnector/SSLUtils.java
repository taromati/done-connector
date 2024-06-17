package me.taromati.doneconnector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class SSLUtils {
    public static SSLSocketFactory createSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new SimpleX509TrustManager()}, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class SimpleX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
