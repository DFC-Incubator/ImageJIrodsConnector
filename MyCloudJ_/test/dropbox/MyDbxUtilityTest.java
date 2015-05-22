package dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.junit.Test;

import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

import com.dropbox.core.DbxException;

import dropbox_backend.DropboxOperations;

public class MyDbxUtilityTest {
	
	@Test
	public void testDbxUploadFile() {
		// Object of class DbxUtility
		CloudOperations cloudHandler = new DropboxOperations();
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
			((DropboxOperations)cloudHandler).DbxLinkUser(code);

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
		CloudOperations cloudHandler = new DropboxOperations();
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
			((DropboxOperations)cloudHandler).DbxLinkUser(code);

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
		CloudOperations cloudHandler = new DropboxOperations();
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
			((DropboxOperations)cloudHandler).DbxLinkUser(code);

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
		CloudOperations cloudHandler = new DropboxOperations();
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
			((DropboxOperations)cloudHandler).DbxLinkUser(code);
	
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