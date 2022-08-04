package com.parkit.parkingsystem.service;

import java.time.Duration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.PricesUtil;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		Duration duration = Duration.between(ticket.getInTime(), ticket.getOutTime());
		double durationInHours = duration.toMillis() / (60.0 * 60.0 * 1000.0);

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			ticket.setPrice(PricesUtil.roundToPrice(durationInHours * Fare.CAR_RATE_PER_HOUR));
			break;
		}
		case BIKE: {
			ticket.setPrice(PricesUtil.roundToPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR));
			break;
		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}

	}
}