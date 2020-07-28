package com.example.spillaip.sms_app;

import java.util.Date;
import java.util.Objects;

public class Scheme {
    String AMC;
    String Fund;
    double NAV;
    double Units;
    double Amount;
    Date TrxDate;

    public Scheme(String AMC, String fund, double NAV, double units, double amount, Date trxDate) {
        this.AMC = AMC;
        Fund = fund;
        this.NAV = NAV;
        Units = units;
        Amount = amount;
        TrxDate = trxDate;
    }

    public String getAMC() {
        return AMC;
    }

    public void setAMC(String AMC) {
        this.AMC = AMC;
    }

    public String getFund() {
        return Fund;
    }

    public void setFund(String fund) {
        Fund = fund;
    }

    public double getNAV() {
        return NAV;
    }

    public void setNAV(double NAV) {
        this.NAV = NAV;
    }

    public double getUnits() {
        return Units;
    }

    public void setUnits(double units) {
        Units = units;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    public Date getTrxDate() {
        return TrxDate;
    }

    public void setTrxDate(Date trxDate) {
        TrxDate = trxDate;
    }

    @Override
    public String toString() {
        return "Scheme{" +
                "AMC='" + AMC + '\'' +
                ", Fund='" + Fund + '\'' +
                ", NAV=" + NAV +
                ", Units=" + Units +
                ", Amount=" + Amount +
                ", TrxDate=" + TrxDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scheme scheme = (Scheme) o;
        return Objects.equals(AMC, scheme.AMC) &&
                Objects.equals(Fund, scheme.Fund) &&
                Objects.equals(NAV, scheme.NAV) &&
                Objects.equals(Units, scheme.Units) &&
                Objects.equals(Amount, scheme.Amount) &&
                Objects.equals(TrxDate, scheme.TrxDate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(AMC, Fund, NAV, Units, Amount, TrxDate);
    }
}
