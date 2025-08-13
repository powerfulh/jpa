package root.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class TestTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer n;
    String name;
    Integer price;
    boolean discount;
    LocalDate updatedDate = LocalDate.now();
    Integer discountAmt;

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void setDiscount(boolean discount) {
        this.discount = discount;
        discountAmt = discount ? price - getPrice() : null;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return (int) ((discount ? 0.9 : 1) * price); // 10 퍼 할인
    }
}
