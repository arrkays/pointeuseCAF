import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Main {

	static SerialPort serialPort;
	static String serialPortName ="";
	static String serveurPath ="";
	static Scanner in = new Scanner(System.in);
	
	public static void main(String[] args) {
		
		getParam(args);
		OpenSerial();
		System.out.println("démarage de l'écoute");
		
	}
	
	public static void OpenSerial() {
		
		serialPort = new SerialPort(serialPortName); 
	    try {
	        serialPort.openPort();//Open port
	        serialPort.setParams(9600, 8, 1, 0);//Set params
	        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
	        serialPort.setEventsMask(mask);//Set mask
	        
	        //creation d'un event lors de la rception d'un  
	        serialPort.addEventListener(new SerialPortEventListener() {
				
				@Override
				public void serialEvent(SerialPortEvent e) {
					 try {
						byte buffer[] = serialPort.readBytes(e.getEventValue());
						String msg = new String(buffer);
						eval(msg);
					} catch (SerialPortException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
	    }
	    catch (SerialPortException ex) {
	        System.out.println(ex);
	    }
	}
	
	/**
	 * évalu le string recu sur le port serie
	 * puis l'envoi sur le serveur web
	 * @param msg
	 */
	final static void eval(String msg) {
		String[] info = msg.split(" ");//ID, NOM, PRENOM
		
		//faire des tests 
		if(info.length > 2) {
			//envoie sur srv apache
			wget(serveurPath+"?id="+URLEncoder.encode(info[0])+"&nom="+URLEncoder.encode(info[1])+"&prenom="+URLEncoder.encode(info[2]));
			affichage(info[1]+" "+info[2]);
		}
		
	}

	/**
	 * gère l'affichage
	 * @param msg
	 */
	private static void affichage(String msg) {
		// TODO 
		System.out.println(msg);
	}
	
	
	/**
	 * Cherche les parametres:
	 * 
	 * -p port serie
	 * -s serveur
	 */
	private static void getParam(String args[]) {
		for(int i= 0; i<args.length; i++) {
			
			if(args[i].equals("-p")) {
				i++;
				serialPortName = args[i];
			}
			
			if(args[i].equals("-s")) {
				i++;
				serveurPath = args[i];
			}
		}
		
		
		//si pas de port trouver en demander un
		String[] portNames = SerialPortList.getPortNames();
		if(serialPortName.equals(""))//si port serie pas en paramètre le choisir
		{
			//liste les port serie
			System.out.println("liste des ports series disponibles:");
		    for(int i = 0; i < portNames.length; i++){
		        System.out.println((i+1)+": "+portNames[i]);
		    }
		    System.out.print("\nfais ton choix:");
		    final int choix = in.nextInt()-1;
		    serialPortName = portNames[choix];
		}
		
		//si pas de serveur trouver en demander un
		if(serveurPath.equals("")) {
			System.out.println("donner le chemin complet du fichier php d'insertion dans la base de données ex: http://127.0.0.1/pointeuse/insert/addLine.php");
			serveurPath = in.next();
		}
	}
	
	//UTILS
	public static void wget(String a) {
		URL url;

        try {
            // get URL content

            //String a="http://localhost:8080/TestWeb/index.jsp";
            url = new URL(a);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                    System.out.println(inputLine);
            }
            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

	}
	
	public static void sendQuerry() {
		try {
            String url = "jdbc:msql://localhost:3306/CAF";
            Connection conn = DriverManager.getConnection(url,"flo","");
            Statement stmt = conn.createStatement();
 
            stmt.executeQuery("INSERT INTO `test` (`id`, `nom`) VALUES (NULL, 'test');");
            conn.close();
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
	}
	
}
