package assignment2.classes;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.Iterator;

import assignment2.exceptions.StockException;

/**
 * This class represents a collection of items representing store inventory,
 * orders, sales logs and truck cargo.
 * 
 * @author Liam Edwards
 * @author Alexander Rozsa
 */
public class Stock implements Iterable<Item> { 

	Map<Item, Integer> stock;

	/**
	 * Instantiate a new stock collection
	 */
	public Stock() {
		stock = new HashMap<Item, Integer>();
	}

	/*
	 * Add new Item to stock list
	 * 
	 * @param Item to add
	 */
	public void addNew(Item item) throws StockException {
		if (item != null) {
				stock.put(item, 0);
		} else {
			throw new StockException("Null item");
	}
		
	}
	/**
	 * Adds items to stock
	 * 
	 * @param Item to use as key
	 * @param Quantity to be added (Can be 0)
	 * @throws StockException
	 */
	public void add(Item item, int quantity) throws StockException {
		if (item == null) {
			throw new StockException("Null item");
		}
		if (quantity >= 0) {
				stock.put(item, 0);
		} else {
			throw new StockException("Negative amount");
	}
	}

	/**
	 * Removes items from stock
	 * 
	 * @param item
	 * @param quantity
	 * @throws StockException
	 */
	public void remove(Item item, int quantity) throws StockException {
		if (stock.containsKey(item)) {
			if (quantity < stock.get(item)) {
				stock.put(item, stock.get(item) - quantity); // Decrements the key's value
			} else {
				throw new StockException("Not enough items");
			}
		} else {
			throw new StockException("Doesn't exist");
		}
	}

	/**
	 * Gets quantity of item
	 * 
	 * @param item
	 * @return quantity of specific item
	 */
	public int getItemQuantity(Item item) {
		if (stock.containsKey(item)) {
			return stock.get(item);
		}
		return 0;
	}

	/**
	 * Return the total quantity the stock list contains
	 * 
	 * @return quantity of all items in stock
	 */
	public int totalQuantity() {
		int totalQuantity = 0;
		for (int quantity : stock.values()) {
			totalQuantity += quantity;
		}
		return totalQuantity;
	}

	/**
	 * Returns if the hashmap contains an item or not
	 * 
	 * @param item
	 * @return true if the item is in stock
	 */
	public boolean contains(Item item) {
		return stock.containsKey(item);
	}

	/**
	 * Returns if the item needs to be reordered or not
	 * 
	 * @param item
	 * @return if the item quantity is <= to the item's reorder point
	 */
	public boolean reorder(Item item) {
		return stock.get(item) <= item.getReorderPoint();
	}
	
	@Override
	public Iterator<Item> iterator() {
		return new Iterator<Item>() {
			Object[] items = stock.keySet().toArray();
			int current = 0;

			@Override
			public boolean hasNext() {
				return current < items.length;
			}

			@Override
			public Item next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return (Item) items[current++];
			}
		};
	}
}