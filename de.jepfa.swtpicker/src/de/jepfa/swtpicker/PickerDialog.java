package de.jepfa.swtpicker;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


/**
 * @author Jens Pfahl
 */
public class PickerDialog extends Dialog {

	private static final String CHOOSE_COLOR_TEXT = "Choose Picker color <a href=\"example.org\">here</a>. Current is : ";

	private static final Map<String, Integer> SWT_CONSTS = new HashMap<>();
	static {
		String[] swtConstNames = new String[] {"NONE", "PUSH", "TOGGLE"}; //TODO only Style Constants grouped by control
		
		Field[] swtFields = SWT.class.getFields();
        for (Field field : swtFields) {
        	for (String swtConstName : swtConstNames) {
        		if (swtConstName.equals(field.getName())) { // TODO also check of current control
	        		try {
	        			int value = field.getInt(null);
	        			SWT_CONSTS.put(field.getName(), value);
	        		}
	        		catch (Exception e) {
	        			// do nothing
	        		}
        		}
			}
		}
	}
	
	private Color lastColor;
    private Control lastControl;
    
    private Color lastSelectedColor;
    private Control lastSelectedControl;
    

    private class MyMouseListener implements MouseMoveListener {


        @Override
        public void mouseMove(MouseEvent e) {
            handleEvent(e);
        }


        private synchronized void handleEvent(MouseEvent e) {
            if (getShell() == null || getShell().isDisposed()) {
            	return;
            }
            
            if (e.getSource() == txtDump) {
            	return;
            }
            
            Control currentControl = (Control)e.getSource();
            if (currentControl == lastControl && lastControl != null) {
            	return;
            }

            
            for (TreeItem item : txtDump.getItems()) {
				item.dispose();
			}
            
            addTreeItem(currentControl, null, true);
			//StringBuilder text = getString(currentControl, 0);
            
            //txtDump.setText(text.toString());
            
            for (final TreeItem item : txtDump.getItems()) {
                item.setExpanded(true);
            }
            
            if (lastControl != null && !lastControl.isDisposed()) {
                lastControl.setBackground(lastColor);
            }

            lastControl = currentControl;
            
            lastColor = currentControl.getBackground();
            currentControl.setBackground(color);
            
        }


    }

    private MyMouseListener mouseListener = new MyMouseListener();

    private Label labelStatus;
    private Tree txtDump;
	private Button btnStartStop;
	
	private int controlCount;

	private static Color color = null;

  
	public PickerDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle((getShellStyle() | SWT.RESIZE)^ SWT.APPLICATION_MODAL);
    }


    @Override
    protected Control createDialogArea(Composite parent) {
    	getShell().setText("SWT Control Picker");
    	
    	if (color == null) {
    		color = getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE);
    	}
        labelStatus = new Label(parent, SWT.NONE);
        labelStatus.setText("Press 'Start' or CRTL-ALT-SHIFT-S to pick Controls under the cursor.\nTo stop them, click 'Stop' or CRTL-ALT-SHIFT-S again.");
        labelStatus.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        
        //txtDump = new Text(parent, SWT.MULTI | SWT.WRAP| SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
        txtDump = new Tree(parent, SWT.NONE | SWT.V_SCROLL | SWT.BORDER);
        txtDump.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 300).create());
        txtDump.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        txtDump.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!e.item.isDisposed()) {
					Control control = (Control) e.item.getData();
					if (!control.isDisposed()) {
						resetManuallySelectedControl();
						
			            lastSelectedControl = control;
			            lastSelectedColor = control.getBackground();
		            
		            	control.setBackground(color);
		            }
				}
			}
		});
               
        btnStartStop = new Button(parent, SWT.TOGGLE);
        btnStartStop.setText("Start");
        btnStartStop.setLayoutData(GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).create());
        
        btnStartStop.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		boolean btnSelectionState = btnStartStop.getSelection();
        		handleToggle(btnSelectionState);
        	}
		});
        
        new Label(parent, SWT.NONE);
        
        final Link colorLink = new Link(parent, SWT.NONE);
        colorLink.setText(CHOOSE_COLOR_TEXT + color.getRGB());
        colorLink.setForeground(color);
        
        colorLink.addSelectionListener(new SelectionAdapter() {
          @Override
		public void widgetSelected(SelectionEvent event) {
            // Create the color-change dialog
            ColorDialog dlg = new ColorDialog(getShell());

            // Set the selected color in the dialog from
            // user's selected color
            dlg.setRGB(colorLink.getForeground().getRGB());

            // Change the title bar text
            dlg.setText("Choose a Color");

            // Open the dialog and retrieve the selected color
            RGB rgb = dlg.open();
            if (rgb != null) {
              // Dispose the old color, create the
              // new one, and set into the label
              color.dispose();
              color = new Color(getShell().getDisplay(), rgb);
              colorLink.setText(CHOOSE_COLOR_TEXT + color.getRGB());
              colorLink.setForeground(color);
              colorLink.pack();
            }
          }
        });

        
        return parent;
    }
    

	@Override
    protected Control createButtonBar(Composite parent) {
    	Control buttonBar = super.createButtonBar(parent);
    	getButton(Dialog.OK).setVisible(false);
    	 
		return buttonBar;
    }

    
    @Override
    public boolean close() {
    	addOrRemoveMouseListener(false);
    	resetManuallySelectedControl();
    	return super.close();
    }


	private void resetManuallySelectedControl() {
		if (lastSelectedControl != null && !lastSelectedControl.isDisposed()) {
			lastSelectedControl.setBackground(lastSelectedColor);
	    }
	}

    
	private void addOrRemoveMouseListener(boolean addOrRemore) {
		controlCount = 0;
		for (Shell shell : getShell().getDisplay().getShells()) {
        	if (shell.isDisposed()) {
        		continue;
        	}
            for (Control control : shell.getChildren()) {
                addOrRemoveMouseListener(control, addOrRemore);
            }
        }
		labelStatus.setText(controlCount + " controls " + (addOrRemore ? "started" : "stopped"));
		labelStatus.pack();
	}


    private void addOrRemoveMouseListener(Control control, boolean addOrRemore) {
        if (control == null || control.isDisposed()) {
        	return;
        }
        controlCount++;
        if (addOrRemore) {
        	control.addMouseMoveListener(mouseListener);
        }
        else {
        	control.removeMouseMoveListener(mouseListener);
        }

        if (control instanceof Composite) {
            Composite composite = (Composite) control;
            for (Control subControl : composite.getChildren()) {
                addOrRemoveMouseListener(subControl, addOrRemore);
            }
        }
    }
    
    public void handleToggle() {
    	boolean btnSelectionState = btnStartStop.getSelection();
		handleToggle(!btnSelectionState);
    }


	private void handleToggle(boolean btnSelectionState) {
		resetManuallySelectedControl();
		if (btnSelectionState) {
			addOrRemoveMouseListener(true);
			btnStartStop.setSelection(true);
			btnStartStop.setText("Stop");
		}
		else {
			if (lastControl != null && !lastControl.isDisposed()) {
		        lastControl.setBackground(lastColor);
		    }
			
			addOrRemoveMouseListener(false);
			btnStartStop.setSelection(false);
			btnStartStop.setText("Start");
		}
	}


	private String getStyleString(int style) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entry : SWT_CONSTS.entrySet()) {
			String styleName = entry.getKey();
			int styleValue = entry.getValue();
			
			if (styleValue != 0 && (style & styleValue) == styleValue) {
				if (sb.length() > 0) {
					sb.append("|");
				}
				sb.append(styleName+"("+styleValue+")");
			}
		}
		return sb.toString();
	}



	private void addTreeItem(Control currentControl, TreeItem rootItem, boolean withCildren) {
		
		TreeItem currItem = createItem(currentControl, rootItem, currentControl+"@"+currentControl.hashCode());
		createItem(currentControl, currItem, "Bounds: "+currentControl.getBounds());
		
		if (currentControl instanceof Composite) {
			createItem(currentControl, currItem, "Layout: "+((Composite)currentControl).getLayout());
		}
		createItem(currentControl, currItem, "LayoutData: "+currentControl.getLayoutData());
		
        createItem(currentControl, currItem, "Shell: "+currentControl.getShell()+"@"+currentControl.getShell().hashCode());
        if (currentControl.getParent() != null) {
			TreeItem parentItem = createItem(currentControl.getParent(), currItem, "Parent: "+currentControl.getParent()+"@"+currentControl.getParent().hashCode());
			addTreeItem(currentControl.getParent(), parentItem, false);
        } else {
        	createItem(currentControl, currItem, "Parent: no parent");
        }
        
		if (withCildren && currentControl instanceof Composite) {
			Control[] children = ((Composite)currentControl).getChildren();
			if (children != null && children.length > 0) {
				TreeItem itemChildren = createItem(currentControl, currItem, "Children: ("+children.length+" children)");
				for (Control control : children) {
					addTreeItem(control, itemChildren, true);
				}
			}
			else {
				createItem(currentControl, currItem, "Children: no children");
			}
		}
	}



	private TreeItem createItem(Control currentControl, TreeItem parentItem, String text) {
		TreeItem item;
		if (parentItem != null) {
			item = new TreeItem(parentItem, SWT.NONE);
		}
		else {
			item = new TreeItem(txtDump, SWT.NONE);
		}
		item.setText(text);
		
		item.setData(currentControl);
		return item;
	}
	


}
