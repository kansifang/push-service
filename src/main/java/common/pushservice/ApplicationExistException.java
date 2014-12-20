package common.pushservice;

public class ApplicationExistException extends Exception {
	public ApplicationExistException(String app) {
		super(app + " exists.");
	}
}
