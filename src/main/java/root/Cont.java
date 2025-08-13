package root;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import root.entity.Medicine;
import root.entity.TestTable;
import root.repo.MedicineRepo;
import root.repo.TestRepo;

import java.util.List;

@RestController
@RequestMapping("/table")
public class Cont {
    final MedicineRepo medicineRepo;
    final TestRepo testRepo;

    public Cont(MedicineRepo medicineRepo, TestRepo testRepo) {
        this.medicineRepo = medicineRepo;
        this.testRepo = testRepo;
    }

    @PostMapping
    public void saveMedicine(@RequestBody @Valid Medicine dto) {
        medicineRepo.save(dto);
    }
    @PostMapping("/test")
    public void saveTest(@RequestBody TestTable dto) {
        testRepo.save(dto);
    }
    @GetMapping("/test")
    public List<TestTable> getTest() {
        return testRepo.findAll();
    }
}
