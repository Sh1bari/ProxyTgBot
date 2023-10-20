package com.example.proxytgbot.scheduler;

import com.example.proxytgbot.exceptions.HasTextPageError;
import com.example.proxytgbot.models.entities.Domain;
import com.example.proxytgbot.models.entities.Geo;
import com.example.proxytgbot.models.entities.Proxy;
import com.example.proxytgbot.models.entities.User;
import com.example.proxytgbot.models.enums.DomainStatus;
import com.example.proxytgbot.models.enums.ProxyStatus;
import com.example.proxytgbot.repositories.DomainRepo;
import com.example.proxytgbot.repositories.GeoRepo;
import com.example.proxytgbot.repositories.ProxyRepo;
import com.example.proxytgbot.repositories.UserRepo;
import com.example.proxytgbot.services.interfaces.MessageSender;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAsync
public class Scheduler {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GeoRepo geoRepo;

    @Autowired
    private MessageSender messageSender;
    @Autowired
    private ProxyRepo proxyRepo;
    @Autowired
    private DomainRepo domainRepo;


    @Async
    @Scheduled(fixedRate = 300000)
    public void scheduler() throws NoSuchAlgorithmException, KeyManagementException {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};

        // Игнорирование ошибок SSL-сертификата
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);

        List<User> userList = (List<User>) userRepo.findAll();
        for (User user : userList) {
            check(user);
        }
    }

    @SneakyThrows
    public void check(User user) {
        for (Geo geo : geoRepo.findAll()) {
            List<Domain> domainList = user.getDomains().stream()
                    .filter(domain -> domain.getGeo().getName().equals(geo.getName()))
                    .filter(domain -> domain.getStatus() == DomainStatus.ACTIVE)
                    .toList();
            List<Proxy> proxyList = user.getProxies().stream()
                    .filter(domain -> domain.getGeo().getName().equals(geo.getName()))
                    .filter(proxy -> proxy.getStatus() == ProxyStatus.ACTIVE)
                    .toList();
            for (Domain domain : domainList) {
                List<Proxy> bannedProxyForDomain = new ArrayList<>();
                List<Proxy> bannedProxy = new ArrayList<>();
                List<Proxy> bannedProxyWithPage = new ArrayList<>();
                List<Proxy> sslBannedProxyWithPage = new ArrayList<>();
                for (Proxy proxyIter : proxyList) {
                    List<String> proxyInfo = List.of(proxyIter.getCode().split(":"));
                    String proxyHost;
                    int proxyPort;
                    String proxyUsername;
                    String proxyPassword;
                    try {
                        proxyHost = proxyInfo.get(0);
                        proxyPort = Integer.parseInt(proxyInfo.get(1));
                        proxyUsername = proxyInfo.get(2);
                        proxyPassword = proxyInfo.get(3);
                    } catch (Exception e) {
                        messageSender.sendErrorMessageByScheduler(user.getTelegramChatId(), "Неправильный формат прокси:\n" + proxyIter.getCode());
                        proxyRepo.deleteFun(proxyIter.getId());
                        continue;
                    }
                    URL url;
                    try {
                        url = new URL(domain.getDomain());
                    } catch (Exception e) {
                        messageSender.sendMessage(user.getTelegramChatId(), "Непредвиденная ошибка в домене: " + domain.getDomain());
                        domain.setStatus(DomainStatus.BANNED);
                        domainRepo.save(domain);
                        break;
                    }

                    Authenticator proxyAuthenticator = new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                        }
                    };

                    java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
                    connection.setConnectTimeout(5000); // 3 секунды
                    connection.setReadTimeout(5000); // 3 секунды
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Chrome");
                    connection.setAuthenticator(proxyAuthenticator);
                    BufferedReader reader;
                    try {
                        int responseCode = connection.getResponseCode();
                        if(responseCode == 200) {
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String line;
                            StringBuilder response = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            reader.close();
                            Document doc = Jsoup.parse(response.toString());
                            String text = doc.body().text();
                            if (text.contains("Uwaga!") ||
                                    text.contains("tinklalapis") ||
                                    text.contains("Tinklalapis") ||
                                    text.contains("uwaga!")
                            ) {
                                throw new HasTextPageError();
                            }
                            // Вывод содержимого ответа
                        }
                    } catch (SocketException | SocketTimeoutException e) {
                        System.out.println("Превышено время ожидания");

                        bannedProxyForDomain.add(proxyIter);
                    } catch (HasTextPageError e) {
                        System.out.println("Есть страница с баном");

                        bannedProxyWithPage.add(proxyIter);
                    } catch (SSLHandshakeException e) {
                        sslBannedProxyWithPage.add(proxyIter);
                        break;
                    } catch (IOException e) {
                        System.out.println("Ошибка в proxy");
                        bannedProxy.add(proxyIter);
                    } catch (Exception e) {
                        System.out.println(e.getClass());
                    } finally {
                        connection.disconnect();
                        Thread.sleep(500);
                    }
                }
                // Время ожидание превышено
                if (!bannedProxyForDomain.isEmpty()) {
                    StringBuilder str = new StringBuilder();
                    bannedProxyForDomain.forEach(o -> {
                        str.append(o.getCode()).append("\n");
                    });
                    messageSender.sendErrorMessageByScheduler(user.getTelegramChatId(), domain.getDomain() + "\nТип ошибки:\n- Превышено время ожидания" +
                            "\nПрокси:\n" + str);
                }
                //Прокси не работает
                if (!bannedProxy.isEmpty()) {
                    StringBuilder str = new StringBuilder();
                    bannedProxy.forEach(o -> {
                        str.append(o.getCode()).append("\n");
                    });
                    messageSender.sendErrorMessageByScheduler(user.getTelegramChatId(), "Прокси не работает:\n" + str);

                }
                //Страница с баном
                if (!bannedProxyWithPage.isEmpty()) {
                    StringBuilder str = new StringBuilder();
                    bannedProxyWithPage.forEach(o -> {
                        str.append(o.getCode()).append("\n");
                    });
                    messageSender.sendErrorMessageByScheduler(user.getTelegramChatId(), domain.getDomain() + "\nТип ошибки:\n- Страница с баном" +
                            "\nПрокси:\n" + str);
                    domain.setStatus(DomainStatus.BANNED);
                    domainRepo.save(domain);
                    messageSender.sendErrorMessageByScheduler(user.getTelegramChatId(), domain.getDomain() + " удаляется");
                }
                //ssl
                if (!sslBannedProxyWithPage.isEmpty()) {
                    StringBuilder str = new StringBuilder();
                    sslBannedProxyWithPage.forEach(o -> {
                        str.append(o.getCode()).append("\n");
                    });
                    messageSender.sendErrorMessageByScheduler(user.getTelegramChatId(), domain.getDomain() + "\nТип ошибки:\n- Неправильно написан или SSL ошибка" +
                            "\nПрокси:\n" + str);
                }

            }
        }
    }
}
