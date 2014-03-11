import java.io.*;
import java.net.*;

/**
 * The SmtpRequestHandler class is designed to handle an incoming
 * SMTP request as specified by RFC 821 (www.ietf.org).
 * 
 * @author Ken Molcsan Jr.
 * @version 1.0a
 * 
 * Date: 6/28/2008
 * 
 */

class SmtpRequestHandler extends Thread{
	// Private Data Members
	private String messageText;
	private String domain;
	private String directory;
	private String recipients;
	private String serverName;
	private Socket client;
	private boolean quit = false;
	private static boolean DEBUG = false;
	
	// connection I/O
	private BufferedReader input = null;
	private DataOutputStream output = null;
	
	private int requiredCmd;
	// Cmd States
	private static int HELO = 0;
	private static int MAIL = 1;
	private static int RCPT = 2;
	private static int DATA = 3;
	
	/**
	 * Purpose: The SmtpRequestHandler constructor initializes the
	 *          thread for this handler.  When called, it is 
	 *          passed the appropriate parameters to handle an
	 *          incoming request object.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions:
	 *   The request socket passed into this constructor must 
	 *   contain a valid socket connection.
	 *   
	 * @param req The socket which has been assigned to this
	 *            incoming request must be passed in.
	 * @param dom The acceptable destination domain name
	 * @param dir The directory where accepted messages must be
	 *            stored.
	 * @return SmtpRequestHandler thread
	 * @see nothing
	 * 
	 */
	SmtpRequestHandler(Socket req, String dom, String dir){
		client = req;
		domain = dom;
		directory = dir;
		messageText = "";
		recipients = "";
		requiredCmd = HELO;
		
		try{
			// Set Host Name
			serverName = InetAddress.getLocalHost().getHostName();
			// Initialize Client I/O streams
			input = new BufferedReader(
					new InputStreamReader(req.getInputStream()));
			output = new DataOutputStream(req.getOutputStream());
			
			this.start();
		}catch(Exception e){
			System.out.println(".. .. .. Client i/o exception occurred: "
					           + e.getMessage());
			try{ client.close(); } catch(Exception f) {
				if(DEBUG) System.out.println(".. .. .. Failed to Close the Client connection");
			}
		}
	}
	
	/**
	 * Purpose: The run() method is required to execute a process
	 *          thread.  For the SmtpRequestHandler, this will
	 *          launch a command interpreter for a client session.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions:
	 *   none
	 * 
	 * @return void
	 * @see nothing
	 * 
	 */
	
	public void run(){
		// Confirm that the connection was received
		sendResponse(220,serverName + " SMTP server ready");
		while(!quit){
			handleCommand();
		}
		// QUIT received, close connection nicely
		sendResponse(221,serverName + " closing connection");
		try{
			client.close();
		}catch(Exception c){
			System.out.println(".. .. .. Error closing client connection: "
					           + c.getMessage());
		}
	}
	
	/**
	 * Purpose: The handleCommand method is intended to identify
	 *          the command type and send it to the appropriate
	 *          command parse method.
	 * 
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: none
	 * 
	 * @return void
	 * @see parseHelo, parseMail, parseRcpt, parseData, parseQuit,
	 *      parseRset, parseNoop
	 */
	private void handleCommand(){
		try{
			String cmd = input.readLine();
			String[] params = {};
			if(cmd.length() > 4)
				params = cmd.substring(5).split(" ");
			String tmp = new String(cmd.toUpperCase());
			if(tmp.startsWith("HELO")){
				parseHelo(params);
			}else if(tmp.startsWith("MAIL")){
				if(heloDone()) parseMail(params);
			}else if(tmp.startsWith("RCPT")){
				if(heloDone())parseRcpt(params);
			}else if(tmp.startsWith("DATA")){
				if(heloDone())parseData(params);
			}else if(tmp.startsWith("QUIT")){
				quit = true;
			}else if(tmp.startsWith("RSET")){
				resetMsg();
				sendResponse(250, "OK");
			}else if(tmp.startsWith("NOOP")){
				sendResponse(250, "OK");
			}else if(tmp.startsWith("EXIT")){
				sendResponse(999, "Exiting Application");
				client.close();
				System.exit(0);
			}else{
				sendResponse(500,"unrecognized command");
			}
		}catch(IOException e){
			System.out.println("Error receiving client command: "
					           + e.getMessage());
			try{client.close();}catch(Exception c){}
		}
	}
	
	/**
	 * Purpose: The heloDone method will be used during command
	 *          parsing to ensure that a HELO command has been
	 *          received prior to executing message commands.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: none
	 * 
	 * @return boolean TRUE if a valid HELO command has been
	 *                 received, FALSE otherwise.
	 * @see parseHelo
	 */
	private boolean heloDone(){
		if(requiredCmd == HELO)
			sendResponse(503,"Polite people say HELO first");
		return requiredCmd > HELO;
	}
	
	/**
	 * Purpose: This method is designed to parse a HELO command
	 *          sent by the client.  The command should consist
	 *          of the HELO lexeme followed by an identifier of
	 *          the sending machine or application.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: none
	 *   
	 * @return void
	 * @see nothing
	 * 
	 */
	private void parseHelo(String[] params){
		// HELO is expecting exactly 1 parameter
		if(params.length > 0){
			sendResponse(250, serverName
					    + " hello " 
					    + client.getInetAddress().getHostName()
					    + " pleased to meet you");
			if(requiredCmd == HELO) requiredCmd = MAIL;
		}else{
			sendResponse(501, "HELO requires valid address");
		}
	}
	
	/**
	 * Purpose: This method is designed to parse a MAIL command
	 *          sent by the client.  The command should consist
	 *          of the MAIL lexeme followed by a FROM:<address>
	 *          parameter which contains the sender's address.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: none
	 *   
	 * @return void
	 * @see nothing
	 * 
	 */
	private void parseMail(String[] params){
		// Check to see whether we already specified the sender
		if(requiredCmd == MAIL){
			// MAIL expects the FROM parameter
			if(params.length > 0 
			   && params[0].toUpperCase().startsWith("FROM")){
				String addr = "";
				if(params[0].split(":").length > 1) 
					addr = params[0].split(":")[1];
				if(addr.matches("[<].*?[>]")){
					sendResponse(250,addr + " sender ok");
					// Start assembling the messageText
					messageText = params[0];
					// set the next expected command
					requiredCmd = RCPT;
				}else{
					sendResponse(550,"malformed address");
				}
			}else{
				sendResponse(501, "syntax error in parameter scanning");
			}
		}else{
			sendResponse(503, "sender already specified");
		}
	}
	
	/**
	 * Purpose: This method is designed to parse a RCPT command
	 *          sent by the client.  The command should consist
	 *          of the RCPT lexeme followed by a TO:<address>
	 *          parameter which contains the recipient address.
	 *          NOTE: the recipient address must be a mailbox
	 *          on this domain, otherwise it will be rejected.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: none
	 *   
	 * @return void
	 * @see nothing
	 * 
	 */
	private void parseRcpt(String[] params){
		if(requiredCmd < RCPT)
			sendResponse(503, "need MAIL before RCPT");
		else{
			// RCPT expects the TO: parameter
			if(params.length > 0 
			   && params[0].toUpperCase().startsWith("TO")){
				String addr = "";
				if(params[0].split(":").length > 1) 
					addr = params[0].split(":")[1];
				if(addr.matches("[<]..*?[@].*?[>]")){
					String a = addr.replaceAll("[<>]", "");
					// Check to make sure the recipient is a
					// domain mailbox
					if(a.split("@")[1].equals(domain)){
						sendResponse(250,addr + " recipient ok");
						messageText += "\n" + params[0];
						
						// set the next expected command
						if(requiredCmd == RCPT){
							requiredCmd = DATA;
							recipients = a.split("@")[0];
						}else{
							// we already have one recipient
							recipients += "," + a.split("@")[0];
						}
					}else
						sendResponse(503,"Recipient rejected. "
								    + "Only mail destined for "
								    + domain + " can be delivered.");
				}else{
					sendResponse(550,"malformed address");
				}
			}else{
				sendResponse(501, "syntax error in parameter scanning");
			}
		}
	}
	
	/**
	 * Purpose: This method is designed to parse a DATA command
	 *          sent by the client.  The initial DATA command will
	 *          trigger a response code of 314, indicating to the
	 *          client that we are ready to listen for the message
	 *          data until a character sequence of <CRLF>.<CRLF>
	 *          is received.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: MAIL and RCPT have been set
	 *   
	 * @return void
	 * @see saveMsgData
	 * 
	 */
	private void parseData(String[] params){
		if(requiredCmd < RCPT)
			sendResponse(503, "need MAIL before DATA");
		else if(requiredCmd < DATA)
			sendResponse(503, "need RCPT before DATA");
		else{
			// Notify the client that the DATA command has been
			// received, then prompt them for the message data.
			sendResponse(354, "enter mail, end with '.' on a line by itself");
			String msg = "";
			String msgLine = "";
			if(DEBUG) System.out.println("Reading DATA string");
			while(true){
				try{
					msgLine = input.readLine();
				}catch(IOException e){
					System.out.println("Error reading data from client: "
							           + e.getMessage());
				}
				if(msgLine.equals(".")) break;
				if(msgLine.startsWith("."))
					msgLine = msgLine.substring(1);
				msg += "\n" + msgLine;
			}
			if(DEBUG) System.out.println("DATA input successful");
			messageText += msg;
			// After the message has been collected, it should
			// be sent automatically
			if(DEBUG) System.out.println("Starting Message Save operation");
			saveMsgData();
			if(DEBUG) System.out.println("Message Data saved successfully");
			sendResponse(250,messageText.hashCode() 
					     + " mail accepted for delivery");
			if(DEBUG) System.out.println("Message accepted, running reset operation");
			resetMsg();
		}
	}
	
	/**
	 * Purpose: The saveMsgData method is intended to save the
	 *          sent message to the recipient(s) data-file(s)
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: valid recipient(s) have been set
	 * 
	 * @return void
	 * @see nothing
	 */
	private void saveMsgData(){
		BufferedWriter outFile = null;
		String[] users = recipients.split(",");
		for(int i=0; i < users.length; i++){
			try{
				if(DEBUG) System.out.println("Ksmtp creating file '" + users[i] + "' in directory '" + directory + "'\n");
				outFile = new BufferedWriter(
					  new FileWriter(new File(directory,users[i]),true));
				if(DEBUG) System.out.println("File creation successful. Writing to file.\n");
				outFile.append("\n" + messageText + "\n");
				if(DEBUG) System.out.println("File write successful. Closing file.\n");
				outFile.close();
			}catch(IOException e){
				System.out.println("Error writing user file: "
						           + users[i] + "\n"
						           + e.getMessage());
			}
		}
	}
	
	/**
	 * Purpose: The sendResponse method will format the response
	 *          to the client based on the provided status code
	 *          and message text.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 *          
	 * @param code Status code to indicate whether the operation
	 *             was a success.
	 * @param message Human readable message describing the status
	 * 
	 * Preconditions:
	 *   none
	 *   
	 * @return void
	 * @see nothing
	 */
	private void sendResponse(int code, String message){
		try{
			output.writeBytes(code + " " + message + "\r\n");
		}catch(IOException e){
			System.out.println("Error sending response: "
					           + e.getMessage());
		}
	}
	
	/**
	 * Purpose: Resetting the message whether by a RSET command,
	 *          or by reaching the end of message transmission
	 *          should be handled in the same way.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: none
	 * 
	 * @return void
	 * @see nothing
	 */
	private void resetMsg(){
		if(heloDone()){
			requiredCmd = MAIL;
			messageText = "";
		}
	}
}