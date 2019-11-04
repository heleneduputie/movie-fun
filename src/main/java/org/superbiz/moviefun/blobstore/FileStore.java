package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.superbiz.moviefun.albums.AlbumsController;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        saveUploadToFile(blob.inputStream, getCoverFile(Long.valueOf(blob.name)));
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        System.err.println("###### Hmmm, why am I here?");
        Path coverFilePath = null;
        try {
            coverFilePath = getExistingCoverPath(Long.valueOf(name));
        } catch (URISyntaxException e) {
            System.out.println("Error while get album cover " + e);
        }

        byte[] imageBytes = readAllBytes(coverFilePath);

        InputStream targetStream = new ByteArrayInputStream(imageBytes);

        String contentType = new Tika().detect(coverFilePath);

        Blob blob = new Blob(name, targetStream, contentType);

        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {
        // ...
    }

    private void saveUploadToFile(InputStream uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] bytes = IOUtils.toByteArray(uploadedFile);
            outputStream.write(bytes);
        }
    }

    private File getCoverFile(long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(long albumId) throws URISyntaxException, IOException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            Resource resource = new ClassPathResource("default-cover.jpg");
            coverFilePath = resource.getFile().toPath();
        }

        return coverFilePath;
    }

    /*private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }*/
}
