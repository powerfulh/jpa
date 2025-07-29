package root.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
public class PowerfulJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    @NotBlank
    public String name;
    @NotBlank
    public String data;
    public int owner;
    public LocalDateTime updated_date = LocalDateTime.now();
}
