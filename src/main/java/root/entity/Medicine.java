package root.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer n;
    @NotBlank
    public String shop;
    @NotBlank
    public String medicine;
    @NotNull
    public Integer price;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    LocalDateTime updatedDate = LocalDateTime.now();
}
