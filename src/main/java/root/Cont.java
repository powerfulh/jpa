package root;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.entity.Medicine;
import root.repo.MedicineRepo;

@RestController
@RequestMapping("/table")
public class Cont {
    final MedicineRepo medicineRepo;

    public Cont(MedicineRepo medicineRepo) {
        this.medicineRepo = medicineRepo;
    }

    @PostMapping
    public void saveMedicine(@RequestBody @Valid Medicine dto) {
        medicineRepo.save(dto);
    }
}
