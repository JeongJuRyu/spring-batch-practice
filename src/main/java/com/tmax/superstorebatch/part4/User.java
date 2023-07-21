package com.tmax.superstorebatch.part4;

import com.tmax.superstorebatch.part5.Orders;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Entity(name = "Users")
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Level level = Level.NORMAL;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Orders> orders;

    private LocalDate updatedDate;

    @Builder
    public User(String username, List<Orders> orders) {
        this.username = username;
        this.orders = orders;
    }

    public boolean availableLevelUp() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
    }

    private int getTotalAmount() {
        return this.orders.stream()
                .mapToInt(Orders::getAmount)
                .sum();
    }

    public enum Level {
        VIP(500000, null),
        GOLD(500000, VIP),
        SILVER(300000, GOLD),
        NORMAL(200000, SILVER);

        private final int nextAmount;
        private final Level nextLevel;
        Level(int nextAmount, Level nextLevel){
            this.nextAmount = nextAmount;
            this.nextLevel = nextLevel;
        }

        public static boolean availableLevelUp(Level level, int totalAmount) {
            if(Objects.isNull(level)){
                return false;
            }

            if(Objects.isNull(level.nextLevel)){
                return false;
            }

            return totalAmount >= level.nextAmount;
        }

        public static Level getNextLevel(int totalAmount) {
            if (totalAmount >= Level.VIP.nextAmount) {
                return VIP;
            }
            if (totalAmount >= Level.GOLD.nextAmount) {
                return GOLD.nextLevel;
            }
            if (totalAmount >= Level.SILVER.nextAmount) {
                return SILVER.nextLevel;
            }
            if (totalAmount >= Level.VIP.nextAmount) {
                return NORMAL.nextLevel;
            }
            return NORMAL;
        }
    }

    public Level levelUp() {
        Level nextLevel = Level.getNextLevel(this.getTotalAmount());
        this.level = nextLevel;
        this.updatedDate = LocalDate.now();

        return nextLevel;
    }
}
