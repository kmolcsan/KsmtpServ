//import java.io.*;
import java.net.*;

/**
 * Statement of purpose:
 * 
 * 	This application will launch an SMTP server which will listen
 *  for incoming messages on the specified port, destined to the
 *  specified domain, and will store these messages by username
 *  in the specified directory.  
 * 
 * 
 * @author Ken Molcsan Jr.
 * @version 1.0a
 * <p>Date: 2008-06-29</p>
 * 
 * 
 * <p>Description of input and output:
 * 
 * 	<br><b>input:</b><br>  
 * 			three parameters are expected to be passed in the 
 * 			following order: port to listen to, current domain
 *          name, and directory in which to place messages. The
 *          application is closed by entering any keystroke into
 *          the System.in console.
 * 	<br><b>output:</b><br> 
 * 			Any errors that are encountered will be written to the
 *          standard System.out console.
 * </p>
 * 
 * 
 * How to use:
 * 
 * 	Save the files Main.java, SmtpConnection.java, and 
 * 	SmtpStateHandler.java.  Compile using the javac.exe
 * 	application.  Run the program from a command line by typing:
 * 	'java Main "portNumber" "domainName" "existingDirectory"'.
 * 
 * 
 * Assumptions on expected data:
 * 
 * 	The directory provided must already be created prior to data
 *  being saved.  User files will be created (or appended to if
 *  already present) within this directory.
 * 
 * 
 * Test Platform:
 * 
 * 	Windows Vista Home Edition
 *  javac 1.6.0_03
 * 
 *  
 * Bibliography:
 *  	@see http://www.ietf.org/rfc/rfc0821.txt
 *  	@see http://www.ietf.org/rfc/rfc0822.txt
 */

class Main{
	private static boolean DEBUG = false;
	/**
	 * Purpose: 
	 * @author Ken Molcsan
	 * @version 1.0a
	 * 
	 * Preconditions: Must be passed three parameters consisting
	 *                of 1) port, 2) domain, and 3) directory.
	 *                Please note: the directory must already be
	 *                created on the disk.
	 * @param args Three arguments expected (port, domain, directory)
	 * @throws Exception
	 * @see SmtpServer
	 */
	public static void main(String[] args){
		try{
			System.out.println("KsmtpServ Running\n"
					+ "Port: " + args[0] + "\n"
					+ "Domain: " + args[1] + "\n"
					+ "Directory: " + args[2] + "\n"
					+ "\nType Exit to close the application");
			if(DEBUG) System.out.println(".. Starting Smtp Server");
			new SmtpServer(args[1], args[2],
					new ServerSocket(Integer.parseInt(args[0])));
			if(DEBUG) System.out.println(".. Smtp Server Started");
			/*
			BufferedReader command = new BufferedReader(new InputStreamReader(System.in));
			System.out.println(".. Server Cmd Interpreter Started");
			while(true){
				if(command.readLine().toUpperCase().startsWith("EXIT")) break;
			}
			System.out.println(".. Server Cmd Interpreter Exiting");
			command.close();
			*/
		}catch(Exception e){
			System.out.println(".. An exception occurred in the main method: "
					+ e.getMessage());
		}
		// System.exit(0);
	}
}

/*
Testing:

The server must be launched before performing tests. This is done
by executing the KsmtpServ.jar with the appropriate parameters. I.E.

c:\> java -jar KsmtpServ.jar 25 kserv.com c:\temp\mail
KsmtpServ Running ... Press any key to exit

------------------------------------------------------------------
Telnet Testing
------------------------------------------------------------------
Microsoft Telnet> open localhost 25
220 pc_mobile SMTP server ready
NOOP
250 OK
HELO
501 HELO requires a valid address
HELLO
500 unrecognized command
HELO localhost
250 pc_mobile hello 0:0:0:0:0:0:0:1 pleased to meet you
HELO
501 HELO requires a valid address
HELO localhost
250 pc_mobile hello 0:0:0:0:0:0:0:1 pleased to meet you
NOOP
250 OK
QUIT
221 pc_mobile closing connection
Connection to host lost.


Microsoft Telnet> open localhost 25
220 pc_mobile SMTP server ready
MAIL FROM:<bob@home.com>
503 Polite people say HELO first
HELO home.com
250 pc_mobile hello 0:0:0:0:0:0:0:1 pleased to meet you
MAIL FROM:bob@home.com
550 malformed address
MAIL FROM:<bob@home.com>
250 <bob@home.com> sender ok
MAIL FROM:<bill@home.com>
503 sender already specified
RSET
250 OK
MAIL FROM:<bill@home.com>
250 <bill@home.com> sender ok
RSET
250 OK
RCPT TO:<ken@kserv.com>
503 need MAIL before RCPT
MAIL FROM:<bob@home.com>
250 <bob@home.com> sender ok
RCPT OT:<ken@kserv.com>
501 syntax error in parameter scanning
RCPT TO:<ken@gmail.com>
503 Recipient rejected. Only mail destined for kserv.com can be delivered.
RCPT TO:<ken@kserv.com>
250 <ken@kserv.com> recipient ok
RCPT TO:<bill@kserv.com>
250 <bill@kserv.com> recipient ok
MAIL FROM:<james@home.com>
503 sender already specified
DATA
354 enter mail, end with '.' on a line by itself
.
250 -977568509 mail accepted for delivery
QUIT
221 pc_mobile closing connection
Connection to host lost.

------------------------------------------------------------------

C:\>java -jar KsmtpServ_1.0a.jar 25 kMail.org c:\temp\mail
KsmtpServ Running ... Press any key to exit

------------------------------------------------------------------

Microsoft Telnet> open localhost 25
220 pc_mobile SMTP server ready
data
503 Polite people say HELO first
mail from:<someguy@mailin.com>
503 Polite people say HELO first
helo
501 HELO requires valid address
helo mailServ
250 pc_mobile hello 0:0:0:0:0:0:0:1 pleased to meet you
data
503 need MAIL before DATA
rcpt
503 need MAIL before RCPT
mail
501 syntax error in parameter scanning
mail from:<someguy@home.fr>
250 <someguy@home.fr> sender ok
data
503 need RCPT before DATA
rcpt to:<warren.buffet@kMail.org>
250 <warren.buffet@kMail.org> recipient ok
data
354 enter mail, end with '.' on a line by itself
From: Some Guy
To: Warren Buffet
Subject: Order More Post-its

Your post-it supply is running low. Please order more. . . .
. . .
..
.. .. .. .. ..
.        yo
.
250 686357314 mail accepted for delivery
data
503 need MAIL before DATA
quit
221 pc_mobile closing connection
Connection to host lost.

Microsoft Telnet> open localhost 25
220 pc_mobile SMTP server ready
helo mailMan
250 pc_mobile hello 0:0:0:0:0:0:0:1 pleased to meet you
from
500 unrecognized command
mail from:<>
250 <> sender ok
rcpt to:<@kMail.org>
550 malformed address
rcpt to:<1@kMail.org>
250 <1@kMail.org> recipient ok
data
354 enter mail, end with '.' on a line by itself
..
....
.
250 350431712 mail accepted for delivery
quit
221 pc_mobile closing connection
Connection to host lost.

------------------------------------------------------------------
Testing with Kmailer_v1.0a
------------------------------------------------------------------
c:\> java -jar Kmailer_v1.0a.jar localhost g.bush@whitehouse.gov 
     ken@kMail.org
From: "President George W. Bush" <his.holiness@whitehouse.gov>
To: "Ken Molcsan" <ken@kMail.org>
Subject: Presidential Pardon

Dear Ken,

I grant you an unconditional presidential pardon.

:P

.....
....
...
..
.

Opening Connection ...
220 pc_mobile SMTP server ready
250 pc_mobile hello 127.0.0.1 pleased to meet you
250 <george.bush@whitehouse.gov> sender ok
250 <ken@kMail.org> recipient ok
354 enter mail, end with '.' on a line by itself
Closing Connection ...
250 350431712 mail accepted for delivery


c:\> java -jar Kmailer_v1.0a.jar localhost g.bush@whitehouse.gov 
     kennymolc@gmail.com
From: "President George W. Bush" <his.holiness@whitehouse.gov>
To: "Ken Molcsan" <kennymolc@gmail.com>
Subject: Presidential Pardon

Dear Ken,

I grant you an unconditional presidential pardon.

:P

.....
....
...
..
.

Opening Connection ...
220 pc_mobile SMTP server ready
250 pc_mobile hello 127.0.0.1 pleased to meet you
250 <george.bush@whitehouse.gov> sender ok
 The server has returned an error:
   -> 503 Recipient rejected. Only mail destined for kMail.org 
          can be delivered.
 Your message could not be sent.
Closing Connection ...
*/