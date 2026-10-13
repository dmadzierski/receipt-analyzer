package pl.madzierski.daniel.file_group;

import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MultipartFileMapper {

    private MultipartFileMapper() {
    }

    public static List<byte[]> toBytes(List<MultipartFile> files) {
        List<byte[]> result = new ArrayList<>();
        if (files == null) {
            return result;
        }
        for (MultipartFile file : files) {
            try {
                result.add(file.getBytes());
            } catch (IOException e) {
                throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_NOT_FOUND);
            }
        }
        return result;
    }
}