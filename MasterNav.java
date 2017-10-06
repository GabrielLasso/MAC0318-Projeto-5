import lejos.pc.comm.*;
import java.util.Scanner;
import java.io.*;
import lejos.geom.*;

public class MasterNav {
	private static final byte ADD_POINT = 0; //adds waypoint to path
	private static final byte TRAVEL_PATH = 1; // enables slave to execute the path
	private static final byte STATUS = 2; // enquires about slave's position
	private static final byte STOP = 3; // closes communication

	private NXTComm nxtComm;
	private DataOutputStream dos;
	private DataInputStream dis;

	private static final String NXT_ID = "NXT05"; // NXT BRICK ID

	public float sendCommand(byte command, float paramX, float paramY) {
		try {
			dos.writeByte(command);
			dos.writeFloat(paramX);
			dos.writeFloat(paramY);
			dos.flush();
			return dis.readFloat();
		} catch (IOException ioe) {
			System.err.println("IO Exception");
			System.exit(1);
			return -1f;
		}
	}
	public boolean sendCommand(byte command) {
		try {
			dos.writeByte(command);
			dos.flush();
			return dis.readBoolean();
		} catch (IOException ioe) {
			System.err.println("IO Exception");
			System.exit(1);
			return false;
		}
	}

	public void connect() {
		try {
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
			/* Uncomment next line for Bluetooth communication */
			// NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			NXTInfo[] nxtInfo = nxtComm.search(MasterNav.NXT_ID);

			if (nxtInfo.length == 0) {
				System.err.println("NO NXT found");
				System.exit(1);
			}

			if (!nxtComm.open(nxtInfo[0])) {
				System.err.println("Failed to open NXT");
				System.exit(1);
			}

			dis = new DataInputStream(nxtComm.getInputStream());
			dos = new DataOutputStream(nxtComm.getOutputStream());

		} catch (NXTCommException e) {
			System.err.println("NXTComm Exception: "  + e.getMessage());
			System.exit(1);
		}
	}

	public void close() {
		try {
			dos.writeByte(STOP);
			dos.writeFloat(0f);
			dos.writeFloat(0f);
			dos.flush();
			Thread.sleep(200);
			System.exit(0);
		} catch (Exception ioe) {
			System.err.println("IO Exception");
		}
	}
}
