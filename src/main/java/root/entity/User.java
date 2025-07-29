package root.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class User {
    @Id
    public Integer n;
    @NotBlank
    @Schema(defaultValue = "test")
    public String id;
    @NotBlank
    public String pw;
}
