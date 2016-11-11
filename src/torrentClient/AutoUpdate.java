package torrentClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

public class AutoUpdate 
{
	private static String data=null;
	private static double newVersion=0.0;
	
	public static void checkForUpdates(String callBackLocation)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run() 
			{
				double remoteVersion = 0.0;
				
				URL url;
				try {
					url = new URL("http://islandpi.noip.me:8080/torrentclient/version.html");
					InputStream html = null;

			        html = url.openStream();
			        
			        int c = 0;
			        StringBuffer buffer = new StringBuffer("");

			        while(c != -1) {
			            c = html.read();
			            
			        buffer.append((char)c);
			        }
			        
			        data = buffer.toString();
			        data = data.substring(data.indexOf("id=\"version\">")+13, data.indexOf("</div>"));
			        remoteVersion = Double.parseDouble(data.substring(1));
			        
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
		        
				
				//Make the decision on what to do.
				if(remoteVersion!=0.0 && remoteVersion==TorrentClient.version)
				{
					if(callBackLocation.equals("LAUNCH"))
					{
						System.out.println("You have the latest version.");
					}
					else if(callBackLocation.equals("MENU"))
					{
						JOptionPane.showMessageDialog(TorrentClient.frmRemoteTorrentDownloader, "You have the latest version!", "Up to date", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else if(remoteVersion!=0.0 && remoteVersion>TorrentClient.version)
				{
					System.out.println("You do not have the latest version.  Please update");
					int updateOrNah = JOptionPane.showConfirmDialog(TorrentClient.frmRemoteTorrentDownloader, "There is a new version available! Do you wish to update?\nYour version: V" + TorrentClient.version + "\nLatest version: V" + remoteVersion, "Please Update", JOptionPane.YES_NO_OPTION);
					
					if(updateOrNah==0)
					{
						//true; update
						newVersion = remoteVersion;
						update();
					}
					else
					{
						//They don't want to update. Fuck em.
					}
				}
				else if(remoteVersion==0.0)
				{
					System.out.println("There was an error contacting the update server.");
					JOptionPane.showMessageDialog(TorrentClient.frmRemoteTorrentDownloader, "Could not check for latest version!\nThis probably means that the rest of the program won't work either.\nCheck your connection and try again.", "Update Error", JOptionPane.ERROR_MESSAGE);
				}				
			}
		};
		
		new Thread(r).start();
	}
	
	private static void update()
	{
		createBatch();
		
		//Download the new jar file
		try {
			FileUtils.copyURLToFile(new URL("http://islandpi.noip.me:8080/torrentclient/update.jar"), new File("update.jar"));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(TorrentClient.frmRemoteTorrentDownloader, "Download failed!", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		//Run the batch file
		
		ProcessBuilder builder = new ProcessBuilder("cmd.exe","/C","start","update.bat");
		try 
		{
			builder.start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//close it all out now.
		System.exit(0);
	}
	
	private static void createBatch()
	{
		File batch = new File("update.bat");
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(batch));
			
			out.println("@ECHO off");
			out.println("echo Updating, please wait...");
			
			//Get current file name
			//Must use main class file.
			File dir = new File(TorrentClient.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			String jar = System.getProperty("java.class.path");
			File f = new File(dir, jar);
			//JOptionPane.showMessageDialog(null, f.getName());
			//System.out.println(f.getName());
			String jarName = f.getName();
			
			
			out.println("del \"" + jarName + "\"");
			
			out.println("rename update.jar \"Torrent Client V" + newVersion + ".jar\"");
			
			//This line deletes itself.
			
			out.println("start javaw -jar \"Torrent Client V" + newVersion + ".jar\"");
			out.println("echo Done updating!  You can close this window now");
			out.println("(goto) 2>nul & del \"%~f0\" & exit");
			
			
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
