package pl.madzierski.daniel.file_group;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
class File {

    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private FileGroup fileGroup;
    private String path;
    private String rawData;
    private Integer partNumber;

    public File(String id, String path, FileGroup fileGroup, String rawData, Integer partNumber) {
        this.id = id;
        this.path = path;
        this.fileGroup = fileGroup;
        this.rawData = rawData;
        this.partNumber = partNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        File that = (File) o;
        return Objects.equals(path, that.path) && Objects.equals(rawData, that.rawData) && Objects.equals(partNumber, that.partNumber);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(path);
        result = 31 * result + Objects.hashCode(rawData);
        result = 31 * result + Objects.hashCode(partNumber);
        return result;
    }

}