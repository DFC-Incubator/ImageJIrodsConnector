package DbxUtils;

import generalUtils.CloudException;
import generalUtils.CloudOperations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.junit.Test;

import com.dropbox.core.DbxException;

import dbxUtils.DbxUtility;

public class MyDbxUtilityTest {
	
	@Test
	public void testDbxUploadFile() {
		// Object of class DbxUtility
		CloudOperations cloudHandler = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			cloudHandler.login();
		} catch (CloudException e) {
			e.printStackTrace();
		}
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			((DbxUtility)cloudHandler).DbxLinkUser(code);

			// Upload a file
			// obj.DbxUploadFile("/Users/mathuratin/Desktop/travel_0013.jpg", "/");
			// obj.DbxUploadFile("<ABSOLUTE_FILE_PATH>", "<TARGET_DROPBOX_PATH>");
		} catch (CloudException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDbxUploadFolder() {
		// Object of class DbxUtility
		CloudOperations cloudHandler = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			cloudHandler.login();
		} catch (CloudException e) {
			e.printStackTrace();
		}
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			((DbxUtility)cloudHandler).DbxLinkUser(code);

			// Upload a file
			// obj.DbxUploadFile("/Users/mathuratin/Desktop/travel_0013.jpg", "/");
			// obj.DbxUploadFile("<ABSOLUTE_FILE_PATH>", "<TARGET_DROPBOX_PATH>");
		} catch (CloudException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDbxDownloadFile() {
		// Object of class DbxUtility
		CloudOperations cloudHandler = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			cloudHandler.login();
		} catch (CloudException e) {
			e.printStackTrace();
		}
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			((DbxUtility)cloudHandler).DbxLinkUser(code);

			// Upload a file
			// obj.DbxUploadFile("/Users/mathuratin/Desktop/travel_0013.jpg", "/");
			// obj.DbxUploadFile("<ABSOLUTE_FILE_PATH>", "<TARGET_DROPBOX_PATH>");
		} catch (CloudException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDbxDownloadFolder() {
		// Object of class DbxUtility
		CloudOperations cloudHandler = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			cloudHandler.login();
		} catch (CloudException e) {
			e.printStackTrace();
		}
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			((DbxUtility)cloudHandler).DbxLinkUser(code);
	
			// Upload a file
			// obj.DbxUploadFile("/Users/mathuratin/Desktop/travel_0013.jpg", "/");
			// obj.DbxUploadFile("<ABSOLUTE_FILE_PATH>", "<TARGET_DROPBOX_PATH>");
		} catch (CloudException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}