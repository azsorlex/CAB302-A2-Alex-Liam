package assignment2.classes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import assignment2.classes.truck.OrdinaryTruck;
import assignment2.classes.truck.RefrigeratedTruck;
import assignment2.classes.truck.Truck;
import assignment2.exceptions.CSVFormatException;
import assignment2.exceptions.DeliveryException;
import assignment2.exceptions.StockException;

/**
 * This class handles the importing and exporting of CSV files
 * 
 * @author Liam Edwards
 * @author Alexander Rozsa
 */
public class IOHandler {

	// Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	private static BufferedReader csvReader;
	private static String line;
	private static Stock refrigeratedItems, ordinaryItems;
	private static List<Item> sortedColdItems, listOrdinaryItems;
	private static Manifest manifest;

	private static final int ITEM_NAME_INDEX = 0;
	private static final int ITEM_COST_INDEX = 1;
	private static final int ITEM_PRICE_INDEX = 2;
	private static final int ITEM_ORDPOINT_INDEX = 3;
	private static final int ITEM_ORDAMT_INDEX = 4;
	private static final int ITEM_TEMP_INDEX = 5; // Optional
	private static final int MANIFEST_ITEM_INDEX = 0;
	private static final int MANIFEST_QUANT_INDEX = 1;
	private static final int SALESLOG_ITEM_INDEX = 0;
	private static final int SALESLOG_QUANT_INDEX = 1;

	/**
	 * Closes the CSVReader
	 * 
	 * @throws CSVFormatException on fail
	 */
	private static void closeCSVReader() throws CSVFormatException {
		try {
			csvReader.close();
		} catch (IOException e) {
			throw new CSVFormatException();
		}
	}
	
	/**
	 * Opens a window and returns the file path of the selected file.
	 * 
	 * @param fileExport : whether to set for export (true) or not (false)
	 * @return file path if approve (yes, OK) is chosen. null otherwise.
	 */
	public static String fileChooser(boolean fileExport) {
		JFileChooser fileChooser = new JFileChooser();
		
		if (fileExport) {
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDialogTitle("Save Manifest");
		} else {
			fileChooser.setDialogTitle("Import File");
		}

		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

	/**
	 * Reads the Item Properties from the given filePath provided via the GUI
	 * 
	 * @param filePath of the file to be read
	 * @throws CSVFormatException if something goes wrong
	 */
	public static void readItemProperties(String filePath) throws CSVFormatException {
		try {
			csvReader = new BufferedReader(new FileReader(filePath));
			while ((line = csvReader.readLine()) != null) {
				String[] properties = line.split(COMMA_DELIMITER);

				if (properties.length == 5) { // If a non-refrigerated item, create an item
					Store.getInventory().add(new Item(properties[ITEM_NAME_INDEX],
							Double.parseDouble(properties[ITEM_COST_INDEX]),
							Double.parseDouble(properties[ITEM_PRICE_INDEX]),
							Integer.parseInt(properties[ITEM_ORDPOINT_INDEX]),
							Integer.parseInt(properties[ITEM_ORDAMT_INDEX])), 0);

				} else if (properties.length == 6) { // If a refrigerated item, create an item
					Store.getInventory().add(new Item(properties[ITEM_NAME_INDEX],
							Double.parseDouble(properties[ITEM_COST_INDEX]),
							Double.parseDouble(properties[ITEM_PRICE_INDEX]),
							Integer.parseInt(properties[ITEM_ORDPOINT_INDEX]),
							Integer.parseInt(properties[ITEM_ORDAMT_INDEX]),
							Double.parseDouble(properties[ITEM_TEMP_INDEX])), 0);

				} else {
					throw new CSVFormatException();
				}
			}

		} catch (NumberFormatException | IOException | StockException e) {
			throw new CSVFormatException();
		} finally {
			closeCSVReader();
		}
	}

	/**
	 * Reads the manifest from the given filePath provided via the GUI
	 * 
	 * @param filePath of the item to be read
	 * @throws CSVFormatException if an invalid truck type is given
	 * @throws DeliveryException if an item is somehow invalid
	 */
	public static void readManifest(String filePath) throws CSVFormatException, DeliveryException {	
		Truck truck = null;
		try {
			csvReader = new BufferedReader(new FileReader(filePath));
			while ((line = csvReader.readLine()) != null) {
				String[] manifestLine = line.split(COMMA_DELIMITER);

				/**
				 * As trucks are specified first in a manifest, create a truck accordingly
				 * Refrigerated trucks can have temperature changed, so start with max temp
				 */
				if (manifestLine[MANIFEST_ITEM_INDEX].startsWith(">")) {
					if (truck != null) {
						Store.adjustCapital(-(truck.getCost()));
					}
					if (manifestLine[MANIFEST_ITEM_INDEX].startsWith(">Ordinary")) {
						truck = new OrdinaryTruck();
					} else if (manifestLine[MANIFEST_ITEM_INDEX].startsWith(">Refrigerated")) {
						truck = new RefrigeratedTruck(10);
					} else {
						throw new CSVFormatException();
					}

					/**
					 * Process items individually. If item has a temperature threshold, compare to
					 * existing temperature If temperature is less than existing truck, change
					 * temperature of truck accordingly
					 */
				} else if (!manifestLine[MANIFEST_ITEM_INDEX].startsWith(">") && manifestLine.length == 2) {
					Item item = Store.getInventory().getItem(manifestLine[MANIFEST_ITEM_INDEX]);

					if (truck.getClass() == RefrigeratedTruck.class && item.getTempThreshold() != null) {
						((RefrigeratedTruck) truck).setTemp(item.getTempThreshold());
					}
					truck.add(item, Integer.parseInt(manifestLine[MANIFEST_QUANT_INDEX]));
					Store.getInventory().add(item, Integer.parseInt(manifestLine[MANIFEST_QUANT_INDEX]));
					Store.adjustCapital(-(item.getManufactureCost() * Integer.parseInt(manifestLine[MANIFEST_QUANT_INDEX])));

				} else if (manifestLine.length != 2 && !manifestLine[MANIFEST_ITEM_INDEX].startsWith(">")) {
					throw new CSVFormatException();
				}
			}

			if (truck != null) {
				Store.adjustCapital(-(truck.getCost()));
			}
		} catch (StockException e) {
			throw new DeliveryException();
		} catch (NumberFormatException | IOException e){
			e.printStackTrace();
		} finally {
			closeCSVReader();
		}
	}

	/**
	 * Reads the sales log from the given filePath provided via the GUI
	 * 
	 * @param filePath of the item to be read
	 * @throws CSVFormatException if the format of the file is wrong
	 * @throws StockException if an item is somehow invalid
	 */
	public static void readSalesLog(String filePath) throws CSVFormatException, StockException {
		try {
			csvReader = new BufferedReader(new FileReader(filePath));
			while ((line = csvReader.readLine()) != null) {
				String[] salesLogLine = line.split(COMMA_DELIMITER);

				if (salesLogLine.length == 2) {
					Item item = Store.getInventory().getItem(salesLogLine[SALESLOG_ITEM_INDEX]);
					Store.getInventory().remove(item, Integer.parseInt(salesLogLine[SALESLOG_QUANT_INDEX]));
					Store.adjustCapital(item.getSellCost() * Integer.parseInt(salesLogLine[SALESLOG_QUANT_INDEX]));
				} else {
					throw new CSVFormatException();
				}
			}

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		} finally {
			closeCSVReader();
		}
	}

	/**
	 * Exports a manifest for all items that need to be reordered
	 * 
	 * @param filePath for the file to be exported
	 * @throws StockException if an item is somehow invalid
	 * @throws CSVFormatException if the manifestWriter fails to close
	 */
	public static void exportManifest(String filePath) throws StockException, CSVFormatException {
		manifest = new Manifest();
		sortedColdItems = new ArrayList<Item>();
		listOrdinaryItems = new ArrayList<Item>();
		RefrigeratedTruck rTruck = new RefrigeratedTruck(10);
		OrdinaryTruck oTruck = new OrdinaryTruck();
		ordinaryItems = new Stock();
		refrigeratedItems = new Stock();

		// Store all items in appropriate stock lists that need to be reordered
		for (Item item : Store.getInventory()) {
			if (Store.getInventory().reorder(item)) {
				if (item.getTempThreshold() == null) {
					ordinaryItems.add(item, item.getReorderAmount());
				} else {
					refrigeratedItems.add(item, item.getReorderAmount());
				}
			}
		}

		// Create a list of all the refrigerated items from coldest to warmest
		for (Item item : refrigeratedItems) {
			sortedColdItems.add(coldestItem());
		}
		for (Item item : ordinaryItems) {
			listOrdinaryItems.add(item);
		}

		// Loop until there are no outstanding cold items to be reordered
		while (refrigeratedItems.totalQuantity() > 0) {
			int remainingCargo = rTruck.remainingCapacity();
			String itemName = sortedColdItems.get(0).getName();
			Item item = refrigeratedItems.getItem(itemName);

			// Set the temperature of the truck
			rTruck.setTemp(sortedColdItems.get(0).getTempThreshold());

			// Remove the appropriate amount of stock by checking remaining amount of cargo
			if (refrigeratedItems.getItemQuantity(itemName) >= remainingCargo) {
				rTruck.add(item, remainingCargo);
				refrigeratedItems.remove(item, remainingCargo);
			} else { // If truck has enough space to remove all of remaining item
				rTruck.add(item, refrigeratedItems.getItemQuantity(item));
				refrigeratedItems.remove(item, refrigeratedItems.getItemQuantity(item));
				sortedColdItems.remove(0); // Shift the sortedItems left 1
			}

			// Add truck to manifest if full and start again
			if (rTruck.remainingCapacity() == 0) {
				manifest.add(rTruck);
				rTruck = new RefrigeratedTruck(10);
			}
		}

		// Fill the remainder of the last remaining refrigerated truck with ordinary
		// items
		while (rTruck.remainingCapacity() > 0 && !listOrdinaryItems.isEmpty()) {

			int remainingCargo = rTruck.remainingCapacity();
			String itemName = listOrdinaryItems.get(0).getName();
			Item item = ordinaryItems.getItem(itemName);

			if (ordinaryItems.getItemQuantity(itemName) >= remainingCargo) {
				rTruck.add(item, remainingCargo);
				ordinaryItems.remove(item, remainingCargo);
			} else { // If truck has enough space to remove all of remaining item
				rTruck.add(item, ordinaryItems.getItemQuantity(item));
				ordinaryItems.remove(item, ordinaryItems.getItemQuantity(item));
				listOrdinaryItems.remove(0); // Shift the sortedItems left 1
			}
		}
		manifest.add(rTruck);

		// Loop until there are no outstanding ordinary items to be reordered
		while (ordinaryItems.totalQuantity() > 0) {
			int remainingCargo = oTruck.remainingCapacity();
			String itemName = listOrdinaryItems.get(0).getName();
			Item item = ordinaryItems.getItem(itemName);

			// Remove the appropriate amount of stock by checking remaining amount of cargo
			if (ordinaryItems.getItemQuantity(itemName) >= remainingCargo) {
				oTruck.add(item, remainingCargo);
				ordinaryItems.remove(item, remainingCargo);
			} else { // If truck has enough space to remove all of remaining item
				oTruck.add(item, ordinaryItems.getItemQuantity(item));
				ordinaryItems.remove(item, ordinaryItems.getItemQuantity(item));
				listOrdinaryItems.remove(0); // Shift the sortedItems left 1
			}

			// Add truck to manifest if full and start again
			if (oTruck.remainingCapacity() == 0) {
				manifest.add(oTruck);
				oTruck = new OrdinaryTruck();
			}
		}
		manifest.add(oTruck);

		// Exports manifest to CSV
		FileWriter manifestWriter = null;
		try {
			manifestWriter = new FileWriter(filePath + "\\manifest.csv");
			for (Truck truck : manifest) {
				if (truck.getClass() == RefrigeratedTruck.class) {
					manifestWriter.append(">Refrigerated");
				} else {
					manifestWriter.append(">Ordinary");
				}
				manifestWriter.append(NEW_LINE_SEPARATOR);

				for (Item cargoItem : truck.getCargo()) {
					manifestWriter.append(cargoItem.getName());
					manifestWriter.append(COMMA_DELIMITER);
					manifestWriter.append(Integer.toString(truck.getCargo().getItemQuantity(cargoItem)));
					manifestWriter.append(NEW_LINE_SEPARATOR);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				manifestWriter.flush();
				manifestWriter.close();
			} catch (IOException e) {
				throw new CSVFormatException();
			}
		}
	}

	/**
	 * Returns the coldest item in the refrigerated items stock
	 * 
	 * @returns the coldest item in the stock
	 */
	private static Item coldestItem() {
		double temp = 10;
		Item coldItem = null;
		for (Item item : refrigeratedItems) {
			if (item.getTempThreshold() <= temp && !sortedColdItems.contains(item)) {
				temp = item.getTempThreshold();
				coldItem = item;
			}
		}
		return coldItem;
	}
}
