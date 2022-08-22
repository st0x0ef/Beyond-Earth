package net.mrscauthd.beyond_earth.entities;

import net.mrscauthd.beyond_earth.gauge.GaugeValueHelper;
import net.mrscauthd.beyond_earth.gauge.IGaugeValue;

public interface IFuelVehicleEntity {
	public int getFuel();

	public void setFuel(int fuel);

	public int getFuelCapacity();

	public void putFuelMuchAsBucket();

	public boolean canPutFuelMuchAsBucket();

	public default IGaugeValue getFuelGauge() {
		int fuel = this.getFuel();
		int capacity = this.getFuelCapacity();
		return GaugeValueHelper.getFuel(fuel, capacity);
	}
}
