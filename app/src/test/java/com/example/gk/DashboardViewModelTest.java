package com.example.gk;

import org.junit.Test;
import static org.junit.Assert.*;
import com.example.gk.viewmodel.DashboardViewModel;
import java.util.ArrayList;
import java.util.List;

public class DashboardViewModelTest {

    @Test
    public void testCalculateStatistics_VND() {
        // 1. Chuẩn bị dữ liệu
        DashboardViewModel viewModel = new DashboardViewModel();
        List<Expense> fakeList = new ArrayList<>();

        // Tạo Expense 1: Thu nhập 1 triệu
        Expense e1 = new Expense();
        e1.isIncome = true;
        e1.amount = 1000000;
        fakeList.add(e1);

        // Tạo Expense 2: Chi tiêu 200k
        Expense e2 = new Expense();
        e2.isIncome = false;
        e2.amount = 200000;
        e2.category = "Ăn uống";
        fakeList.add(e2);

        // Tạo Expense 2: Chi tiêu 300k
        Expense e3 = new Expense();
        e3.isIncome = false;
        e3.amount = 300000;
        e3.category = "Ăn uống";
        fakeList.add(e3);

        // Giả sử đang xem bằng VND (Tỷ giá = 1.0)
        double rateFromVND = 1.0;

        // 2. Gọi hàm cần test
        viewModel.calculateStatistics(fakeList, rateFromVND);

        // 3. Kiểm tra kết quả (Assert)
        assertEquals(1000000.0, viewModel.convertedIncome, 0.001);
        assertEquals(500000.0, viewModel.convertedExpense, 0.001);
        assertEquals(500000.0, viewModel.convertedDifference, 0.001);

        // Kiểm tra category map
        assertTrue(viewModel.categoryTotals.containsKey("Ăn uống"));
        assertEquals(500000.0, viewModel.categoryTotals.get("Ăn uống"), 0.001);
    }

    @Test
    public void testCalculateStatistics_USD() {
        // Test trường hợp quy đổi tỷ giá
        DashboardViewModel viewModel = new DashboardViewModel();
        List<Expense> fakeList = new ArrayList<>();

        // Thu nhập 2,500,000 VND
        Expense e1 = new Expense();
        e1.isIncome = true;
        e1.amount = 2500000;
        fakeList.add(e1);

        // Tỷ giá: 1 USD = 25,000 VND -> rateFromVND = 25000
        double rateFromVND = 25000.0;

        // Gọi hàm
        viewModel.calculateStatistics(fakeList, rateFromVND);

        // Kỳ vọng: 2,500,000 / 25,000 = 100 USD
        assertEquals(100.0, viewModel.convertedIncome, 0.001);
    }
}