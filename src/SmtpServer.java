import java.net.*;

/**
 * The SmtpServer Class is designed to listen for an incoming Smtp
 * request, and spawn an SmtpRequestHandler object to manage the
 * remote request.
 * 
 * @author Ken Molcsan Jr.
 * @version 1.0a
 * 
 * Date: 6/28/2008
 * 
 */

class SmtpServer extends Thread{
	// Private Data Members
	//private int portNum;
	private String domainName;
	private String messageDir;
	private static boolean DEBUG = false;
	private ServerSocket listener = null;
	
	/**
	 * Purpose: The SmtpServer constructor will instantiate an
	 *          SmtpServer objects with the requested parameters.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: 
	 *   none
	 * 
	 * @return SmtpServer object
	 * @see nothing
	 * 
	 */
	SmtpServer(String domain, String directory, ServerSocket s){
		domainName = domain;
		messageDir = directory;
		listener = s;
		
		this.start();
	}
	
	/**
	 * Purpose: The run() method is the method that will be called
	 *          by the Thread start() method after the thread has
	 *          been instantiated.  For this server thread, run
	 *          will call the listener.
	 * 
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions
	 *   none
	 *   
	 * @return void
	 * @see SmtpServer.listen()
	 * 
	 */
	public void run(){
		if(DEBUG) System.out.println(".. .. The Smtp Server Thread is being run.");
		listen();
	}
	
	/**
	 * Purpose: The listen() method initializes the server
	 *          socket listener and waits for an incoming
	 *          connection.  When a connection is received, an
	 *          SmtpRequestHandler thread will be triggered, and
	 *          the listener will continue to listen for incoming
	 *          requests.
	 *          
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions:
	 *   The SmtpServer object must be initialized with the 
	 *   correct port for the listener to work correctly. The
	 *   domain name and message directory will be handled by an
	 *   instantiated SmtpRequestHandler thread.
	 *   
	 * @return void
	 * @see SmtpRequestHandler
	 * 
	 */
	public void listen(){
		//ServerSocket listener = null;
		
		// Initialize the listener, exit with an error if we fail.
		/*
		try{
			listener = new ServerSocket(portNum);
		}catch(Exception e){
			System.out.println(".. .. A fatal error occured. \n"
					           + e.getMessage());
			return;
		}
		*/
		
		// Start listening
		while(true){
			try{
				// block until request encountered
				Socket request = listener.accept();
				if(DEBUG) System.out.println(".. .. Connection received from: " 
						+ request.getInetAddress().getCanonicalHostName() + "\n");
				
				
				// Trigger the request handler thread, then 
				// continue listening
				if(DEBUG) System.out.println(".. .. Launching SmtpRequestHandler.");
				new SmtpRequestHandler(request, domainName, messageDir);
				if(DEBUG) System.out.println(".. .. SmtpRequestHandler completed.");
			}catch(Exception le){
				System.out.println(".. .. An error occurred in the Smtp Server listener: " + le.getMessage());
				break;
			}
		}
	}
}