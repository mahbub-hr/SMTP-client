package smtpskeleton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.Base64;

public class SMTPClient {

	private  static String encoder(File file){
        String encodedfile = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fis.read(bytes);
            encodedfile = new String(Base64.getEncoder().encode(bytes));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return encodedfile;
    }
	
	public static void main(String[] args) throws IOException {
		String mailServer = "webmail.buet.ac.bd";
		InetAddress mailHost = InetAddress.getByName(mailServer);
		// InetAddress localHost = InetAddress.getLocalHost();
		Socket smtpSocket = new Socket(mailHost, 25);
		smtpSocket.setSoTimeout(20*1000);
		BufferedReader in = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
		PrintWriter pr = new PrintWriter(smtpSocket.getOutputStream(), true);
		String initialID ="";
		try{
			initialID = in.readLine();
		}
		catch(SocketTimeoutException e){
			System.out.println("Time out! Try Again.");
			smtpSocket.close();
			return;
		}
		System.out.println(initialID);
		Scanner scn = new Scanner(System.in);
		String str;
		String welcome = "";
		int state = 0;
		File f = new File("hello.txt");
		String encodedByte = encoder(f);
		System.out.println("\n"+encodedByte+"\n");
		

		System.out.println("at begining state. waiting for <HELO> <hostname> commmand");
		while (true) {
			str = scn.nextLine();
			//time start 
			pr.println(str);
			if (state != 4) {
			try{
				welcome = in.readLine();
			}
			catch(SocketTimeoutException e){
				System.out.println("Time out! Try Again.");
				smtpSocket.close();
				scn.close();
				return;
			}
				System.out.println(welcome);
				if (welcome.startsWith("221")) {
					System.out.println("Closing the client...");
					smtpSocket.close();
					scn.close();
					break;
				}
			}

			switch (state) {
			case 0:

				if (welcome.startsWith("2")) {
					state = 1;
					System.out.println("going to wait state....");
				} else {
					System.out.println("Please enter <HELO> <hostname>");
				}
				break;
			case 1:
				if (welcome.startsWith("2")) {
					state = 2;
					System.out.println("going to \"envelop created\" state....");
				} else {
					System.out.println("Please enter <MAIL> <FROM>:<id@example.com>");
				}
				break;
			case 2:
				if (str.equalsIgnoreCase("RSET")) {
					System.out.println("going to wait state....");
					state = 1;
				} else if (welcome.startsWith("2")) {
					state = 3;
					System.out.println("going to \"recipient set\" state....");
				} else {
					System.out.println("Please enter <RCPT> <TO>:<id@example.com>");
				}
				break;
			case 3:
				System.out.println("in case 3");
				if (str.equalsIgnoreCase("RSET")) {
					System.out.println("going to wait state....");
					state = 1;
				} else if (welcome.startsWith("3")) {
					state = 4;
					System.out.println("Enter your messege:");
				} else {
					System.out.println("Please enter <DATA> to proceed");
				}
				break;

			case 4:
				// System.out.println("in case 4");
				// str = scn.nextLine();
				// System.out.println(str);
				while (str.trim().equals(".") == false) {
					pr.println(str);
					str = scn.nextLine();
					// System.out.println("testing dot " + str);
				}
				pr.println(".");
				pr.flush();
				state = 5;
				// System.out.println("Dot is sent! ");

			case 5:
				welcome = in.readLine();
				System.out.println(welcome);
				if (welcome.startsWith("2")) {
					state = 1;
					System.out.println("Messege Sent!\ngoing to wait state....\n");
				}
				break;
			}
		}

	}
	
	
	
}
