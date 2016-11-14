package torrentClient;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.swing.MigLayout;

public class TorrentClient {

	public static JFrame frmRemoteTorrentDownloader;
	private JTextField searchBox;
	private JComboBox<String> downloadType;
	private JScrollPane scrollPane;
	private JPanel resultsPanel;


	private List<JLabel> numbers = null;
	private List<JCheckBox> checkBoxes = null;
	private List<JLabel> seeds = null;
	private List<String> magnets = null;
	private JButton downloadButton;
	private JButton searchButton;
	private JLabel backgroundImageLabel;

	

	public final static double version = 	1.00;
	private String ipAddress = 				"";
	private int port = 						0;
	private JMenuBar menuBar;
	private JMenu mnOptions;
	private JMenuItem ipMenuItem;
	private JMenuItem portMenuItem;
	private JMenu mnHelp;
	private JMenuItem aboutMenuItem;
	private JMenuItem checkForUpdatesMenuItem;
	private Preferences prefs;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
			public void run() {
				try {
					TorrentClient window = new TorrentClient();
					window.frmRemoteTorrentDownloader.setVisible(true);
					AutoUpdate.checkForUpdates("LAUNCH");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TorrentClient() 
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initialize() {
		//Set look and feel if not on windows.
		if(!System.getProperty("os.name").toLowerCase().contains("windows"))
		{
			try 
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} 
			catch (ClassNotFoundException e1) 			{e1.printStackTrace();} 
			catch (InstantiationException e1) 			{e1.printStackTrace();} 
			catch (IllegalAccessException e1) 			{e1.printStackTrace();} 
			catch (UnsupportedLookAndFeelException e1) 	{e1.printStackTrace();}
		}
		else
		{
			//Basically, if it is windows, do nothing and let it default to Java look and feel.
		}

		frmRemoteTorrentDownloader = new JFrame();
		frmRemoteTorrentDownloader.setIconImage(Toolkit.getDefaultToolkit().getImage(TorrentClient.class.getResource("/res/icon.png")));
		frmRemoteTorrentDownloader.setTitle("Remote Torrent Downloader V" + version);
		frmRemoteTorrentDownloader.setBounds(100, 100, 440, 610);  //Height for logo 1 = 600
		frmRemoteTorrentDownloader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRemoteTorrentDownloader.getContentPane().setLayout(new MigLayout("","[][100]","[][][400]"));

		searchBox = new JTextField();
		searchBox.setToolTipText("Your search query");
		frmRemoteTorrentDownloader.getContentPane().add(searchBox, "cell 0 0,pushx ,growx,aligny center");
		searchBox.setColumns(10);

		searchButton = new JButton("Search");
		frmRemoteTorrentDownloader.getContentPane().add(searchButton, "cell 1 0,growx,alignx right,aligny top,wrap");


		//ActionListeners for UI components.
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				initiateSearch();
			}
		});

		downloadType = new JComboBox();
		downloadType.setToolTipText("Select what type of item you wish to download.  This is critical in order for Plex to correctly organize the new content!");
		downloadType.setModel(new DefaultComboBoxModel(new String[] {"Movie", "TV Show"}));
		frmRemoteTorrentDownloader.getContentPane().add(downloadType, "cell 0 1,pushx ,growx,aligny center");

		downloadButton = new JButton("Download");
		downloadButton.setToolTipText("Send the selected items to the server to be downloaded");
		frmRemoteTorrentDownloader.getContentPane().add(downloadButton, "cell 1 1, growx, alignx right, wrap");

		scrollPane = new JScrollPane();
		frmRemoteTorrentDownloader.getContentPane().add(scrollPane, "cell 0 2 5 1,push ,grow");

		resultsPanel = new JPanel();
		scrollPane.setViewportView(resultsPanel);
		resultsPanel.setLayout(new MigLayout("", "[]", "[][][][]"));

		resultsPanel.add(new JLabel("Your results will appear here."), "cell 0 0");
		
		backgroundImageLabel = new JLabel("");
		
		//backgroundImageLabel.setIcon(new ImageIcon(TorrentClient.class.getResource("/res/icon.png")));
		backgroundImageLabel.setIcon(
				new ImageIcon(
						new ImageIcon(
								TorrentClient
								.class
								.getResource("/res/pbaylogo2.png"))
								.getImage()
								.getScaledInstance(375, 427, Image.SCALE_SMOOTH)));  //height for logo 1 = 361
		
		resultsPanel.add(backgroundImageLabel, "cell 0 1,push ,alignx center,grow,wrap");
		
		menuBar = new JMenuBar();
		frmRemoteTorrentDownloader.setJMenuBar(menuBar);
		
		mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);
		mnOptions.setToolTipText("Configure network settings.");
		
		ipMenuItem = new JMenuItem("Change Server IP Address");
		mnOptions.add(ipMenuItem);
		
		portMenuItem = new JMenuItem("Change Server Port");
		mnOptions.add(portMenuItem);
		
		
		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		aboutMenuItem = new JMenuItem("About");
		mnHelp.add(aboutMenuItem);
		aboutMenuItem.setToolTipText("Displays some basic information about the program");
		
		checkForUpdatesMenuItem = new JMenuItem("Check for updates");
		mnHelp.add(checkForUpdatesMenuItem);
		
		prefs = Preferences.userNodeForPackage(this.getClass());
		
		ipAddress = prefs.get("IP", ipAddress);
		port = prefs.getInt("PORT", port);
		
		/*
		lblThePirateBay = new JLabel("The Pirate Bay");
		lblThePirateBay.setForeground(Color.BLACK);
		lblThePirateBay.setFont(new Font("Blackadder ITC", Font.BOLD | Font.ITALIC, 60));
		lblThePirateBay.setHorizontalAlignment(SwingConstants.CENTER);
		resultsPanel.add(lblThePirateBay, "cell 0 2, span, alignx center, growx, wrap");
		*/
		
		//frmRemoteTorrentDownloader.pack();
		//backgroundImageLabel.setBounds(backgroundImageLabel.getX(), backgroundImageLabel.getY(), 640, 480);

		
		//Create the keyboard listener for pressing enter in the search box.
		searchBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				initiateSearch();
			}
		});

		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				sendDownload();
			}
		});
		
		ipMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				String newIP = (String) JOptionPane.showInputDialog(frmRemoteTorrentDownloader, "Please enter the IP address of the server.", "IP Address", JOptionPane.QUESTION_MESSAGE, null, null, prefs.get("IP", ipAddress));
				if(newIP==null){
					System.out.println("they pressed cancel");
				}
				else if(newIP.isEmpty()){
					System.out.println("It's empty.");
				}
				else{
					System.out.println("Not empty");
					ipAddress=newIP;
					prefs.put("IP", ipAddress);
					try {
						prefs.flush();
					} catch (BackingStoreException e1) {
						JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "There was an error storing the values.  You may need\nto set them again the next time you run the program!", "Options Error", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		
		portMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				String newPort = (String) JOptionPane.showInputDialog(frmRemoteTorrentDownloader, "Please enter the port of the server.", "Port", JOptionPane.QUESTION_MESSAGE, null, null, prefs.getInt("PORT", port));
				if(newPort==null){
				}
				else if(newPort.isEmpty()){
				}
				else{
					port=Integer.parseInt(newPort);
					prefs.putInt("PORT", port);
					try {
						prefs.flush();
					} catch (BackingStoreException e1) {
						JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "There was an error storing the values.  You may need\nto set them again the next time you run the program!", "Options Error", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "This program allows you to browse the Pirate Bay and select\n"
																		+ "torrents to be downloaded on a remote machine.  PLEX media\n"
																		+ "server is running on the remote machine, so completed downloads\n"
																		+ "are automatically added to the PLEX server.", "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		checkForUpdatesMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				AutoUpdate.checkForUpdates("MENU");
			}
		});
		
		
		
		
		
	}

	private void initiateSearch()
	{	
		//Disable the search button
		searchButton.setEnabled(false);
		searchButton.setText("Searching");
		
		//Run it in a thread.
		Runnable r = new Runnable(){
			@Override
			public void run() {
				//The actual search stuff starts here.

				List<List<String>> results = null;

				if(!searchBox.getText().contains(","))
				{
					/*
					if(!remoteQueryToggle.isSelected()) 
					{
						//This is local search.
						if(searcher==null) searcher = new TorrentSearcher();
						results = searcher.searchTPB(searchBox.getText());
					}
					 */

					//This is remote search.
					results = remoteSearch(searchBox.getText());


					//Make sure results exist and populate the scrollPane.
					if(results!=null && !results.isEmpty() && !results.get(0).isEmpty())
					{
						//results must contain data, so put it into the scrollPane.
						displayResults(results);
					}
					else
					{
						JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "Sorry, there were no results", "No Results", JOptionPane.INFORMATION_MESSAGE);
					}

				}
				else
				{
					//Search query contains a comma.
					JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "Your query appears to contain a comma.  Please try again without a comma", "Error", JOptionPane.ERROR_MESSAGE);
				}

				//Use invokeLater() to redraw the GUI on the Event Dispatch Thread,
				//which is where the GUI components live.
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						searchButton.setText("Search");
						searchButton.setEnabled(true);
						resultsPanel.repaint();
						//scrollPane.setSize(scrollPane.getPreferredSize().width+50, scrollPane.getPreferredSize().height);
						scrollPane.repaint();
						//frmRemoteTorrentDownloader.pack();
						frmRemoteTorrentDownloader.setSize(scrollPane.getPreferredSize().width+50, frmRemoteTorrentDownloader.getHeight());
						frmRemoteTorrentDownloader.getContentPane().repaint();
						frmRemoteTorrentDownloader.revalidate(); //Most important fucking line ever
						frmRemoteTorrentDownloader.repaint();
					}
				});
			}
		};
		new Thread(r).start();
	}

	@SuppressWarnings("unchecked")
	private List<List<String>> remoteSearch(String query)
	{
		List<List<String>> results = null;

		try 
		{
			Socket socket = new Socket(ipAddress,port);
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			out.println("QUERY," + query);
			out.flush();

			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(socket.getInputStream()));
			results = (List<List<String>>) decoder.readObject();


			decoder.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "There was an error retreiving the results from the remote server.\nPlease check your internet connection or contact Cyle at 757-903-5747", "Connection Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		return results;
	}

	private void displayResults(List<List<String>> results)
	{
		numbers = new ArrayList<JLabel>();
		checkBoxes = new ArrayList<JCheckBox>();
		seeds = new ArrayList<JLabel>();
		magnets = new ArrayList<String>();

		resultsPanel.removeAll();

		for(int k=0; k<results.get(0).size(); k++)
		{
			numbers.add(new JLabel((k+1)+"."));
			resultsPanel.add(numbers.get(k));
			numbers.get(k).setVisible(true);

			checkBoxes.add(new JCheckBox(results.get(0).get(k)));
			resultsPanel.add(checkBoxes.get(k));
			checkBoxes.get(k).setVisible(true);

			seeds.add(new JLabel("Seeds: " + results.get(1).get(k) + "   Leeches: " + results.get(2).get(k)));
			resultsPanel.add(seeds.get(k),"wrap");
			seeds.get(k).setVisible(true);

			magnets.add(results.get(3).get(k));
		}
		//resultsPanel.repaint();
		//frmRemoteTorrentDownloader.pack();
		//frmRemoteTorrentDownloader.getContentPane().repaint();
		//frmRemoteTorrentDownloader.repaint();
	}

	private void sendDownload()
	{
		String type = (String) downloadType.getSelectedItem();
		if(type.equals("Movie")) type="MOVIE";
		else if(type.equals("TV Show")) type="TV";

		List<Integer> downloads = new ArrayList<Integer>();

		for(int i=0; i<numbers.size(); i++)
		{
			if(checkBoxes.get(i).isSelected())
			{
				downloads.add(i);
			}
		}


		for(int r=0; r<downloads.size(); r++)
		{
			try {
				Socket socket = new Socket("cylesmovies.noip.me",25252);
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				//remove commas from title
				if(checkBoxes.get(downloads.get(r)).getText().contains(",")) checkBoxes.get(downloads.get(r)).setText(checkBoxes.get(downloads.get(r)).getText().replace(",", ""));

				out.println(type+","+checkBoxes.get(downloads.get(r)).getText()+","+magnets.get(downloads.get(r)));
				out.flush();
				out.close();
				socket.close();

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "Server not found; could not begin download.", "Connection Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "Connection error; could not begin download.", "Connection Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

		JOptionPane.showMessageDialog(frmRemoteTorrentDownloader, "Download initiated! Please check the Plex server occasionally.\nPlease note that the items may show up in Plex before they are 100% finished downloading.\nIf they do not play immediately, try again in a few minutes.", "Download Initiated!", JOptionPane.INFORMATION_MESSAGE);
	}

}












