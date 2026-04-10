/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class MavenWrapperDownloader {
    private static final String DEFAULT_DOWNLOAD_URL =
            "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar";

    public static void main(String[] args) throws Exception {
        Path baseDirectory = args.length == 0 ? Paths.get(".") : Paths.get(args[0]);
        Path wrapperPropertiesPath = baseDirectory.resolve(".mvn/wrapper/maven-wrapper.properties");
        Path wrapperJarPath = baseDirectory.resolve(".mvn/wrapper/maven-wrapper.jar");

        Properties properties = new Properties();
        if (Files.exists(wrapperPropertiesPath)) {
            try (InputStream inputStream = Files.newInputStream(wrapperPropertiesPath)) {
                properties.load(inputStream);
            }
        }

        String wrapperUrl = properties.getProperty("wrapperUrl", DEFAULT_DOWNLOAD_URL);

        Files.createDirectories(wrapperJarPath.getParent());
        downloadFileFromURL(wrapperUrl, wrapperJarPath);
        System.out.println("Downloaded Maven wrapper jar to " + wrapperJarPath);
    }

    private static void downloadFileFromURL(String urlString, Path destination) throws IOException {
        URL website = new URL(urlString);
        try (InputStream inStream = website.openStream();
             OutputStream outStream = Files.newOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) >= 0) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
