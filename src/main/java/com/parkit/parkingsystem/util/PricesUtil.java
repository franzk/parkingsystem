package com.parkit.parkingsystem.util;

public class PricesUtil {
	
	public static double roundToPrice(double rawPrice) {
		return Math.round(rawPrice * 100.0) / 100.0;
	}
	
}
