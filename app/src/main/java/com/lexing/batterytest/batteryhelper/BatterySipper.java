package com.lexing.batterytest.batteryhelper;

import android.graphics.drawable.Drawable;

public class BatterySipper implements Comparable<BatterySipper> {

	private String name;
	private String pkgName;
	private Drawable icon;
	private double value;
	private double percent;

	public BatterySipper(String pkgName, double time) {
		value = time;
		this.pkgName = pkgName;
	}

	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		if(this.name == null || "".equals(this.name)) {
			return pkgName;
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public double getPercentOfTotal() {
		return percent;
	}

	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	@Override
	public int compareTo(BatterySipper other) {
		return (int) (other.getValue() - getValue());
	}
}