package io.elves.http.server.banner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Banner {
    private Banner() {

    }

    public static void print() {
        String url = Banner.class.getClassLoader().getResource("banner.txt").getPath();
        File file = new File(url);
        try (FileInputStream f = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            f.read(content);
            System.out.println(StringUtils.toEncodedString(content, StandardCharsets.UTF_8));
        } catch (Exception e) {
            //ignore
        }
    }
}
