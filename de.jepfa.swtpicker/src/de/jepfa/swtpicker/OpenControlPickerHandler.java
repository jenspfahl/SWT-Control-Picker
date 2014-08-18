package de.jepfa.swtpicker;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;


/**
 * @author Jens Pfahl
 */
public class OpenControlPickerHandler extends AbstractHandler {

	private static PickerDialog pickerDialog;

	@Override
	public synchronized Object execute(ExecutionEvent event) throws ExecutionException {
		
		if (pickerDialog != null) {
			pickerDialog.handleToggle();
			return null;
		}
		
		try {
			pickerDialog = new PickerDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			pickerDialog.open();
		} finally {
			pickerDialog = null;
		}
		
		return null;
	}

}
