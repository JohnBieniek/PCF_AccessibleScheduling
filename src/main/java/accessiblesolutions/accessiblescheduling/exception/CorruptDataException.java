package accessiblesolutions.accessiblescheduling.exception;

public class CorruptDataException extends Exception {
	private static final long serialVersionUID = 1L;
	private Object corruptObject = null;
	private Class corruptClass = null;
	
	public CorruptDataException(Class clazz, Object object) {
		corruptClass=clazz;
		corruptObject=object;
	}

	public CorruptDataException(String message) {
		super(message);
	}

	public CorruptDataException(Throwable cause) {
		super(cause);
	}

	public CorruptDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public CorruptDataException(String message, Throwable cause, boolean enableSuppression,
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
