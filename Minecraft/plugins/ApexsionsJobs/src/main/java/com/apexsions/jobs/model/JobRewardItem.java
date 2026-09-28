package com.apexsions.jobs.model;

public class JobRewardItem {
    private final String target;
    private final double pay;
    private final double exp;

    public JobRewardItem(String target, double pay, double exp) {
        this.target = target;
        this.pay = pay;
        this.exp = exp;
    }

    public String getTarget() { return target; }
    public double getPay() { return pay; }
    public double getExp() { return exp; }
}
