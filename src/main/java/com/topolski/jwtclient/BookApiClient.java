package com.topolski.jwtclient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.stream.Stream;

@Controller
public class BookApiClient {
    public BookApiClient() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        addBooks();
        getBooks();
    }

    private void addBooks() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        String jwt = generateJwt(true);
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwt);
        System.out.println(jwt);
        String bookToAdd = "Spring Boot in action - user";
        HttpEntity httpEntity = new HttpEntity(bookToAdd, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange("http://localhost:8080/books",
                HttpMethod.POST,
                httpEntity,
                Void.class);
    }

    private void getBooks() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + generateJwt(true));
        HttpEntity httpEntity = new HttpEntity(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String[]> exchange = restTemplate.exchange("http://localhost:8080/books",
                HttpMethod.GET,
                httpEntity,
                String[].class);
        Stream.of(exchange.getBody()).forEach(System.out::println);
    }

    private String generateJwt(boolean isAdmin) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        Algorithm algorithm = Algorithm.RSA256(null, getPrivateKey());
        return JWT.create()
                .withClaim("admin", isAdmin)
                .withClaim("iat", Instant.now().getEpochSecond())
                .withClaim("exp", Instant.now().getEpochSecond() + 60)
                .sign(algorithm);
    }

    private RSAPrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        byte[] keyBytes = readFile("private_key.der");
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private byte[] readFile(String filename) throws IOException {
        return Files.readAllBytes(Paths.get("src/main/resources/" + filename));
    }
}
