package app.ogs.views;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import app.ogs.BackendClient;
import app.ogs.model.Location;
import app.ogs.model.Sample;

import java.time.LocalDate;
import java.util.List;

public class AddSampleDialog extends TitleAreaDialog {

    protected Combo locationCombo;
    protected Text depthText;
    protected DateTime datePicker;
    protected Text unitWeightText;
    protected Text waterContentText;
    protected Text shearStrengthText;

    protected final List<String> locations;

    public AddSampleDialog(Shell parentShell) {
        super(parentShell);
        locations = BackendClient.getLocations();
    }

    @Override
    public void create() {
        super.create();
        setTitle("Add New Ground Sample");
        setMessage("Enter details of the ground sample to add it to the system.", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(2, false));

        createFormFields(container);
        return area;
    }

    private void createFormFields(Composite container) {
        Label locationLabel = new Label(container, SWT.NONE);
        locationLabel.setText("Location:");
        locationCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        locationCombo.setItems(locations.toArray(new String[0]));

        createLabeledText(container, "Depth (m):", depthText = new Text(container, SWT.BORDER));
        createLabeledDate(container, "Date Collected:", datePicker = new DateTime(container, SWT.DATE | SWT.DROP_DOWN));
        createLabeledText(container, "Unit Weight (kN/m³):", unitWeightText = new Text(container, SWT.BORDER));
        createLabeledText(container, "Water Content (%):", waterContentText = new Text(container, SWT.BORDER));
        createLabeledText(container, "Shear Strength (kPa):", shearStrengthText = new Text(container, SWT.BORDER));
    }

    private void createLabeledText(Composite parent, String label, Text textField) {
        new Label(parent, SWT.NONE).setText(label);
        textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createLabeledDate(Composite parent, String label, DateTime dateField) {
        new Label(parent, SWT.NONE).setText(label);
    }

    @Override
    protected void okPressed() {
        try {
            Sample sample = collectAndValidateInput();
            saveSample(sample);
            super.okPressed();
        } catch (IllegalArgumentException ex) {
            MessageDialog.openError(getShell(), "Invalid Input", ex.getMessage());
        }
    }
    
    protected void saveSample(Sample sample) {
        BackendClient.addSample(sample);
    }

    protected Sample collectAndValidateInput() {
        String location = locationCombo.getText();
        if (location.isEmpty()) throw new IllegalArgumentException("Location must be selected.");

        double depth = parseDoubleField(depthText.getText(), "Depth");
        double unitWeight = parseDoubleField(unitWeightText.getText(), "Unit Weight");
        double waterContent = parseDoubleField(waterContentText.getText(), "Water Content");
        double shearStrength = parseDoubleField(shearStrengthText.getText(), "Shear Strength");

        // Validations
        if (waterContent < 5 || waterContent > 150)
            throw new IllegalArgumentException("Water content must be between 5% and 150%.");
        if (unitWeight < 12 || unitWeight > 26)
            throw new IllegalArgumentException("Unit weight must be between 12 and 26 kN/m³.");
        if (shearStrength < 2 || shearStrength > 1000)
            throw new IllegalArgumentException("Shear strength must be between 2 and 1000 kPa.");

        LocalDate date = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDay());

        Sample sample = new Sample();
        sample.setLocation(location);
        sample.setDepth(depth);
        sample.setDateCollected(date);
        sample.setUnitWeight(unitWeight);
        sample.setWaterContent(waterContent);
        sample.setShearStrength(shearStrength);
        return sample;
    }

    private double parseDoubleField(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }
}
