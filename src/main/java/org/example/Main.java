package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


public class Main {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=pKQaUkQcQ8OvnRgBll7Fgv4WDUfUxsPUPSWRdMYR";

    public static void main(String[] args) throws IOException {

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build()) {

            HttpGet request = new HttpGet(REMOTE_SERVICE_URI);
            request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            CloseableHttpResponse response = httpClient.execute(request);
            Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            Post post = mapper.readValue(sb.toString(), Post.class);//response.getEntity().getContent()
            // Получаем имя файла
            String strUrl = post.getUrl();
            String[] splitString = strUrl.split("/");
            System.out.println(splitString[splitString.length - 1]);
            //Делаем запрос
            HttpGet requestUrl = new HttpGet(strUrl);
            CloseableHttpResponse responseUrl = httpClient.execute(requestUrl);
            InputStream inputStream = responseUrl.getEntity().getContent();
            //Проверяем есть ли файл и если нет то создаем
            String pathToFileOnDisk = "F://" + splitString[splitString.length - 1];
            Path path = Path.of(pathToFileOnDisk);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            FileOutputStream fileOS = new FileOutputStream(pathToFileOnDisk);
            int part;
            while ((part = inputStream.read()) != -1) {
                fileOS.write(part);
            }
            fileOS.close();
            inputStream.close();
        }
    }
}