package chau.moudule;

@SuppressWarnings("serial")
public class LinphoneCoreException extends Exception {

	public LinphoneCoreException() {
	}

	public LinphoneCoreException(String detailMessage) {
		super(detailMessage);
	}

	public LinphoneCoreException(Throwable e) {
		super(e);
	}

	public LinphoneCoreException(String detailMessage,Throwable e) {
		super(detailMessage,e);
	}
}
