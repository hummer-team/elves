package io.elves.http.server.banner;

import io.elves.common.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Banner {


    private Banner() {

    }

    public static void print() {
        try (InputStream stream = ResourceUtil.getResourceAsStream(Banner.class.getClassLoader(), "banner.txt");
             BufferedReader f = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            int r;
            StringBuilder stringBuilder = new StringBuilder();
            while ((r = f.read()) != -1) {
                stringBuilder.append((char) r);
            }
            System.out.println(stringBuilder.toString());
        } catch (Exception e) {
            //ignore
        }
    }
}
