package assignment2.classes;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author Alexander Rozsa
 * @author Liam Edwards
 *
 */
public class GUI extends JFrame implements ActionListener, Runnable {

	private static final long serialVersionUID = -8954008955048531845L;
	public static final int WIDTH = 640; 
	public static final int HEIGHT = 480;

	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public GUI(String title) throws HeadlessException {
		super(title);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setSize(WIDTH, HEIGHT);
		
		JButton button = new JButton();
		button.setText("Click here");
		add(button);
		
		button.addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("You clicked me!");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new GUI("Inventory Manager Tycoon"));
	}

}
