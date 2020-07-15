package stuconcurrent.chapter1;

import java.math.BigDecimal;

public class Test {

    public static void main(String[] args) {
        method(150000, 5, "0.0027");
    }

    public static void method(int money, int year, String rate) {
        System.out.println("总借款：" + money);
        int month = year * 12;
        System.out.println("总期数：" + month + "/"+year+"年");
        double payByMonth = new BigDecimal(money + "").multiply(new BigDecimal(rate)).doubleValue();
        double baseByMonth = new BigDecimal(money + "").divide(new BigDecimal(month + ""), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
        double totalByMonth = new BigDecimal(payByMonth + "").add(new BigDecimal(baseByMonth + "")).doubleValue();
        System.out.println("每月还款：" + baseByMonth + "(本金) + " + payByMonth + "(利息) = " + totalByMonth);
        System.out.println("总利息金额：" + new BigDecimal(payByMonth).multiply(new BigDecimal(month)).doubleValue());
        get(baseByMonth, payByMonth, (double) money, month, 0);
    }

    private static void get(double baseByMonth, double payByMonth, double base, int month, int payMonth) {
        if (month == 0) return;
        payMonth++;
        double v = new BigDecimal(payByMonth + "").multiply(new BigDecimal("12")).divide(new BigDecimal(base + ""), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue();
        System.out.println("第" + payMonth + "期年化利率：" + v + "%");
        get(baseByMonth, payByMonth, new BigDecimal(base + "").subtract(new BigDecimal(baseByMonth + "")).doubleValue(), month - 1, payMonth);
    }
}
