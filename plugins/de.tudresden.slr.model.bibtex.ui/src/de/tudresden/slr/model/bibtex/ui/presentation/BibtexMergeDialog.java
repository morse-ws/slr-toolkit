package de.tudresden.slr.model.bibtex.ui.presentation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.tudresden.slr.model.bibtex.ui.presentation.BibtexMergeData.Criteria;
import de.tudresden.slr.model.bibtex.util.BibtexResourceImpl;

public class BibtexMergeDialog extends Dialog {
	
    private BibtexMergeData mergeData;
    private CheckboxTableViewer ctv;
    private Text filename;
    private Text preview;

    public BibtexMergeDialog(Shell parentShell, BibtexMergeData d) {
        super(parentShell);
        // make non modal
    	setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
    	setBlockOnOpen(false);
    	this.mergeData = d;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        
        buildFilesPart(container); 
        buildPreview(container);       
        buildOptions(container);
        buildCriteria(container);
        buildSavePart(container);
        
        return container;
    }

	private void buildPreview(Composite container) {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.TOP;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 1;
        gridData.verticalSpan = 4;
        
		Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(gridData);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Preview: ");
        preview = new Text(composite, SWT.BORDER  | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        
        GridData textGrid = new GridData(500, 500);
        preview.setLayoutData(textGrid);
        updatePreviewText();
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = SWT.END;
	}
	
	private void updatePreviewText() {
		preview.setText(mergeData.getConflicts().stream()
        		.map(conflict -> conflict.printConflict())
        		.collect(Collectors.joining("\n")));
	}

	private void buildCriteria(Composite container) {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 1;
        gridData.verticalSpan = 1;
        
		Group criteriaGroup = new Group(container, SWT.SHADOW_NONE);
        criteriaGroup.setText("Similarity Criteria");
        criteriaGroup.setLayout(new GridLayout(3, false));
        criteriaGroup.setLayoutData(gridData);
        
        for (Criteria value : Criteria.values()) {
        	// create check box
        	Button b = new Button(criteriaGroup, SWT.CHECK);
            b.setSelection(mergeData.getWeights().get(Criteria.doi) > 0);
            b.setText(getButtonTextForCriteria(value));
            
            // create scale
            Scale scale = new Scale(criteriaGroup, SWT.HORIZONTAL);
            scale.setMinimum(0);
            scale.setMaximum(getMaxScaleValueForCriteria(value));
            scale.setSelection(getDefaultScaleSelectionValueForCriteria(value));
            scale.setIncrement(1);
    	    
    	    // create text box for value of scale
    	    Text text = new Text(criteriaGroup, SWT.CENTER);
    	    text.setText(Integer.toString(scale.getSelection()));    	    
    	    scale.addListener(SWT.Selection, new Listener() {
    	      public void handleEvent(Event event) {
    	        mergeData.getWeights().put(value, scale.getSelection());
    	        text.setText(Integer.toString(scale.getSelection()));
    	        if (scale.getSelection() == 0)
    	        	b.setSelection(false);
    	        else
    	        	b.setSelection(true);
                updatePreviewText();
    	      }
    	    });
            
            // add listener for check box updates
            b.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    mergeData.getWeights().put(value, b.getSelection() ? 95 : 0);
                    scale.setSelection(b.getSelection() ? 95 : 0);
        	        text.setText(Integer.toString(scale.getSelection()));
        	        updatePreviewText();
                }
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    mergeData.getWeights().put(value, b.getSelection() ? 95 : 0);
                    scale.setSelection(b.getSelection() ? getDefaultScaleSelectionValueForCriteria(value) : 0);
        	        text.setText(Integer.toString(scale.getSelection()));
        	        updatePreviewText();
                }
            });
        }
	}
	
	private String getButtonTextForCriteria(Criteria criteria) {
		switch (criteria) {
		case doi:
			return criteria.name() + " equals";
		case year:
			return criteria.name() + "s +-";
		default:
			return criteria.name() + " in %";
		}
	}
	
	private int getMaxScaleValueForCriteria(Criteria criteria) {
		switch (criteria) {
		case doi:
			return 1;
		case year:
			return 10;
		default:
			return 100;
		}
	}
	
	private int getDefaultScaleSelectionValueForCriteria(Criteria criteria) {
		switch (criteria) {
		case doi:
			return 1;
		case year:
			return 5;
		default:
			return 95;
		}
	}

	private void buildFilesPart(Composite container) { 
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = 1;
        gridData.verticalSpan = 1;
        
		Group group = new Group(container, SWT.SHADOW_NONE);
        group.setText("Files to merge");
        group.setLayout(new FillLayout());
        group.setLayoutData(gridData);
        
        Label label = new Label(group, SWT.NONE);
        label.setText("Preview: ");
        label.setText(mergeData.getResourceList().stream()
        		.map(resource -> resource.getURI().toString())
        		.collect(Collectors.joining("\n")));
	}

	private void buildSavePart(Composite container) {
		Composite savePart = new Composite(container, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 1;
        gridData.horizontalAlignment = GridData.FILL;
        savePart.setLayout(new GridLayout(2, false));
        savePart.setLayoutData(gridData);
        Label save = new Label(savePart, SWT.NONE);
        save.setText("Save as: ");
        save.setLayoutData(new GridData (SWT.END, SWT.END, false, false));
        filename = new Text(savePart, SWT.BORDER | SWT.SINGLE);
        filename.setText("mergeResult.bib");
        mergeData.setFilename(filename.getText());
        filename.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
        		mergeData.setFilename(filename.getText());
        		validateDialog();
            }
        });
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = SWT.END;
        filename.setLayoutData(gd);
	}

	private void buildOptions(Composite container) {
		Group options = new Group(container, SWT.SHADOW_NONE);
        options.setText("Options");
        options.setLayout(new RowLayout(SWT.VERTICAL));
        final Button b0 = new Button(options, SWT.CHECK);
        b0.setText("Eliminate duplicates");
        b0.setSelection(true);
        final Button b1 = new Button(options, SWT.CHECK);
        b1.setText("Delete original files after merge");  
        b1.setSelection(false);
        final Button b2 = new Button(options, SWT.CHECK);
        b2.setText("Manually resolve conflicts");  
        b2.setSelection(false);
        b0.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
            	boolean chk = b0.getSelection();
                b2.setEnabled(chk);
            }
        });
        b1.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                
            }
        });   
        b2.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                
            }
        });
	}

	private void validateDialog() {
		boolean valid = filename.getText().matches("[-_. A-Za-z0-9]+\\.bib") && ctv.getCheckedElements().length > 1;
		valid = valid && fileExists();
		getButton(IDialogConstants.OK_ID).setEnabled(valid);
	}
    
    private boolean fileExists() {
    	File tempFile = new File(getFilePath());
    	return tempFile.exists();
    }
	
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Merge BibTeX files");
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        createButton(parent, IDialogConstants.OK_ID, "Merge", true);
    }
    
    @Override
    protected void okPressed() {
    	writePreviewToFile();
    }
    
    private String getFilePath() {
    	BibtexResourceImpl resource1 = mergeData.getResourceList().get(0);
    	String filePath = resource1.getURI().path().substring(0, resource1.getURI().path().length() - resource1.getURI().lastSegment().length());
    	filePath = ResourcesPlugin.getWorkspace().toString() + filePath + filename.getText();
    	return filePath;
    }
    
    private void writePreviewToFile() {	 
    	File file = new File(getFilePath());
    	try {
			if (file.createNewFile())
				Files.write(Paths.get(getFilePath()), preview.getText().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
