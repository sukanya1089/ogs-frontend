package app.ogs.views;


import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import app.ogs.BackendClient;
import app.ogs.model.Sample;

public class EditSampleDialog extends AddSampleDialog {

    private final Sample originalSample;

    public EditSampleDialog(Shell parentShell, Sample sample) {
        super(parentShell);
        this.originalSample = sample;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Edit Sample");
        setMessage("Modify and save the ground sample.", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control area = super.createDialogArea(parent);

        // Pre-fill form fields
        locationCombo.setText(originalSample.getLocation());
        locationCombo.setEnabled(false);
        depthText.setText(String.valueOf(originalSample.getDepth()));
        datePicker.setDate(
                originalSample.getDateCollected().getYear(),
                originalSample.getDateCollected().getMonthValue() - 1,
                originalSample.getDateCollected().getDayOfMonth()
        );
        unitWeightText.setText(String.valueOf(originalSample.getUnitWeight()));
        waterContentText.setText(String.valueOf(originalSample.getWaterContent()));
        shearStrengthText.setText(String.valueOf(originalSample.getShearStrength()));

        return area;
    }
    
    protected void saveSample(Sample sample) {
    	sample.setId(originalSample.getId());
        BackendClient.updateSample(sample);
    }


}
