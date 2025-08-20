package root;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import root.entity.Medicine;
import root.entity.TestTable;
import root.repo.MedicineRepo;
import root.repo.TestRepo;
import root.service.RenameMe;

import java.util.List;

@RestController
@RequestMapping("/table")
public class Cont {
    final MedicineRepo medicineRepo;
    final TestRepo testRepo;

    final RenameMe renameMe;

    public Cont(MedicineRepo medicineRepo, TestRepo testRepo, RenameMe renameMe) {
        this.medicineRepo = medicineRepo;
        this.testRepo = testRepo;
        this.renameMe = renameMe;
    }

    @GetMapping("/med")
    public List<Medicine> getMedicine() {
        return medicineRepo.findAll();
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
    @GetMapping("/levenshtein/{src}/{target}")
    public int getLevenshteinDistance(@PathVariable String src, @PathVariable String target) {
        return renameMe.levenshteinDistance(src, target);
    }
}
