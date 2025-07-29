package root.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
public class PowerfulApi {
    @Id
    Integer n;
    @NotBlank
    String name;
    @NotBlank
    String data;
    int owner;
    LocalDateTime updated_date;
}
