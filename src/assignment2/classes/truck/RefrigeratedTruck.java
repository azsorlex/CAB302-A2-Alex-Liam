package assignment2.classes.truck;

import assignment2.classes.Item;
import assignment2.exceptions.StockException;

/**
 * This class represents a refrigerated truck
 * 
 * @author Alexander Rozsa
 * @author Liam Edwards
 */
public class RefrigeratedTruck extends Truck {

	private Double temperature;

	/**
	 * Constructs the RefrigeratedTruck object
	 * 
	 * @param temperature
	 */
	public RefrigeratedTruck(double temperature) {
		setTemp(temperature);
		maxCapacity = 800;
	}

	/**
	 * Adds items to the truck's cargo
	 * 
	 * @param item to be added
	 * @param quantity of item to be added
	 * @throws StockException if the item has a temperature threshold and the truck's temperature is greater than that threshold
	 */
	@Override
	public void add(Item item, int quantity) throws StockException {
		if (item.getTempThreshold() == null || temperature <= item.getTempThreshold()) {
			super.add(item, quantity);
		} else {
			throw new StockException();
		}
	}

	/**
	 * Sets the temperature
	 * 
	 * @param temperature to be set
	 */
	public void setTemp(double temperature) {
		if (this.temperature == null || temperature < this.temperature) {
			if (temperature < -20) {
				this.temperature = -20.0;
			} else if (temperature > 10) {
				this.temperature = 10.0;
			} else {
				this.temperature = temperature;
			}
		}
	}

	/**
	 * Gets the cost
	 * 
	 * @return cost of the truck
	 */
	@Override
	public double getCost() {
		return (900 + 200 * Math.pow(0.7, (temperature / 5.0)));
	}
}
