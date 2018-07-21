package accessiblesolutions.accessiblescheduling.exception;

public class BadRequestException extends Exception {
	private static final long serialVersionUID = 2L;
	private Object corruptObject = null;
	private Class corruptClass = null;
	
	public BadRequestException(Class clazz, Object object) {
		corruptClass=clazz;
		corruptObject=object;
	}

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(Throwable cause) {
		super(cause);
	}

	public BadRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadRequestException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Class getCorruptClass() {
		return corruptClass;
	}

	public void setCorruptClass(Class corruptClass) {
		this.corruptClass = corruptClass;
	}

	public Object getCorruptObject() {
		return corruptObject;
	}

	public void setCorruptObject(Object corruptObject) {
		this.corruptObject = corruptObject;
	}
}
