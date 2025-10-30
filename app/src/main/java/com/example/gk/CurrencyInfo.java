package com.example.gk;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "currencies")
public class CurrencyInfo {
    @PrimaryKey
    @NonNull
    public String code;       // Ví dụ: "USD"

    public String name;       // Ví dụ: "Đô la Mỹ"

    public CurrencyInfo() {}

    public CurrencyInfo(@NonNull String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
